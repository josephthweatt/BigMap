package com.example.joseph.bigmap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Interacts with the BigMap server, specifically PHP code made to work with Android
public class APIHandler extends AsyncTask implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static String URLHead = "http://jathweatt.com/BigMap/";
    public static String signIn = "signin.php";
    public static String myBroadcastingChannels = "MyBroadcastingChannels.php";

    public static Boolean signInSuccessful;
    public static Boolean isBroadcasting;
    public static ArrayList<Integer> userChannels;
    public static String[] userInputs;
    public static String cachedPHPData; // stores data from server
    private static HashMap<Long, Coordinates> locationPacket;

    private Context mContext;
    private static FusedLocationProviderApi fusedLocation = LocationServices.FusedLocationApi;
    private static GoogleApiClient googleApiClient;
    private static LocationRequest locationRequest;

    public int executeCommand;

    public APIHandler(int command) {
        if (userInputs[0] == null) {
            Log.e("User not entered: ", "No user profile was given");
            return;
        }
        executeCommand = command;
    }

    public APIHandler(String[] inputs, int command, Context context) {
        mContext = context;
        signInSuccessful = false;
        isBroadcasting = false;
        userInputs = inputs;
        executeCommand = command;
        locationPacket = new HashMap<Long, Coordinates>();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        switch (executeCommand) {
            case 0:
                signInSuccessful();
                break;
            case 1:
                if (isBroadcasting = isBroadcastingChannels()) {
                    userChannels = getBroadcastingChannels();
                }
                break;
            case 2:
                googleApiClient = new GoogleApiClient.Builder(mContext)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                locationRequest = new LocationRequest();
                locationRequest.setInterval(5000); // look at provider every 5 seconds
                locationRequest.setFastestInterval(1000); // or one second if its convenient
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                googleApiClient.connect();
                break;
        }
        return null;
    }

    /****************************************
     * Methods interacting with BigMap Server
     ****************************************/
    private void signInSuccessful() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("user-info[]", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("user-info[]", userInputs[1]));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + signIn.toLowerCase());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(parameters));
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            String line;
            String response = "";
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    response += line;
                }
            }

            if (response.contains("Welcome back, ")) {
                signInSuccessful = true;
            } else {
                signInSuccessful = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Boolean isBroadcastingChannels() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("user-info[]", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("user-info[]", userInputs[1]));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + myBroadcastingChannels);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(parameters));
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            String line;
            String response = "";
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    response += line;
                }
            }
            if (response.contains("Your broadcasting channels: ")) {
                cachedPHPData = response;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private ArrayList<Integer> getBroadcastingChannels() {
        String title = "Your broadcasting channels: ";
        if (cachedPHPData.contains(title)) {
            ArrayList<Integer> userChannels = new ArrayList<Integer>();
            String[] channels
                    = cachedPHPData.substring(title.length(), cachedPHPData.length()).split(" ");

            for (String number : channels) {
                userChannels.add(Integer.parseInt(number));
            }
            return userChannels;
        }
        return null;
    }

    // will send location packet from LocationService to the server
    private Boolean sendLocationPacket(Double[] packet) {
        return false; //temporary
    }

    // method turns query params to a POST String. I found the method on this stackoverflow thread:
    // stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
    private String getQuery(List<AbstractMap.SimpleEntry> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey().toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue().toString(), "UTF-8"));
        }

        return result.toString();
    }

    /*******************************
     * Non-asynchronous messages
     ******************************/
    public static String[] channelsAsString() {
        String[] channels = new String[userChannels.size()];
        for (int i = 0; i < userChannels.size(); i++) {
            channels[i] = userChannels.get(i).toString();
        }
        return channels;
    }

    /*******************
     * Google Maps API
     * @param bundle
     *******************/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Error connecting", connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        locationPacket.put(location.getTime(),
                new Coordinates(location.getLatitude(), location.getLongitude()));
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /********************************************
     * A simple class to hold coordinate objects
     ********************************************/
    private class Coordinates {
        public double lat, lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
