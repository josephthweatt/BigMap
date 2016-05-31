package com.example.joseph.bigmap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
    LocationService locationService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_activity);

        channelId = getIntent().getIntExtra("channelId", 0);
        header = "Channel " + channelId;
        ((TextView) findViewById(R.id.channel_header)).setText(header);

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        if (sharedPreferences.contains(header)) {
            broadcasting = sharedPreferences.getBoolean(header, false);
        } else {
            broadcasting = false;
        }

        broadcastButton = (Button) findViewById(R.id.channel_button);
        setButtonState(broadcasting);
        broadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change current state of broadcasting
                setButtonState(!broadcasting);
            }
        });
    }

    @Override
    public void onPause() {
        // saves the state of channel broadcasting
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(header, broadcasting);
        editor.apply();
        super.onPause();
    }

    public void setButtonState(Boolean selectedState) {
        if (selectedState) { // user is broadcasting
            broadcastButton.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.broadcasting));
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationService = new LocationService();
                startService(new Intent(ChannelActivity.this, LocationService.class));

                broadcastButton.setText("Broadcasting");
            } else {
                Toast.makeText(getApplicationContext(),
                        "GPS permissions not granted", Toast.LENGTH_SHORT).show();
            }
        } else { // user not broadcasting
            broadcastButton.setBackgroundColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.notBroadcasting));
            broadcastButton.setText("Click to broadcast");
        }
        broadcasting = selectedState;
    }
}
