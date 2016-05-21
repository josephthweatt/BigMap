<?php
    // error_reporting(E_ALL); // uncomment for debugging
    include 'MemberUtilities.php';

    // this file will return the key of any channel the user is currently in
    // if the user is not in any channels, it displays "-1"
    // if the user is nonexistent, it displays "0"
    $con = mysqli_connect("localhost", "root");

    isset($_POST["user-info"]) ? $userInfo = $_POST["user-info"] : die("user info not set");

    $id = userExists($userInfo);
    if ($id != 0 && !newBroadcaster($id)) {
        $query = "SELECT * FROM channels_broadcasting WHERE broadcasting = " . $id;
        $channelIds = getFromTable($query);
        $idString = "Your broadcasting channels: ";
        foreach ($channelIds as $id) {
            $idString .= $id . " ";
        }
        echo $idString; // final result
    } else {
        echo "You have not registered to any channels";
    }