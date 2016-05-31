package com.example.joseph.bigmap;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by Joseph on 5/30/2016.
 */
public class LocationServiceTest {

    public static void main(String[] args) {
        System.out.println(timeAsString(new Location()));
    }

    // should return in UTC
    private static String timeAsString (Location location) {
        // code taken from: stackoverflow.com/questions/12747549/android-location-time-into-date
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(location.getTime());
        return format.format(date);
    }
}