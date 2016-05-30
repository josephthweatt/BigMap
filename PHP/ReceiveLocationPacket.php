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
     * This file receives 4 types of inputs, in the form of POST:
     *      1.) the user's id:          [userid]
     *      2.) channels broadcasting:  [channelId]...
     *      3.) the locationPackets:    [time, latitude, longitude]...
     *      4.) the number of packets   [packetCount]
     *
     * The names of the LocationPackets will simply be formatted as locationPacket#,
     * where '#' is the number (starting from zero. 0 is the earliest packet)
     ***********************************************************************************/

    $con = mysqli_connect("localhost", "root");

    isset($_POST["userInfo"]) ? $userInfo = $_POST["userInfo"] : die ("no user id specified");
    isset($_POST["channelIds"]) ? $channelIds = $_POST["channelIds"] : die ("channel ids not specified");
    isset($_POST["packetCount"]) ? $packetCount = $_POST["packetCount"] : die ("packet count not specified");

    userExists($userInfo) ? $userId = getUserId($userInfo) : die ();

    foreach ($channelIds as $channelId) {
        if (isAMember($channelId)) {
            addLocationPacket($channelId);
        }
    }

    // set the user's current (last known) location
    $lastLocation = $_POST["locationPacket" . ($packetCount - 1)];
    $query = "UPDATE broadcast_member SET current_lat = "
        . $lastLocation[1] . ", current_long = " . $lastLocation[2] . " WHERE broadcaster_id = " . $userId;
    mysqli_query($con, $query);
    echo "Locations properly stored"; // end of script

    // TODO: turn broadcast_member into something like last_known_location

    function addLocationPacket($channelId) {
        global $con;
        global $userId;
        global $packetCount;

        for ($i = 0; $i < $packetCount; $i++) {
            $query = "INSERT INTO location_history VALUES ("
                . $userId . "," . $channelId . "," . $_POST["locationPacket" . $i][0]
                . "," . $_POST["locationPacket" . $i][1] . "," . $_POST["locationPacket" . $i][2] . ")";
            mysqli_query($con, $query);
        }
    }