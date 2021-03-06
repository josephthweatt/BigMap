<?php
    // error_reporting(E_ALL); // uncomment for debugging
    include '../MemberUtilities.php';

    // this will receive user credentials and a channel id to
    // give the user permission to broadcast
    $con = mysqli_connect("localhost," "<username>", "<password>");

    isset($_POST["name"]) ? $userInfo[0] = $_POST["name"] 
            : dieNice("user info not set 1");
    isset($_POST["password"]) ? $userInfo[1] = $_POST["password"] 
            : dieNice("user info not set");
    isset($_POST["channel-id"]) ? $channelId = $_POST["channel-id"] 
            : dieNice("channel id not specified");

    $id = userExists($userInfo);
    if ($id && channelExists($channelId)) {
        if (alreadyJoined($id, $channelId) == 0) {
            // Add the connection to the member database
            mysqli_select_db($con, "bm_channel");
            $query = "INSERT INTO channels_broadcasting VALUES (" . $id . ", " . $channelId . ", 0)";
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
            echo "Success"; // End of script
        } else {
            echo "already joined";
        }
    } else {
        echo "Channel does not exist";
    }
?>
