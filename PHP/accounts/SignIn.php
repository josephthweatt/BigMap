<!DOCTYPE html>
    <html>
        <head>
            <title>Sign Into BigMap</title>
        </head>
        <body>
            <?php
                // error_reporting(E_ALL); // uncomment for debugging
                include '../MemberUtilities.php';
                $con = mysqli_connect("localhost", "root") or 
                    die(mysqli_error($con));

                if (isset($_POST["user-info"])) {
                    $userInfo = $_POST["user-info"];
                    if (userExists($userInfo)) {
                        echo "Welcome back, " . $userInfo[0] . "!";
                    }
                }
            ?>
        </body>
</html>
