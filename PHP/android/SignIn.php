<?php
    // error_reporting(E_ALL); // uncomment for debugging
    include '../MemberUtilities.php';
    $con = mysqli_connect("localhost," "<username>", "<password>") or 
        die(mysqli_error($con));

    if (isset($_POST["user-info"])) {
        $userInfo = $_POST["user-info"];
        if (userExists($userInfo)) {
            echo getUserId($userInfo) . PHP_EOL;
        } else {
            echo "failed";
        }
    }
?>
