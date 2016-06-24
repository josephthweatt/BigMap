<!DOCTYPE html>
    <html>
        <head>
            <title>Sign Into BigMap</title>
            <link rel="stylesheet" type="text/css" href="../../page-style.css">
        </head>
        <body>
            <?php
                // error_reporting(E_ALL); // uncomment for debugging
                include '../MemberUtilities.php';
                $con = mysqli_connect("localhost", "db_friend", "dolTAP3B") or 
                    die(mysqli_error($con));

                if (isset($_POST["user-info"])) {
                    $userInfo = $_POST["user-info"];
                    if (userExists($userInfo)) {
                        echo "Welcome back, " . $userInfo[0] . "!";
                        echo "<a href=\"../../homepage.html\" class=\"button\" style=\"width=100px\">Return to Home</a>";
                    }
                }
            ?>
        </body>
</html>
