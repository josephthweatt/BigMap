package com.example.joseph.bigmap;

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.List;
import java.util.Scanner;

// Interacts with the BigMap server, specifically PHP code made to work with Android
public class APIHandler extends AsyncTask {
    public static String URLHead = "http://jathweatt.com/BigMap/";
    public static String signIn = "signin.php";
    public static String myBroadcastingChannels = "MyBroadcastingChannels.php";

    public static Boolean signInSuccessful;
    public static Boolean isBroadcasting;
    public static ArrayList<Integer> userChannels;
    public static String[] userInputs;
    public static String cachedPHPData; // stores data from server

    public int executeCommand;

    public APIHandler(int command) {
        if (userInputs[0] == null) {
            Log.e("User not entered: ", "No user profile was given");
        }
        executeCommand = command;
    }

    public APIHandler(String[] inputs, int command) {
        signInSuccessful = false;
        isBroadcasting = false;
        userInputs = inputs;
        executeCommand = command;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        switch (executeCommand) {
            case 0:
                signInSuccessful(); break;
            case 1:
                if (isBroadcasting = checkForBroadcastingChannels()) {
                    userChannels = getBroadcastingChannels();
                }
                break;
            case 2:
                userChannels = getBroadcastingChannels(); break;
        }
        return null;
    }

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

            if(response.contains("Welcome back, ")) {
                signInSuccessful = true;
            } else {
                signInSuccessful = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
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

    private Boolean checkForBroadcastingChannels() {
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

    public ArrayList<Integer> getBroadcastingChannels () {
        if (cachedPHPData.contains("Your broadcasting channels: ")) {
            ArrayList<Integer> userChannels = new ArrayList<Integer>();
            Scanner reader = new Scanner(cachedPHPData);

            while (reader.hasNextInt()) {
                userChannels.add(reader.nextInt());
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
}
