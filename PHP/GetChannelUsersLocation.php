<?php
    error_reporting(E_ALL); // uncomment for debugging
    include 'MemberUtilities.php';
    $con = mysqli_connect("localhost", "root");

    isset($_POST["channelId"]) ? $channelId = $_POST["channelId"] : die ("channel id not set");
    isset($_POST["membersId"]) ? $membersId = $_POST["membersId"] : die ("no member ids were found");

    /* the response ought to return an array of the current user's locations
     * with this text structure:
     *      [userId] [current lat] [current long]\n
     *      [userId] [current lat] [current long]\n...
     */
    foreach ($membersId as $id) {
        // 'if' checks against JS injection
        if (alreadyJoined($id, $channelId)) {
            echo $id . " " . getCurrentLat($id) . " " . getCurrentLong($id) . PHP_EOL;
        }
    } // end of script

    function getCurrentLat($userId) {
        global $con;
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT current_lat FROM broadcast_member WHERE broadcaster_id = " . $userId;
        return mysqli_fetch_assoc(mysqli_query($con, $query))["current_lat"];
    }

    function getCurrentLong($userId) {
        global $con;
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT current_long FROM broadcast_member WHERE broadcaster_id = " . $userId;
        return mysqli_fetch_assoc(mysqli_query($con, $query))["current_long"];
    }