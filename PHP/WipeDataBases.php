<!DOCTYPE html>
<html>
    <head>
        <title>Databases Wiped</title>
    </head>
    <body>
        <?php
            // error_reporting(E_ALL); // uncomment for debugging
            $con = mysqli_connect("localhost", "root");

            function truncateTable($tableName) {
                global $con;
                $query = "TRUNCATE TABLE " . $tableName;
                mysqli_query($con, $query);
            }

            // wipe metadata
            mysqli_select_db($con, "bm_metadata"); // back to metadata
            $query = "UPDATE static_variables SET MemberCount = 0, ChannelCount = 0";
            mysqli_query($con, $query) or die(mysqli_error($con));

            // wipe member data
            mysqli_select_db($con, "bm_members");
            truncateTable("user_info");
            truncateTable("channels_hosting");

            // wipe channel data
            mysqli_select_db($con, "bm_channel");
            truncateTable("location_history");
            truncateTable("channels_broadcasting");
            truncateTable("channel_info");
            truncateTable("broadcast_member");

            echo "<h1>All databases have been wiped</h1>";
        ?>
    </body>
</html>
