package com.example.joseph.bigmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Interacts with the BigMap server, specifically PHP code made to work with Android
public class APIHandler extends AsyncTask {
    private String TAG = "APIHandler";
    public static final String PREFS_NAME = "StoredUserInfo";
    public static Context context;
    static SharedPreferences sharedPreferences;

    public static String URLHead = "http://jathweatt.com/BigMap/PHP/";
    public static String signIn = "SignIn.php";
    public static String myBroadcastingChannels = "MyBroadcastingChannels.php";
    public static String receiveLocationPacket = "ReceiveLocationPacket.php";

    public static Boolean signInSuccessful;
    public static Boolean isBroadcasting;
    public static ArrayList<Integer> userChannels;
    public static ArrayList<Integer> broadcastingChannels;
    private static String[] userInputs;
    private static String cachedPHPData; // stores data from server
    private int executeCommand;

    public APIHandler(int command) {
        if (userInputs[0] == null) {
            Log.e(TAG, "No user profile was given");
            return;
        }
        executeCommand = command;
    }

    // should be the first constructor called (to set the static methods
    public APIHandler(String[] inputs, int command) {
        signInSuccessful = false;
        isBroadcasting = false;
        userInputs = inputs;
        executeCommand = command;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    // TODO: add Javadoc for the switchcase
    @Override
    protected Object doInBackground(Object[] params) {
        switch (executeCommand) {
            case 0:
                signInSuccessful();
                break;
            case 1:
                if (isBroadcasting = isBroadcastingChannels()) {
                    userChannels = getRegisteredChannels();
                }
                break;
            case 2:
                if (sendLocationPacket()) {
                    String ids = "";
                    for (int i : broadcastingChannels) {
                        ids += i + " ";
                    }
                    Log.i(TAG, "locationPacket successfully sent to channel: "
                            + ids);
                } else {
                    Log.e(TAG, "error sending locationPacket to server");
                }
                break;
            case 3:
                sendStop();
                Log.i(TAG, "no longer sending locationPackets");
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
            url = new URL(URLHead + signIn);
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
            signInSuccessful = response.contains("Welcome back, ");
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

    // returns all registered channels (even ones not currently broadcasting)
    private ArrayList<Integer> getRegisteredChannels() {
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

    // results found in broadcastingChannels
    protected static void setBroadcastingChannels() {
        if (broadcastingChannels == null) {
            broadcastingChannels = new ArrayList<>();
        }
        // TODO: find out when/where the context should be assigned, its currently at login
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);

        broadcastingChannels.clear();
        for (Integer i : userChannels) {
            // check if user wants to broadcast the channel
            if (sharedPreferences.getBoolean("Channel " + i, false)) {
                broadcastingChannels.add(i);
            }
        }
    }

    /*****************************************************************************
     *  The server receives location with 4 types of inputs, in the form of POST:
     *      1.) the user's info:        [userInfo]
     *      2.) channels broadcasting:  [channelIds]...
     *      3.) the locationPackets:    [time, latitude, longitude]...
     *      4.) the number of packets   [packetCount]
     *  Returns true if the information was successfully stored.
     *****************************************************************************/
    private Boolean sendLocationPacket() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("userInfo[]", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("userInfo[]", userInputs[1]));
        for (int i : broadcastingChannels) {
            parameters.add(new AbstractMap.SimpleEntry("channelIds[]", i));
        }
        // put locationPacket into POST format
        HashMap locationPacket = LocationService.getLocationPacket();
        LocationService.Coordinates coordinates;
        Iterator iterator = locationPacket.entrySet().iterator();
        int packetNumber = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            coordinates = (LocationService.Coordinates) entry.getValue();

            parameters.add(new AbstractMap.SimpleEntry(
                    "locationPacket" + packetNumber + "[]", entry.getKey()));
            parameters.add(new AbstractMap.SimpleEntry(
                    "locationPacket" + packetNumber + "[]", coordinates.lat));
            parameters.add(new AbstractMap.SimpleEntry(
                            "locationPacket" + packetNumber + "[]", coordinates.lon));
            packetNumber++;
        }
        parameters.add(new AbstractMap.SimpleEntry("packetCount", packetNumber));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + receiveLocationPacket);
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
            if (response.contains("Locations properly stored")) {
                LocationService.clearLocationPacket();
                return true;
            } else {
                return false;
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

    // call for the server to hide the user's location
    private void sendStop() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("userInfo[]", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("userInfo[]", userInputs[1]));
        parameters.add(new AbstractMap.SimpleEntry("STOP", true));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + receiveLocationPacket);
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
}
