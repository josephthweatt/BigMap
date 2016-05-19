package com.example.joseph.bigmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "StoredUserInfo";
    TextView header;
    EditText username;
    EditText password;
    Button submit;

    String[] userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfo = new String[2];

        submit = (Button) findViewById(R.id.submit_signin_info);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View args) {
                header = (TextView) findViewById(R.id.main_header);
                header.setText("Please Wait...");

                username = (EditText) findViewById(R.id.enter_username);
                password = (EditText) findViewById(R.id.enter_password);
                userInfo[0] = username.getText().toString().trim();
                userInfo[1] = password.getText().toString().trim();

                // send userInfo to the Server, wait 5 secs max for response
                APIHandler handler = new APIHandler(userInfo);
                try {
                    handler.execute(0).get(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException|TimeoutException|ExecutionException e) {
                    e.printStackTrace();
                }

                // launch the next activity (user's main menu)
                if (handler.signInSuccessful) {
                    // store user info to sharedPreferences
                    SharedPreferences shared = getSharedPreferences(PREFS_NAME, 2); // 2 = writeable
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("username", userInfo[0]);
                    editor.putString("password", userInfo[1]);
                    editor.apply();

                    Intent gotoMainMenu = new Intent(MainActivity.this, MainMenuActivity.class);
                    startActivity(gotoMainMenu);
                } else {
                    header.setText("Login Failed, try again");
                }
            }
        });
    }
}