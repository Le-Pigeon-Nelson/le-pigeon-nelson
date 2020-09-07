package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class LocationService implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 100;

    private static LocationService instance = null;
    private final Context context;

    private LocationManager locationManager;
    public Location location;
    public boolean locationServiceAvailable;

    private BroadcastPlayer broadcastPlayer;

    /**
     * Singleton implementation
     * @return
     */
    public static LocationService getLocationManager(Context context) {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }
    public static LocationService getLocationManager() {
        if (instance == null) {  return null; }
        return instance;
    }

    public void register(BroadcastPlayer broadcastPlayer) {
        this.broadcastPlayer = broadcastPlayer;
    }

    /**
     * Local constructor
     */
    private LocationService(Context context) {
        this.locationServiceAvailable = false;
        this.context = context;
        broadcastPlayer = null;

        initLocationService();
        Log.d("LocationService", "LocationService created");
    }


    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationService() {

        try {
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS status
            this.locationServiceAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            updateLocationService();
        } catch (Exception ex) {
            Log.d("LocationService", "Error creating location service: " + ex.getMessage());
        }
    }

    private void updateLocationService() {
        locationManager.removeUpdates(this);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationService", "Missing permission.");
            return;
        }

        if (locationServiceAvailable) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        else {
            Log.w("LocationService", "GPS is not available");
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ignored
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationServiceAvailable = true;
        updateLocationService();
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationServiceAvailable = false;
        updateLocationService();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationManager", "Location changed");
        this.location = location;
        if (broadcastPlayer != null && broadcastPlayer.isWorking())
            broadcastPlayer.locationChanged();
    }
}