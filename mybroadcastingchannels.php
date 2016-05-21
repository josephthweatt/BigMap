<?php
    // this file will return the key of any channel the user is currently in
    // if the user is not in any channels, it displays "-1"
    // if the user is nonexistent, it displays "0"
    $con = mysqli_connect("localhost", "root");

    isset($_POST["user-info"]) ? $userInfo = $_POST["user-info"] : die("user info not set");

    if ($id = userExists($userInfo)) {
        $query = "SELECT * FROM channels_broadcasting WHERE broadcasting = " . $id;
        $channelIds = getFromTable($query);
        $idString = "Your broadcasting channels: ";
        foreach ($channelIds as $id) {
            $idString .= $id . " ";
        }
        echo $idString; // final result
    }

    // returns user id
    function userExists($userInfo) {
        global $con;
        mysqli_select_db($con, "bm_members");

        $query = "SELECT * FROM user_info WHERE username = \"" . $userInfo[0] . "\"";
        $userArray = getFromTable($query);
        if ($userArray == null) {
            echo "User does not exist";
            return 0;
        }
        if ($userInfo[1] == $userArray["password"]) {
            return $userArray["id"];
        } else {
            echo "Invalid password";
            return 0;
        }
    }

    // takes connection and query to return an array of something
    function getFromTable($query) {
        global $con;
        $object = mysqli_query($con, $query)
        or die ("Error submitting query message: " . $query);
        return mysqli_fetch_assoc($object);
    }