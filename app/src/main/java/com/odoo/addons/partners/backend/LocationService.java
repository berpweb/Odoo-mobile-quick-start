package com.odoo.addons.partners.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = LocationService.class.getSimpleName();

    private static boolean started = false;
    private static LocationListener locationListener;
    private static LatLng latLng;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    public static void start(Context context) {
        if (!LocationService.started) {
            context.startService(new Intent(context, LocationService.class));
        }
    }

    public static void start(Context context, LocationListener locationListener) {
        LocationService.start(context);
        LocationService.locationListener = locationListener;
    }

    public static LatLng getLatLng() {
        return latLng;
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = null;
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        if (mLastLocation != null) {
            // Log.i(TAG, "Location is: " + mLastLocation.getLatitude() + ":" + mLastLocation.getLongitude());
            LocationService.latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (LocationService.locationListener != null) {
                try {
                    LocationService.locationListener.onLocation(LocationService.latLng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Log.w(TAG, "NULL location");
            if (LocationService.locationListener != null) {
                LocationService.locationListener.onLocation(null);
            }
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        // Stopping Service
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Log.i(TAG, "onStartCommand");
        started = true;

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Log.i(TAG, "onDestroy");
        started = false;
        super.onDestroy();
    }

    public interface LocationListener {
        void onLocation(LatLng latLng);
    }
}
