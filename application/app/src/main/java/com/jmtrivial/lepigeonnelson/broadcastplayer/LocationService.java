package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationService implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; //1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;
    private final Context context;

    private LocationManager locationManager;
    public Location location;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
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
        this.isGPSEnabled = false;
        this.isNetworkEnabled = false;
        if (forceNetwork) isGPSEnabled = false;
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

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            updateLocationService();
        } catch (Exception ex) {
            Log.d("LocationService", "Error creating location service: " + ex.getMessage());
        }
    }

    private void updateLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationService", "Missing permission.");
            return;
        }

        if (!isNetworkEnabled && !isGPSEnabled) {
            // cannot get location
            this.locationServiceAvailable = false;
        }
        else {
            this.locationServiceAvailable = true;

            if (isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ignored
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER))
            this.isGPSEnabled = true;
        if (provider.equals(LocationManager.NETWORK_PROVIDER))
            this.isNetworkEnabled = true;
        updateLocationService();
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER))
            this.isGPSEnabled = false;
        if (provider.equals(LocationManager.NETWORK_PROVIDER))
            this.isNetworkEnabled = false;
        updateLocationService();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationManager", "Location changed");
        this.location = location;
        if (broadcastPlayer != null)
            broadcastPlayer.locationChanged();
    }
}