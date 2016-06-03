<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Sign Into BigMap</title>
    </head>
    <body>
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