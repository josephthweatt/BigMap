package com.example.joseph.bigmap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChannelActivity extends AppCompatActivity{
    private static String TAG = "ChannelActivity";
    public static final String PREFS_NAME = "StoredUserInfo";
    SharedPreferences sharedPreferences;

    private String header;
    private Button broadcastButton;

    protected int channelId;
    protected Boolean broadcasting;
    static Intent serviceIntent;
    LocationService locationService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_activity);

        channelId = getIntent().getIntExtra("channelId", 0);
        header = "Channel " + channelId;
        ((TextView) findViewById(R.id.channel_header)).setText(header);

        if (serviceIntent == null) {
            serviceIntent = new Intent(ChannelActivity.this, LocationService.class);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        if (sharedPreferences.contains(header)) {
            broadcasting = sharedPreferences.getBoolean(header, false);
        } else {
            broadcasting = false;
        }

        broadcastButton = (Button) findViewById(R.id.channel_button);
        setBroadcastState(broadcasting);
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change current state of broadcasting
                setBroadcastState(!broadcasting);
            }
        });
    }

    public void setBroadcastState(Boolean selectedState) {
        if (selectedState) { // user is broadcasting
            broadcastButton.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.broadcasting));
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                startBroadcasting();
                broadcastButton.setText("Broadcasting");
            } else {
                Toast.makeText(getApplicationContext(),
                        "GPS permissions not granted", Toast.LENGTH_SHORT).show();
            }
        } else { // user not broadcasting
            stopBroadcasting();
            broadcastButton.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.notBroadcasting));
            broadcastButton.setText("Click to broadcast");
        }
    }

    private void startBroadcasting() {
        if (locationService == null) {
            locationService = new LocationService();
        }
        broadcasting = true;
        storeBroadcastState();
        APIHandler.setBroadcastingChannels();
        // TODO: make sure the service doesn't stop every time the activity refreshes
        // begin locationService if the APIHandler just received its first broadcasting channel
        if (APIHandler.broadcastingChannels.size() == 1) {
            startService(serviceIntent);
        }
    }

    private void stopBroadcasting() {
        broadcasting = false;
        storeBroadcastState();
        APIHandler.setBroadcastingChannels();
        // TODO: make sure the service doesn't stop every time the activity refreshes
        // stop the locationService if there are no other channels broadcasting
        if (APIHandler.broadcastingChannels.size() == 0 && locationService != null) {
            stopService(serviceIntent);
        }
    }

    private void storeBroadcastState() {
        // saves the state of channel broadcasting
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(header, broadcasting);
        editor.apply();

        // update the list in the APIHandler
        APIHandler.setBroadcastingChannels();
    }
}
