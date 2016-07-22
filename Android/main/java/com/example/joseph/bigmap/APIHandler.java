package com.example.joseph.bigmap;

import android.content.Context;
import android.content.SharedPreferences;
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

// Interacts with the BigMap server, specifically PHP code made to work with Android
public class APIHandler extends AsyncTask {
    private String TAG = "APIHandler";
    public static final String PREFS_NAME = "StoredUserInfo";
    public static Context context;
    static SharedPreferences sharedPreferences;

    public static String URLHead = "http://192.169.148.214/BigMap/PHP/";
    public static String signIn = "android/SignIn.php";
    public static String signUp = "android/MakeUser.php";
    public static String myBroadcastingChannels = "accounts/MyBroadcastingChannels.php";
    public static String addChannel = "android/joinChannel.php";
    public static String receiveLocationPacket = "ChannelSocket/ReceiveLocationPacket.php";

    public static Boolean signInSuccessful;
    public static Boolean isBroadcasting;
    public static ArrayList<Integer> userChannels;
    public static ArrayList<Integer> broadcastingChannels;
    private static String[] userInputs;
    private static String cachedPHPData; // stores data from server
    private int executeCommand;

    public APIHandler(int command) {
        if (userInputs == null) {
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

    public void setCommand(int command) {
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
                signIn();
                break;
            case 1:
                if (isBroadcasting = isBroadcastingChannels()) {
                    userChannels = getRegisteredChannels();
                }
                break;
            case 2:
                signUp();
                break;
            case 3:
                if (channelToAdd > 0) {
                    addChannel();
                }
                break;
        }
        return null;
    }

    /****************************************
     * Methods interacting with BigMap Server
     ****************************************/
    private void signIn() {
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
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    if (line.matches(".*\\d+.*")) { // check if there's an number in the line
                        signInSuccessful = true;
                        // save user id
                        int id = Integer.parseInt(line.trim());
                        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("userId", id);
                        editor.apply();
                        Log.i(TAG, "User stored with id " + id);
                        return;
                    }
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

    public void signUp() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("signup[]", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("signup[]", userInputs[1]));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + signUp);
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
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    if (line.matches(".*\\d+.*")) { // check if there's an number in the line
                        signInSuccessful = true;
                        // save user id
                        int id = Integer.parseInt(line.trim());
                        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("userId", id);
                        editor.apply();
                        Log.i(TAG, "User stored with id " + id);
                        return;
                    } else if (line.contains("User already exists")) {
                        signInSuccessful = false;
                        Log.i(TAG, "User already exists");
                    }
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

    /**
     * addChannel() does not accept parameters because it is an Async Task, but the
     * int's below serve as both the params and returns, which can be called from
     * an APIHandler's objective to set or read.
     *
     * @param channelToAdd - mush be assigned prior to executing addChannel
     * @return addChannelStatusCode - a number to relay success of adding a channel
     *          Examples:
     *                  1 - successful
     *                  2 - already joined
     *                  3 - channel doesn't exist
     */
    public int channelToAdd = 0;
    public int addChannelStatusCode = 0;
    public void addChannel() {
        List<AbstractMap.SimpleEntry> parameters = new ArrayList<AbstractMap.SimpleEntry>();
        parameters.add(new AbstractMap.SimpleEntry("name", userInputs[0]));
        parameters.add(new AbstractMap.SimpleEntry("password", userInputs[1]));
        parameters.add(new AbstractMap.SimpleEntry("channel-id", channelToAdd));

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        try {
            // get output stream for the connection and write the parameter query string to it
            url = new URL(URLHead + addChannel);
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
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Success")) {
                        userChannels = getRegisteredChannels();
                        Log.i(TAG, "Joined Channel " + channelToAdd);
                        channelToAdd = 0; // reset channel
                        addChannelStatusCode = 1;
                        return;
                    } else if (line.contains("already joined")) {
                        addChannelStatusCode = 2;
                        Log.i(TAG, "User already joined this channel");
                    } else if (line.contains("Channel does not exist")) {
                        addChannelStatusCode = 3;
                        Log.i(TAG, "Channel does not exist");
                    }
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
