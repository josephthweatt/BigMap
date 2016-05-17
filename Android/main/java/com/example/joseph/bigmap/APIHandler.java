package com.example.joseph.bigmap;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

// Interacts with the BigMap server, specifically PHP code made to work with Android
public class APIHandler {
    public static String URLHead = "http://jathweatt.com/BigMap/";
    public static String signIn = "SignIn.php";

    public APIHandler() {}

    public static Boolean signInSuccessful() {
        String url = URLHead + signIn;
        String source = null;
        try {
            source = IOUtils.toString(new URL(url), Charset.forName("UTF-8"));
        } catch (IOException e) {

        }
    }
}
