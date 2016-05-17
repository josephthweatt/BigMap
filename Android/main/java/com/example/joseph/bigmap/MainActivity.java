package com.example.joseph.bigmap;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    Button submit;
    EditText username;
    EditText password;

    String[] userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfo = new String[2];

        submit = (Button) findViewById(R.id.submit_signin_info);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View args) {
                username = (EditText) findViewById(R.id.enter_username);
                password = (EditText) findViewById(R.id.enter_password);
                userInfo[0] = username.getText().toString();
                userInfo[1] = password.getText().toString();

                // send userInfo to the Server

            }
        });
    }
}