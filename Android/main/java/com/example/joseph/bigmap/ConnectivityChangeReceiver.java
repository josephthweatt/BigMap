package com.example.joseph.bigmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

// this will keep the application broadcasting even when there's
// a connection change--like when a user switches to 4G
public class ConnectivityChangeReceiver extends BroadcastReceiver {
    public final String TAG = "Conn...ChangeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Connection changed");
        ConnectivityManager cm
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            // reboot service
           LocationService.resetWebsocketConnection();
        }
    }
}
