<?php
    /**********************************************************************************
     * This class will store all static functions required to handle user information,
     * including login status and preventing duplicate information
     *********************************************************************************/
     error_reporting(E_ALL); // uncomment for debugging
    /********************************* USER CREDENTIALS ******************************/
    if(!isset($con)) {
        $con = mysqli_connect("localhost", "db_friend", "dolTAP3B");
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

    function getUsername($userId) {
        global $con;
        mysqli_select_db($con, "bm_members");
        $query = "SELECT username FROM `user_info` WHERE id = " . $userId;
        return getFromTable($query)['username'];
    }

    function getUserId($userInfo) {
        global $con;
        mysqli_select_db($con, "bm_members");
        $query = "SELECT id FROM user_info WHERE username = \""
            . $userInfo[0] . "\" AND password = \"" . $userInfo[1] . "\"";
        return mysqli_query($con, $query)->fetch_array()[0];
    }

    //returns true if the username isn't found in the db
    function userNotFound() {
        global $con, $userInfo;
        $query = "SELECT * FROM user_info";
        $users = mysqli_query($con, $query);
    
        if ($userInfo[0] == "") {
            echo "Please enter a username";
            return false;
        } else if ($userInfo[1] == "") {
            echo "Please enter a password";
            return false;
        } else {
            foreach ($users as $user) {
                if ($userInfo[0] == $user["username"]) {
                    echo "This user already exists";
                    return false;
                }
            }
        }
        return true;
    }

    /**************************************** CHANNELS *******************************/
    function channelExists($channelId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        
        $value = "EXISTS(SELECT 1 FROM channel_info WHERE id = " . $channelId . ")";
        $query = "SELECT " . $value;
        $channelExists = getFromTable($query);
        if ($channelExists[$value] !=0) {
            return true;
        } else {
            return false;
        }
    }

    function getChannelMembers($channelId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");

        $query = "SELECT user_id FROM `channels_broadcasting` WHERE channel_id = "
            . $channelId;
        $object = mysqli_query($con, $query);

        $userArray = [];
        foreach ($object as $userId) {
            $userArray[] = $userId["user_id"];
        }
        return $userArray;
    }

    // return true if the user needs a broadcasting profile
    function newBroadcaster($userId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT COUNT(*) FROM broadcast_member WHERE broadcaster_id = "
            . $userId;
        $result = getFromTable($query);
        if ($result["COUNT(*)"] == 0) {
            return true;
        } else {
            return false;
        }
    }

    // true if a user is already in a channel
    function alreadyJoined($userId, $channelId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT COUNT(*) FROM channels_broadcasting WHERE user_id = "
            . $userId . " AND channel_id = " . $channelId;
        $result = getFromTable($query);
        return $result["COUNT(*)"]; // returns 1 or 0
    }

    // read whether the user is broadcasting, then return boolean
    function isUserBroadcasting($userId, $channelId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT is_broadcasting FROM channels_broadcasting WHERE user_id = "
            . $userId . " AND channel_id = " . $channelId;
        $result = getFromTable($query);
        return $result["is_broadcasting"];
    }

    // set whether the user is broadcasting 
    // @param broadcastBoolean: must either put one or zero (false counts as "something")
    function setUserBroadcasting($userId, $channelId, $broadcastBoolean) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "UPDATE channels_broadcasting SET is_broadcasting = "
            . $broadcastBoolean . " WHERE user_id = " . $userId
            . " AND channel_id = " . $channelId;
        mysqli_query($con, $query);
    }

    // returns ALL the channels that the user is affiliated with
    function getUsersChannels($userId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT channel_id FROM `channels_broadcasting` WHERE user_id = " . $userId;
        $object = mysqli_query($con, $query);

        $result = array();
        while ($row = mysqli_fetch_row($object)) {
            array_push($result, $row[0]);
        }
        return $result;
    }

    // returns only the channel id's that the user is broadcasting to
    function getUsersBroadcastingChannels($userId) {
        global $con;
        checkConnection();
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT channel_id FROM `channels_broadcasting` WHERE user_id = " 
            . $userId . " AND is_broadcasting = 1";
        $object = mysqli_query($con, $query);

        $result = array();
        while ($row = mysqli_fetch_row($object)) {
            array_push($result, $row[0]);
        }
        return $result;
    }

    // other utils

    // called when a php script needs to die. This offers the user to return home
    function dieNice($msg) {
        echo $msg;
        echo "<a href=\"../index.html\" class=\"button\">Return to Home</a>";
        exit;
    }

    // takes connection and query to return an array of something
    function getFromTable($query) {
        global $con;
        $object = mysqli_query($con, $query)
            or die ("Error submitting query message: " . $query);
        return mysqli_fetch_assoc($object);
    }

    function checkConnection() {
        global $con;
        return !isset($con)
            ? $con = mysqli_connect("localhost", "db_friend", "dolTAP3B")
            : true;
    }