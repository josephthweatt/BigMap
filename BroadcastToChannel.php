<?php
    // this will receive user credentials and a channel id to
    // give the user permission to broadcast
    $con = mysqli_connect("localhost", "username", "password");

    isset($_POST["user-info"]) ? $userInfo = $_POST["user-info"] : die("user info not set");
    isset($_POST["channel-id"]) ? $channelId = $_POST["channel-id"] : die("channel id not specified");

    if ($id = userExists($userInfo) && channelExists($channelId)) {
        
    }

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

    function channelExists($channelId) {
        global $con;
        mysqli_select_db($con, "bm_channel");

        $query = "SELECT EXISTS(SELECT 1 FROM channel_info WHERE id = " . $channelId . ")";
        $channelExists = getFromTable($query);
        if ($channelExists[0] !=0) {
            return true;
        } else {
            return false;
        }
    }

    // takes connection and query to return an array of something
    function getFromTable($query) {
        global $con;
        $object = mysqli_query($con, $query)
        or die ("Error submitting query message: " . $query);
        return mysqli_fetch_assoc($object);
    }