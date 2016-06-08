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
        <!-- saved from url=(0014)about:internet -->
        <!-- saved from url=(0016)http://localhost -->

        <!-- use PHP vars to help execute GoogleMapHelper.js -->
        <?php
            include 'MemberUtilities.php';
            $con = mysqli_connect("localhost", "root");
            $channelId = isset($_POST["channelId"]) ? $_POST["channelId"] : die ("no user id specified");
            $membersArray = getChannelMembers($channelId);
            $membersJSON = json_encode($membersArray);
        ?>
        <script type="text/javascript" src="../js/AJAXUtils.js"></script>
        <script type="text/javascript">var channelId = "<?= $channelId ?>";</script>
        <script type="text/javascript">var membersIdArray= addEscapes(<?= $membersJSON ?>);</script>
        <script type="text/javascript">var usersLocations; /* will be array of userLocation class */</script>
        <script type="text/javascript">var mapScope;</script>
        <script type="text/javascript" src="../js/GoogleMapHelper.js"></script>
    </head>
    <body onload="getUsersLocationForMap()">
        <!-- Deploy Google map -->
        <div id="map"></div>
        <script>
            var map;
            var purpleDot = '../Images/purple-dot.png'; // default marker for user's location
            function initMap() {
                map = new google.maps.Map(document.getElementById('map'), {
                    center: {lat: mapScope["center"][0], lng: mapScope["center"][1]},
                    zoom: 8 // TODO: create a function to find how far to zoom out (enough to show every user)
                });

                // get user's location markers
                var userMarkers = [];
                for (var user in usersLocations) {
                    userMarkers = new google.maps.Marker({
                                        position : {lat : parseFloat(usersLocations[user].lat),
                                                    lng : parseFloat(usersLocations[user].long)},
                                        map : map,
                                        icon : purpleDot
                                    });
                }
            }
        </script>
        <script
            src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB9EWuO31iw4rHYdHHs4d5aC_F6UEmoyx0&callback=initMap"
            async defer></script>

        <!-- show a table of the members and a list of their locations (server side)-->
        <?php
        // error_reporting(E_ALL); // uncomment for debugging
        $con = mysqli_connect("localhost", "root");

        isset($_POST["userInfo"]) ? $userInfo = $_POST["userInfo"] : die ("no user id specified");
        isset($_POST["channelId"]) ? $channelId = $_POST["channelId"] : die ("channel ids not specified");

        userExists($userInfo) ? $userId = getUserId($userInfo) : die ();
        if (alreadyJoined($userId, $channelId)) {
            // begin creating the page ($con currently points to bm_channel)
            echo "<h1>You have entered Channel " . $channelId . "</h1>";

            foreach($membersArray as $memberId) {
                // set user's location history
                echo "<fieldset style=\"display: inline-block\"><legend>" . getUsername($memberId["user_id"]) . "</legend>";
                 echo "<table>";

                $locationHistory = getLocationHistory($memberId["user_id"], $channelId);
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