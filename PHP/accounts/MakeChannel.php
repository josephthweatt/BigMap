<!DOCTYPE html>
<html>
<head>
    <title>Make a channel</title>
</head>
<body>
    <?php
        // error_reporting(E_ALL); // uncomment for debugging
        include '../MemberUtilities.php';
        $con = mysqli_connect("localhost", "db_friend", "dolTAP3B");

        mysqli_select_db($con, "bm_members");
        if (isset($_POST["user-info"])) {
            $userInfo = $_POST["user-info"];
            if ($memberId = userExists($userInfo)) {
                // create chanel
                $channelId = getNewChannelId();
                mysqli_select_db($con, "bm_channel");
                $query = "INSERT INTO channel_info VALUES (" . $channelId
                    . ", now(), " . $memberId . ")";
                mysqli_query($con, $query) or die(mysqli_error($con));
    
                // increment the channel count
                mysqli_select_db($con, "bm_metadata");
                $query = "UPDATE static_variables SET ChannelCount = " . $channelId;
                mysqli_query($con, $query);
    
                // add chanel to user's profile
                mysqli_select_db($con, "bm_members");
                $query = "INSERT INTO channels_hosting VALUES (" . $memberId
                    . ", " . $channelId . ")";
                mysqli_query($con, $query) or die(mysqli_error($con));
    
                echo "A new channel has been added to your account";
            }
        } else {
            echo "Invalid input";
        }
    
        function getNewChannelId() {
            global $con;
            mysqli_select_db($con, "bm_metadata");
            $query = "SELECT ChannelCount FROM static_variables ROW LIMIT 1";
            $row = getFromTable($query);
    
            return $row["ChannelCount"] + 1;
        }
    ?>
</body>
</html>