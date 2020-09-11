package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private Location location;

    private BroadcastPlayer broadcastPlayer;
    private LostApiClient lostApiClient;
    private boolean locationServiceAvailable;

    /**
     * Singleton implementation
     * @return
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

        initLocationService();
        Log.d("LocationService", "LocationService created");
    }


    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationService() {
        lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
        lostApiClient.connect();

        ArrayList<LocationRequest> requests = new ArrayList<>();
        LocationRequest highAccuracy = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requests.add(highAccuracy);

        boolean needBle = false;
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addAllLocationRequests(requests)
                .setNeedBle(needBle)
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(lostApiClient, request);

        LocationSettingsResult locationSettingsResult = result.await();
        LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
        Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case Status.SUCCESS:
                // All location settings are satisfied. The client can make location requests here.
                break;
            case Status.RESOLUTION_REQUIRED:
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
                // Location settings are not satisfied and cannot be resolved.
                break;
            default:
                break;
        }

    }


    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location loc = LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
        if (loc != null) {
            location = loc;
            locationServiceAvailable = true;
        }
        else
            locationServiceAvailable = false;

        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(5000)
                .setSmallestDisplacement(10);

        com.mapzen.android.lost.api.LocationListener listener = new com.mapzen.android.lost.api.LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                // Do stuff
                Log.d("LocationManager", "Location changed");
                location = loc;
                if (broadcastPlayer != null && broadcastPlayer.isWorking())
                    broadcastPlayer.locationChanged();
            }
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, listener);

    }

    @Override
    public void onConnectionSuspended() {
        locationServiceAvailable = false;
    }

    public Location getLocation() {
        return location;
    }

    public float getAzimuth() {
        return location.getBearing();
    }

    public boolean isLocationServiceAvailable() {
        return locationServiceAvailable;
    }
}