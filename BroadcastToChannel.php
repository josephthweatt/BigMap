<?php
    // this will receive user credentials and a channel id to
    // give the user permission to broadcast
    $con = mysqli_connect("localhost", "root");

    isset($_POST["user-info"]) ? $userInfo = $_POST["user-info"] : die("user info not set");
    isset($_POST["channel-id"]) ? $channelId = $_POST["channel-id"] : die("channel id not specified");

    $id = userExists($userInfo);
    if ($id && channelExists($channelId)) {
        if (alreadyJoined($id, $channelId) == 0) {
            // Add the connection to the member database
            mysqli_select_db($con, "bm_channel");
            $query = "INSERT INTO channels_broadcasting VALUES (" . $id . ", " . $channelId . ")";
            mysqli_query($con, $query);

            /**************************************************************************************
             *  create the broadcast_member and location_history for the new broadcaster
             *  we will only need to insert the values of the member ids. Other values set to null
             **************************************************************************************/
            if (newBroadcaster($id)) {
                $query = "INSERT INTO broadcast_member VALUES (" . $id . ", null, null)";
                mysqli_query($con, $query);
                $query = "INSERT INTO location_history VALUES (" . $id . ", null, null, null)";
                mysqli_query($con, $query);
            }
            echo $userInfo[0] . " has joined channel " . $channelId; // End of script
        } else {
            echo "User has already joined";
        }
    }

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
        if ($channelExists !=0) {
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