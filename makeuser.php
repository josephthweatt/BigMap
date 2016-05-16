<!DOCTYPE html>
<html>
	<head>
		<title>User Sign up</title>
	</head>
	<body>
		<?php 
			$userInfoTable = "user_info";
			$metaInfoTable = "static_variables";
			$newMembers = 0;
			
			$con = mysqli_connect("localhost", "root") or die(mysqli_error($con));

			mysqli_select_db($con, "bm_metadata") or die(mysqli_error($con));
			$query = "SELECT MemberCount from " . $metaInfoTable . " ROW LIMIT 1";
			$result = mysqli_query($con, $query);
			$row = mysqli_fetch_assoc($result);
			$currentMembers = $row["MemberCount"];
			$newMembers = $currentMembers + 1;

			mysqli_select_db($con, "bm_members") or die(mysqli_error($con));
			isset($_POST["signup"]) ? $userInfo = $_POST["signup"] : die("signup not set");
			if (newUserValid()) {
				$query = "INSERT INTO " . $userInfoTable . " VALUES (" . $newMembers
					. ", \"" . $userInfo[0] . "\", \"" . $userInfo[1] . "\")";
				mysqli_query($con, $query) or die(mysqli_error($con));

				// if the insert is successful, we increment member count in metadata
				mysqli_select_db($con, "bm_metadata"); // back to metadata
				$query = "UPDATE " . $metaInfoTable . " SET MemberCount = " . $newMembers;
				mysqli_query($con, $query) or die(mysqli_error($con));
				echo "User info has been stored";
			}

			//returns true if the username isn't found in the db
			function newUserValid() {
				global $con, $userInfo;
				$query = "SELECT * FROM user_info";
				$users = mysqli_query($con, $query);

				if ($userInfo[0] == "") {
					echo "Please enter a username";
					return false;
				} else if ($userInfo[1] == "") {
					echo "Please enter a password";
					return false;
				} else {
					foreach ($users as $user) {
						if ($userInfo[0] == $user["username"]) {
							echo "This user already exists";
							return false;
						}
					}
				}
				return true;
			}
		?>
	</body>
</html>