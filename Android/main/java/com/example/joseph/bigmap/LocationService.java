package com.example.joseph.bigmap;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Locale;

// Will receive and submit location data to the database.
// This class calls WebSocketService to connect to the server
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static String TAG = "LocationService";

    private static AbstractMap.SimpleEntry<String, Coordinates> locationPacket;
    private static FusedLocationProviderApi fusedLocation = LocationServices.FusedLocationApi;
    private static GoogleApiClient googleApiClient;
    private static LocationRequest locationRequest;
    protected static WebSocket webSocket;

    public static int activeChannel = 0;
    public static String[] broadcasterBatch;

    public LocationService() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000); // look at provider every 5 seconds
        locationRequest.setFastestInterval(500); // or one second if its convenient
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        // start running WebSocket
        webSocket = new WebSocket();
        webSocket.connectWebSocket();

        Log.i(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        googleApiClient.disconnect();
        locationPacket = null;
        webSocket.disconnect();

        Log.i(TAG, "Service stopped");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /****************************
     * Override Maps API methods
     ****************************/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: find out what needs to be put here
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    /*
     * WARNING: immediately sending location to the websocket might cause the
     * high traffic on the socket. Make sure this isn't the case,
     * and bring back the timertask if the issue exists
     */
    @Override
    public void onLocationChanged(Location location) {
        if (setLocationPacket(location)) { // send location if it changed
            if (webSocket.connected && APIHandler.broadcastingChannels != null
                    && APIHandler.broadcastingChannels.size() > 0) {
                webSocket.sendLocation();
            }
        }
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocation.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /**************************
     * LocationPacket functions
     **************************/
    /**
     * @param location - current location grabbed by LocationPacket
     * @return Boolean - returns true if the locationPacket was updated
     */
    public Boolean setLocationPacket(Location location) {
        try {
            if (location.getLatitude() != locationPacket.getValue().lat
                    || location.getLongitude() != locationPacket.getValue().lon) {
                locationPacket = new AbstractMap.SimpleEntry<>(timeAsString(location),
                        new Coordinates(location.getLatitude(), location.getLongitude()));
                return true;
            }
        } catch (NullPointerException e) {
            locationPacket = new AbstractMap.SimpleEntry<>(timeAsString(location),
                    new Coordinates(location.getLatitude(), location.getLongitude()));
            return true;
        }
        return false;
    }

    public static AbstractMap.SimpleEntry<String, Coordinates> getLocationPacket() {
        return locationPacket;
    }

    public static void clearLocationPacket() {
        locationPacket = null;
    }

    private String timeAsString(Location location) {
        // code taken from: stackoverflow.com/questions/12747549/android-location-time-into-date
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Date date = new Date(location.getTime());
        return format.format(date);
    }

    /*************************************
     * A small class for storing location
     *************************************/
    protected class Coordinates {
        public double lat, lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    /*****************************************************************************************
     * WebSocket methods to connect, receive, and share user's location.
     * This replaces the location broadcasting methods in APIHandler.java. Whereas APIHandler
     * implemented POST requests to communicate with PHP and stored all data to mySQL,
     * this class will connect with a webSocket and will not store location data to MySQL.
     *****************************************************************************************/
    public class WebSocket {
        public static final String PREFS_NAME = "StoredUserInfo";
        SharedPreferences sharedPreferences;

        public Boolean connected = false;
        private WebSocketClient webSocketClient;

        /************ WEBSOCKET METHODS ****************/
        private void connectWebSocket() {
            URI uri;
            try {
                uri = new URI("ws://192.169.148.214:2000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
                    int id = sharedPreferences.getInt("userId", 0);
                    webSocketClient.send("connect-android " + id + " " + getChannelIds());
                    Log.i("Websocket", "Opened");
                }

                @Override
                public void onMessage(String s) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    String[] segments = s.split(" ");
                    if (segments[0].equals("broadcaster-batch")) {
                        // send the broadcaster batch to the receiver in ChannelActivity
                        bundle.putStringArray("broadcaster-batch", segments);
                        Log.i(TAG, "Got broadcaster batch");
                    } else if (activeChannel == Integer.parseInt(segments[segments.length - 1])) {
                        // send data to map in ChannelActivity
                        bundle.putStringArray("broadcaster-update", segments);
                        Log.i(TAG, "Got location update");
                    }
                    intent.putExtras(bundle);
                    intent.setAction("BROADCAST_ACTION");
                    sendBroadcast(intent);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    connected = false;
                    Log.i("Websocket", "Closed " + s);
                }

                @Override
                public void onError(Exception e) {
                    Log.i("Websocket", "Error " + e.getMessage());
                }
            };
            webSocketClient.connect();
            connected = true;
        }

        public void sendLocation() {
            try {
                String channelIds = getBroadcastingChannelIds();
                webSocketClient.send("update-location-android "
                        + locationPacket.getValue().lat + " "
                        + locationPacket.getValue().lon + " "
                        + channelIds);
                Log.i(TAG, "Sent location to channels: " + channelIds);
            } catch (WebsocketNotConnectedException e) {
                Log.w(TAG, "BigMap tried to send a location with the connection closed");
            } catch (NullPointerException e) {
                Log.w(TAG, "User's location has not been found yet");
            }
        }

        // called when halting the broadcast of the last active channel
        public void stopBroadcasting() {
            try {
                webSocketClient.send("STOP_BROADCASTING");
                Log.i(TAG, "No longer broadcasting to any channels");
            } catch (WebsocketNotConnectedException e) {
                Log.w(TAG, "Tried to stop broadcast, but websocket appears disconnected");
            }
        }

        public void disconnect() {
            try {
                webSocketClient.send("STOP_BROADCASTING");
                webSocketClient.close();
                connected = false;
            } catch (WebsocketNotConnectedException e) {
                Log.w(TAG, "Tried to stop broadcast, but websocket appears disconnected");
            }
        }

        /**
         * This method uses the websocket to send a string[] to addBroadcasterMarkers[]
         * in ChannelActivity.java
         */
        public void getAllBroadcastersLocation(int channelId) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
            int id = sharedPreferences.getInt("userId", 0);
            webSocketClient.send("get-all-broadcasters " + id + " " + channelId);
            // TODO: create this get-all-broadcasters instruction in the PHP websocket
        }

        //returns a string of the users registered channel ids
        private String getChannelIds() {
            String channelIds = "";
            for (int i = 0; i < APIHandler.userChannels.size(); i++) {
                channelIds += APIHandler.userChannels.get(i) + " ";
            }
            return channelIds.trim();
        }

        //returns a string of the users broadcasting channel ids
        private String getBroadcastingChannelIds() {
            String channelIds = "";
            for (int i = 0; i < APIHandler.broadcastingChannels.size(); i++) {
                channelIds += APIHandler.broadcastingChannels.get(i) + " ";
            }
            return channelIds.trim();
        }
    }

    // this will be called if something has caused the websocket to
    // disconnect while broadcasting (i.e. connection changes)
    public static void resetWebsocketConnection () {
        webSocket.connectWebSocket();
    }
}
