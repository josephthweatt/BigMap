<!DOCTYPE html>
<html>
	<head>
		<title>User Sign up</title>
	<link rel="stylesheet" type="text/css" href="../../page-style.css">
	</head>
	<body>
		<?php 
			// error_reporting(E_ALL); // uncomment for debugging
			include '../MemberUtilities.php';
		
			$userInfoTable = "user_info";
			$metaInfoTable = "static_variables";
			$newMembers = 0;

			$con = mysqli_connect("localhost," "<username>", "<password>")
				or die(mysqli_error($con));

			mysqli_select_db($con, "bm_metadata") or die (mysqli_error($con));
			$query = "SELECT MemberCount from " . $metaInfoTable . " ROW LIMIT 1";
			$result = mysqli_query($con, $query);
			$row = mysqli_fetch_assoc($result);
			$currentMembers = $row["MemberCount"];
			$newMembers = $currentMembers + 1;

			$con->select_db("bm_members") or die(mysqli_error($con));

			isset($_POST["signup"]) ? $userInfo = $_POST["signup"] : dieNice("signup not set");
			if (userNotFound()) {
				$query = "INSERT INTO " . $userInfoTable . " VALUES (" . $newMembers
					. ", \"" . $userInfo[0] . "\", \"" . $userInfo[1] . "\")";
				mysqli_query($con, $query) or die(mysqli_error($con));

				// if the insert is successful, we increment member count in metadata
				mysqli_select_db($con, "bm_metadata"); // back to metadata
				$query = "UPDATE " . $metaInfoTable . " SET MemberCount = " . $newMembers;
				mysqli_query($con, $query) or die(mysqli_error($con));
				echo "User info has been stored";
			}
			echo "<a href=\"../../index.html\" class=\"button\">Return to Home</a>";
		?>
	</body>
</html>
