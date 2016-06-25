package com.example.joseph.bigmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainMenuActivity extends AppCompatActivity {
    private static String TAG = "MainMenuActivity";
    public static final String PREFS_NAME = "StoredUserInfo";

    TextView welcome;
    Button channels;

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

                if (handler.isBroadcasting) {
                    Intent channels = new Intent(MainMenuActivity.this, ChannelListActivity.class);
                    startActivity(channels);
                } else {
                    welcome.setText("You are not broadcasting to any channels");
                }
            }
        });
    }
}
