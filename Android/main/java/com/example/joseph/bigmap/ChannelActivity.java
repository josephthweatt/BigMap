package com.example.joseph.bigmap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ChannelActivity extends FragmentActivity implements OnMapReadyCallback {
    private static String TAG = "ChannelActivity";
    public static final String PREFS_NAME = "StoredUserInfo";
    SharedPreferences sharedPreferences;

    private String header;
    private Button broadcastButton;

    protected int channelId;
    protected Boolean broadcasting;
    static Intent serviceIntent;
    LocationService locationService;

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_activity);

        // start map fragment
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment mapFragment = fragmentManager.findFragmentById(R.id.map);
            SupportMapFragment supportMapFragment = (SupportMapFragment) mapFragment;
            supportMapFragment.getMapAsync(this);
        }

        // set header title
        channelId = getIntent().getIntExtra("channelId", 0);
        header = "Channel " + channelId;
        ((TextView) findViewById(R.id.channel_header)).setText(header);

        // service intent used to start LocationService
        if (serviceIntent == null) {
            serviceIntent = new Intent(ChannelActivity.this, LocationService.class);
        }

        // set the state of a button, either from shared preferences or "off" by default
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

    /*******************
     * Maps API Methods
     *******************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}
