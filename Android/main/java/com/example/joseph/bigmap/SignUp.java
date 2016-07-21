package com.example.joseph.bigmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Joseph on 7/20/2016.
 */
public class SignUp extends AppCompatActivity{
    private static String TAG = "SignUp";
    public static final String PREFS_NAME = "StoredUserInfo";

    TextView header;
    EditText username;
    EditText password;
    Button signup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        signup = (Button) findViewById(R.id.submit_signin_info);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                header = (TextView) findViewById(R.id.signup_header);
                username = (EditText) findViewById(R.id.enter_new_username);
                password = (EditText) findViewById(R.id.enter_new_password);

                String[] userInfo = new String[2];
                userInfo[0] = username.getText().toString().trim();
                userInfo[1] = password.getText().toString().trim();

                APIHandler handler = new APIHandler(userInfo, 2);
                handler.execute();
                if (APIHandler.signInSuccessful) {
                    // store user info to sharedPreferences
                    SharedPreferences shared = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("username", userInfo[0]);
                    editor.putString("password", userInfo[1]);
                    editor.apply();

                    Intent goToMainMenu = new Intent(SignUp.this, MainMenuActivity.class);
                    startActivity(goToMainMenu);
                } else {
                    header.setText("User Already exists, try another name");
                }
            }
        });
    }

}
