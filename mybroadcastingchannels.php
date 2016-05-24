<?php
    // error_reporting(E_ALL); // uncomment for debugging
    include 'MemberUtilities.php';

    // this file will return the key of any channel the user is currently in
    // if the user is not in any channels, it displays "-1"
    // if the user is nonexistent, it displays "0"
    $con = mysqli_connect("localhost", "root");

    isset($_POST["user-info"]) ? $userInfo = $_POST["user-info"] : die("user info not set");

    $id = userExists($userInfo);
    if ($id != 0 && !newBroadcaster($id)) { // bm_channel selected here
        $query = "SELECT channel_id FROM channels_broadcasting WHERE user_id = " . $id;
        $queryObject = mysqli_query($con, $query);

        $idString = "Your broadcasting channels: ";
        while ($channel = mysqli_fetch_assoc($queryObject)) {
            $idString .= $channel["channel_id"] . " ";
        }
        echo $idString; // final result
    } else {
        echo "You have not registered to any channels";
    }