<!DOCTYPE html>
<html>
<head>
    <title>User Sign up</title>
</head>
<body>
    <?php
        // error_reporting(E_ALL); // uncomment for debugging
        $con = mysqli_connect("localhost", "username", "password");

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
    
        // returns user id
        function userExists($userInfo) {
            global $con;
            mysqli_select_db($con, "bm_members");
    
            $query = "SELECT * FROM user_info WHERE username = \"" .$userInfo[0] . "\"";
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
    
        function getNewChannelId() {
            global $con;
            mysqli_select_db($con, "bm_metadata");
            $query = "SELECT ChannelCount FROM static_variables ROW LIMIT 1";
            $row = getFromTable($query);
    
            return $row["ChannelCount"] + 1;
        }
    
        // takes connection and query to return an array of something
        function getFromTable($query) {
            global $con;
            $object = mysqli_query($con, $query)
                or die ("Error submitting query message: " . $query);
            return mysqli_fetch_assoc($object);
        }
    ?>
</body>
</html>