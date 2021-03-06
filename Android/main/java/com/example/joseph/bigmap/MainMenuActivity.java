package com.example.joseph.bigmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainMenuActivity extends AppCompatActivity {
    private static String TAG = "MainMenuActivity";
    public static final String PREFS_NAME = "StoredUserInfo";

    static LocationService locationService;
    static Intent serviceIntent;

    TextView welcome;
    Button channels;
    Button makeChannel;
    EditText newChannelId;
    Button newChannelSubmit;

    @Override
    public void onCreate(Bundle savedInstanceVariable) {
        super.onCreate(savedInstanceVariable);
        setContentView(R.layout.main_menu);

        // set welcome header
        SharedPreferences shared = getSharedPreferences(PREFS_NAME, 0);
        welcome = (TextView) findViewById(R.id.welcome_back);
        welcome.setText("Welcome back, " + shared.getString("username", "") + "!");

        channels = (Button) findViewById(R.id.enter_channels);
        channels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcome.setText("Connecting to Channels...");
                // see if the user has any channels, allow up to 5 seconds
                APIHandler handler = new APIHandler(1);
                try {
                    handler.execute().get(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException|TimeoutException |ExecutionException e) {
                    welcome.setText("Connection Failed");
                    Toast.makeText(getApplicationContext(),
                            "Can't connect to server", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }

                if (APIHandler.isBroadcasting) {
                    Intent channels = new Intent(
                            MainMenuActivity.this, ChannelListActivity.class);
                    startActivity(channels);
                } else {
                    welcome.setText("You have no channels");
                }
            }
        });

        makeChannel = (Button) findViewById(R.id.add_channel);
        makeChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newChannelId = (EditText) findViewById(R.id.new_channel_id);
                newChannelId.setVisibility(View.VISIBLE);
                newChannelSubmit = (Button) findViewById(R.id.new_channel_id_submit);
                newChannelSubmit.setVisibility(View.VISIBLE);
                newChannelSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        APIHandler handler = new APIHandler(3);
                        int newChannel = Integer.parseInt(newChannelId.getText().toString());
                        handler.channelToAdd
                                = newChannel;
                        try {
                            handler.execute().get(5000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | TimeoutException | ExecutionException e) {
                            Toast.makeText(getApplicationContext(),
                                    "Can't connect to server", Toast.LENGTH_SHORT).show();
                        }
                        if (handler.addChannelStatusCode == 1) {
                            Toast.makeText(getApplicationContext(),
                                    "Channel " + newChannel + " added", Toast.LENGTH_SHORT).show();
                        } else if (handler.addChannelStatusCode == 2) {
                            Toast.makeText(getApplicationContext(),
                                    "You have already joined Channel " + newChannel,
                                    Toast.LENGTH_SHORT).show();
                        } else if (handler.addChannelStatusCode == 3) {
                            Toast.makeText(getApplicationContext(),
                                    "Channel " + newChannel + " does not exist",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // LocationService is started here
        locationService = new LocationService();
        if (serviceIntent == null) {
            serviceIntent = new Intent(MainMenuActivity.this, LocationService.class);
        }
        startService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, R.style.SignoutAlertDialog));
        builder.setMessage("Returning back to the sign in will sign you out. " +
                "You cannot broadcast without an account.\n\nContinue?");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(serviceIntent);
                        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear().apply();
                        Intent signIn = new Intent(MainMenuActivity.this, MainActivity.class);
                        startActivity(signIn);
                    }
                }
        );
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // stay in menu
                    }
                }
        );
        AlertDialog alert = builder.create();
        alert.show();
    }
}
