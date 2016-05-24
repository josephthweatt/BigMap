<?php
    /**********************************************************************************
     * This class will store all static functions required to handle user information,
     * including login status and preventing duplicate information
     *********************************************************************************/
    // error_reporting(E_ALL); // uncomment for debugging
    /********************************* USER CREDENTIALS ******************************/
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
        mysqli_select_db($con, "bm_channel");

        $query = "SELECT EXISTS(SELECT 1 FROM channel_info WHERE id = " . $channelId . ")";
        $channelExists = getFromTable($query);
        if ($channelExists !=0) {
            return true;
        } else {
            return false;
        }
    }

    // true if a user is already in a channel
    function alreadyJoined($id, $channelId) {
        global $con;
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT COUNT(*) FROM channels_broadcasting WHERE user_id = "
            . $id . " AND channel_id = " . $channelId;
        $result = getFromTable($query);
        return $result["COUNT(*)"]; // returns 1 or 0
    }

    // return true if the user needs a broadcasting profile
    function newBroadcaster($id) {
        global $con;
        mysqli_select_db($con, "bm_channel");
        $query = "SELECT COUNT(*) FROM broadcast_member WHERE broadcaster_id = "
            . $id;
        $result = getFromTable($query);
        if ($result["COUNT(*)"] == 0) {
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