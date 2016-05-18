package com.example.joseph.bigmap;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// Wll recieve and submit location data to the databas
public class LocationService extends Service {
    private LocationManager locationManager;
    private Map<Double, Double> locationPacket;

    private APIHandler handler;

    public LocationService (APIHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationPacket = new HashMap<Double, Double>();

        Timer timer = new Timer();
        TimerTask receiveLocation = new  ReceiveLocationTask();
        TimerTask submitPacket = new SubmitLocationPacket();
        timer.schedule(receiveLocation, 0, 1000);
        timer.schedule(submitPacket, 0, 1000);

    }


    private class ReceiveLocationTask extends TimerTask {
        Location location;
        public void run() {
            try {
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                locationPacket.put(location.getLatitude(), location.getLongitude());
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
