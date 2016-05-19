package com.example.joseph.bigmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

// Wll recieve and submit location data to the databas
public class LocationService extends Service {
    private LocationManager locationManager;
    private Double[] locationPacket;

    private APIHandler handler;

    public LocationService (APIHandler handler) {
        this.handler = handler;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationPacket = new Double[2];

        Timer timer = new Timer();
        TimerTask receiveLocation = new  ReceiveLocationTask();
        TimerTask submitPacket = new SubmitLocationPacket();
        timer.schedule(receiveLocation, 0, 1000);
        timer.schedule(submitPacket, 0, 1000);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();

    private class ReceiveLocationTask extends TimerTask {
        Location location;
        public void run() {
            try {
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                locationPacket[0] = location.getLatitude();
                locationPacket[1] = location.getLongitude();
            } catch (SecurityException e) {
                Log.e("Security Exception: ", "android.permission.ACCESS_FINE_LOCATION");
            }
        }
    }

    // sends location packet to the BigMap database
    private class SubmitLocationPacket extends TimerTask{
        public void run() {
            handler.execute(new Integer(1), locationPacket);
        }
    }
}
