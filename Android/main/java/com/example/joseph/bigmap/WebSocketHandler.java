package com.example.joseph.bigmap;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This replaces the location broadcasting methods in APIHandler.java. Whereas APIHandler
 * implemented POST requests to communicate with PHP and stored all data to mySQL,
 * this class will connect with a webSocket and will not store location data to MySQL.
 */
public class WebSocketHandler extends Service{
    SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "StoredUserInfo";

    private WebSocketClient webSocketClient;

    /************* OVERRIDED ABSTRACT SERVICE METHODS ***************/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectWebSocket();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        webSocketClient.close();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
                String channelIds = "";
                for (int i = 0; i < APIHandler.broadcastingChannels.size(); i++) {
                    channelIds += APIHandler.broadcastingChannels.get(i) + " ";
                }

                webSocketClient.send("connect-android "
                        + sharedPreferences.getInt("userId", 0) + " " + channelIds);
                Log.i("Websocket", "Opened");

            }

            @Override
            public void onMessage(String s) {

            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        webSocketClient.connect();
    }
}
