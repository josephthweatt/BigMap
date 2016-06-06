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
        <title>Sign Into BigMap</title>
    </head>
    <body>
        <!-- Deploy Google map -->
        <div id="map"></div>
        <script>
            var map;
            function initMap() {
                map = new google.maps.Map(document.getElementById('map'), {
                    center: {lat: -34.397, lng: 150.644},
                    zoom: 8
                });
            }
        </script>
        <script
            src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB9EWuO31iw4rHYdHHs4d5aC_F6UEmoyx0&callback=initMap"
            async defer></script>

        <!-- show a table of the members and a list of their locations (server side)-->
        <?php
        // error_reporting(E_ALL); // uncomment for debugging
        include 'MemberUtilities.php';

        $con = mysqli_connect("localhost", "root");

        isset($_POST["userInfo"]) ? $userInfo = $_POST["userInfo"] : die ("no user id specified");
        isset($_POST["channelId"]) ? $channelId = $_POST["channelId"] : die ("channel ids not specified");

        userExists($userInfo) ? $userId = getUserId($userInfo) : die ();
        if (alreadyJoined($userId, $channelId)) {
            // begin creating the page ($con currently points to bm_channel)
            echo "<h1>You have entered Channel " . $channelId . "</h1>";

            $channelMembers = getChannelMembers($channelId);
            foreach($channelMembers as $memberId) {
                // set user's location history
                echo "<fieldset style=\"display: inline-block\"><legend>" . getUsername($memberId) . "</legend>";
                 echo "<table>";

                $locationHistory = getLocationHistory($memberId, $channelId);
                foreach ($locationHistory as $location) {
                    echo "<tr>";
                    echo "<td><p>" . $location['time'] . "</p></td>";
                    echo "<td><p>" . $location['latitude'] . "</p></td>";
                    echo "<td><p>" . $location['longitude'] . "</p></td>";
                    echo "</tr>";
                }
                 echo "</table>";
                echo "</fieldset>";
            }
        } else {
            echo "<h1>You are not signed into this channel</h1>";
        }
        ?>
    </body>
</html>