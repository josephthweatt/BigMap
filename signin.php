<!DOCTYPE html>
    <html>
        <head>
            <title>Sign Into BigMap</title>
        </head>
        <body>
            <?php
                $con = mysqli_connect("localhost", "username", "password") or 
                    die(mysqli_error($con));

                if (isset($_POST["user-info"])) {
                    $userInfo = $_POST["user-info"];
                    if (userExists($userInfo)) {
                        echo "Welcome back, " . $userInfo[0] . "!";
                    }
                }

                // returns user id
                function userExists($userInfo) {
                    global $con;
                    mysqli_select_db($con, "bm_members");

                    $query = "SELECT * FROM user_info WHERE username = \"" . $userInfo[0] . "\"";
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
