package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.gyroscope.KalmanGyroscopeSensor;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LocationSettingsStates;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.Status;

import java.util.ArrayList;


public class SensorsService implements LostApiClient.ConnectionCallbacks {

    private static SensorsService instance = null;
    private final Context context;
    private final Activity activity;
    public static final int REQUEST_CHECK_SETTINGS = 100;
    private final KalmanGyroscopeSensor sensor;
    private final MeanFilter meanFilter;
    private LocationRequest request;

    private final int refreshDelayGPSms = 500;
    private final int smallestDisplacementGPS = 10;

    private float[] fusedOrientation = new float[3];


    private Location location;

    private BroadcastPlayer broadcastPlayer;
    private LostApiClient lostApiClient;

    private SensorSubject.SensorObserver sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {
            updateValues(values);
        }
    };

    public boolean isLocationAvailable() {
        return locationAvailable;
    }

    private boolean locationAvailable;

    private void updateValues(float[] values) {
        fusedOrientation = values;
        fusedOrientation = meanFilter.filter(fusedOrientation);
    }

    /**
     * Singleton implementation
     */
    public static SensorsService getSensorsService(Activity activity) {
        if (instance == null) {
            instance = new SensorsService(activity);
        }
        return instance;
    }

    public static SensorsService getSensorsService() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public void register(BroadcastPlayer broadcastPlayer) {
        this.broadcastPlayer = broadcastPlayer;
    }

    /**
     * Local constructor
     */
    private SensorsService(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        broadcastPlayer = null;
        this.sensor = new KalmanGyroscopeSensor(context);
        this.sensor.register(sensorObserver);
        this.sensor.start();
        this.meanFilter = new MeanFilter();
        // do we need meanFilter.setTimeConstant(...); ?

        locationAvailable = false;

        request = null;

        lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
        lostApiClient.connect();

        Log.d("LocationService", "LocationService created");
    }


    @Override
    public void onConnected() {
        Log.d("SensorsService", "connected");
        request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).
                setInterval(refreshDelayGPSms).setSmallestDisplacement(smallestDisplacementGPS);
        checkSensorsSettings();
    }

    @Override
    public void onConnectionSuspended() {
        location = null;
    }

    public Location getLocation() {
        return location;
    }

    public float getAzimuth() {
        return (float) (Math.toDegrees(fusedOrientation[0]) + 360) % 360;
    }


    @SuppressLint("MissingPermission")
    public void checkSensorsSettings() {
        if (request != null) {
            ArrayList<LocationRequest> requests = new ArrayList<>();
            requests.add(request);
            boolean needBle = false;
            LocationSettingsRequest sRequest = new LocationSettingsRequest.Builder()
                    .addAllLocationRequests(requests)
                    .setNeedBle(needBle)
                    .build();

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(lostApiClient, sRequest);

            LocationSettingsResult locationSettingsResult = result.await();
            LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
            Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case Status.SUCCESS:
                    Log.d("SensorsService", "success");
                    locationAvailable = true;
                    Location loc = LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
                    if (loc != null) {
                        location = loc;
                    }

                    // All location settings are satisfied. The client can make location requests here.
                    com.mapzen.android.lost.api.LocationListener listener =
                            new com.mapzen.android.lost.api.LocationListener() {
                        @Override
                        public void onLocationChanged(Location loc) {
                            // Do stuff
                            Log.d("LocationManager", "Location changed");
                            location = loc;
                            if (broadcastPlayer != null && broadcastPlayer.isWorking())
                                broadcastPlayer.locationChanged();
                        }
                    };
                    LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient,
                            request, listener);
                    break;
                case Status.RESOLUTION_REQUIRED:
                    Log.d("SensorsService", "resolution required");
                    // Location requirements are not satisfied. Redirect user to system settings for resolution.
                    try {
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
                case Status.INTERNAL_ERROR:
                case Status.INTERRUPTED:
                case Status.TIMEOUT:
                case Status.CANCELLED:
                    Log.d("SensorsService", "error during initialization");

                    // Location settings are not satisfied and cannot be resolved.
                    locationAvailable = false;
                    break;
                default:
                    break;
            }
        }

    }
}