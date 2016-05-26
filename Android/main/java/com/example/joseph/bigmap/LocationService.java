package com.example.joseph.bigmap;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

// Will receive and submit location data to the database
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static HashMap<Long, Coordinates> locationPacket;
    private static APIHandler apiHandler;
    private static FusedLocationProviderApi fusedLocation = LocationServices.FusedLocationApi;
    private static GoogleApiClient googleApiClient;
    private static LocationRequest locationRequest;

    Timer timer;

    public LocationService() {
        locationPacket = new HashMap<Long, Coordinates>();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // look at provider every 5 seconds
        locationRequest.setFastestInterval(1000); // or one second if its convenient
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void stopBroadcastLocation() {
        googleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        timer = new Timer();
        TimerTask submitPacket = new SubmitLocationPacket();
        timer.schedule(submitPacket, 2000, 2000);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Access APIHandler every 'x' seconds to send the locationPacket to the server
    private class SubmitLocationPacket extends TimerTask {
        public void run() {
            apiHandler = new APIHandler(2);
            apiHandler.execute(2);
        }
    }

    public static void clearLocationPacket() {
        locationPacket.clear();
    }

    /****************************
     * Override Maps API methods
     * @param bundle
     ****************************/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Error connecting", connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        locationPacket.put(location.getTime(),
                new Coordinates(location.getLatitude(), location.getLongitude()));
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /*************************************
     * A small class for storing location
     *************************************/
    private class Coordinates {
        public double lat, lon;
        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
