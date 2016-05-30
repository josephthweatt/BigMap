<?php
    error_reporting(E_ALL); // uncomment for debugging
    include 'MemberUtilities.php';

    /************************************************************************************
     * This file will receive the locationPacket from mobile applications and store them
     * into the location_history table of the bm_channel database. The location packet
     * includes the time, lat, and long of the user. Lat and long (Coordinates) are
     * represented by doubles, while Time is represented by a long int. Time is taken to
     * be the milliseconds since January 1, 1970, so this will need to be converted to
     * represent real UTC (we're using UTC because we don't have the time to include
     * location-specific features).
     *
     * This file receives 3 types of inputs, in the form of POST:
     *      1.) the user's id:          [userid]
     *      2.) channels broadcasting:  [channelId]...
     *      2.) the locationPacket:     [time, latitude, longitude]...
     ***********************************************************************************/

    $con = mysqli_connect("localhost", "root");

    isset($_POST["userInfo"]) ? $userInfo = $_POST["userInfo"] : die ("no user id specified");
    isset($_POST["channelIds"]) ? $channelIds = $_POST["channelIds"] : die ("channel ids not specified");
    isset($_POST["locationPacket"]) ? $locationPacket = $_POST["locationPacket"] : die ("location Packet not found");

    userExists($userInfo) ? $userId = getUserId($userInfo) : die ();

    foreach ($channelIds as $channelId) {
        if (isAMember($channelId)) {
            addLocationPacket($channelId);
        }
    }

    // set the user's current (last known) location
    $query = "UPDATE broadcast_member SET current_lat = "
        . $locationPacket[1] . ", current_long = " . $locationPacket[2] . " WHERE broadcaster_id = " . $userId;
    mysqli_query($con, $query);
    echo "Locations properly stored"; // end of script

    // TODO: turn broadcast_member into something like last_known_location

    // TODO: Make this so that it can add multiple location instances
    function addLocationPacket($channelId) {
        global $con;
        global $locationPacket;
        global $userId;

        $query = "INSERT INTO location_history VALUES ("
            . $userId . "," . $channelId . "," . $locationPacket[0]
            . "," . $locationPacket[1] . "," . $locationPacket[2] . ")";
        mysqli_query($con, $query);

    }