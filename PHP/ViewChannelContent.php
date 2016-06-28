<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="initial-scale=1.0">
        <style>
            html, body {
                height: 100%;
                margin: 0;
                padding: 0;
            }
            #map {
                height: 30%;
                padding: 5%;
            }
        </style>
        <!-- saved from url=(0014)about:internet -->
        <!-- saved from url=(0016)http://localhost -->

        <!-- use PHP vars to help execute GoogleMapHelper.js -->
        <?php
            include 'MemberUtilities.php';
            $con = mysqli_connect("localhost", "db_friend", "dolTAP3B");
            isset($_COOKIE["name"]) ? $userInfo[0] = $_COOKIE["name"] : dieNice("no user id specified");
            isset($_COOKIE["password"])? $userInfo[1]= $_COOKIE["password"] : dieNice("no user id specified");
            isset($_POST["channelId"])? $channelId= $_POST["channelId"] : dieNice("channel id not specified");

            userExists($userInfo) ? $userId = getUserId($userInfo) : dieNice("user does not exist");
            if (!alreadyJoined($userId, $channelId)) {
                dieNice("You are not signed into this channel");
            } else {
                $membersArray = getChannelMembers($channelId);
                $membersJSON = json_encode($membersArray);
            }
        ?>
        <script type="text/javascript">
            var channelId = "<?= $channelId ?>", userId = "<?= $userId ?>";
					document.title = "Channel " + channelId;
            var usersLocations; /* will be array of userLocation class */
            var mapScope;
        </script>
        <script type="text/javascript" src="../js/GoogleMapHelper.js"></script>
    </head>
    <!-- userInfo will be used passed to the socket's open function to ensure they are allowed into the channel -->
    <body>
        <!-- Deploy Google map -->
        <div id="map"></div>
        <div id="cant_connect" style="visibility: hidden; color: red">Can't connect to channel</div>
        <script async defer
            src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB9EWuO31iw4rHYdHHs4d5aC_F6UEmoyx0&callback=initMap">
	    </script>
        <!-- show a table of the members and their locations (server side)-->
        <?php
        // begin creating the page ($con currently points to bm_channel)
        echo "<h1>You have entered Channel " . $channelId . "</h1>";

        foreach($membersArray as $memberId) {
            // set user's location history
            echo "<fieldset style=\"display: inline-block\">"
                . "<legend>" . getUsername($memberId) . "</legend>";
             echo "<table id=\"$memberId\">";
                echo "<tr><td><p style='color: red'>Not Broaddcasting</p></td></tr>";
   	     echo "</table>";
            echo "</fieldset>";
        }
        ?>
    </body>
</html>
