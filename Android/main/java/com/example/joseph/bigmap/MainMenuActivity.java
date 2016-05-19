package com.example.joseph.bigmap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Joseph on 5/19/2016.
 */
public class MainMenuActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "StoredUserInfo";

    TextView welcome;

    @Override
    public void onCreate(Bundle savedInstanceVariable) {
        super.onCreate(savedInstanceVariable);
        setContentView(R.layout.main_menu);

        // set welcome header
        SharedPreferences shared = getSharedPreferences(PREFS_NAME, 2);
        welcome = (TextView) findViewById(R.id.welcome_back);
        welcome.setText("Welcome back, " + shared.getString("username", "") + "!");


    }
}
