<!DOCTYPE html>
    <html>
        <head>
            <title>Sign Into BigMap</title>
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
                        echo getUserId($userInfo);
                    } else {
                        echo "failed";
                    }
                }
            ?>
        </body>
</html>
