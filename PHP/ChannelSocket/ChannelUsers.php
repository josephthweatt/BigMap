<?php
    include '../MemberUtilities.php';
    use Ratchet\ConnectionInterface;

if (!isset($con)) {
    $con = mysqli_connect("localhost", "db_friend", "dolTAP3B");
}

    // required for all user classes
    abstract class User {
        public $id;
        public $channelId;
        public $conn;
        static public $connArray = array(); // array of ALL connections

        public function __construct($id, $channelId, ConnectionInterface $conn) {
            $this->id = $id;
            $this->channelId = $channelId;
            $this->conn = $conn;
            $connArray[] = $conn;
        }

        function getCurrentLat($userId) {
            global $con;
            mysqli_select_db($con, "bm_channel");
            $query = "SELECT current_lat FROM broadcast_member WHERE broadcaster_id = " . $userId;
            return mysqli_fetch_assoc(mysqli_query($con, $query))["current_lat"];
        }

        function getCurrentLong($userId) {
            global $con;
            mysqli_select_db($con, "bm_channel");
            $query = "SELECT current_long FROM broadcast_member WHERE broadcaster_id = " . $userId;
            return mysqli_fetch_assoc(mysqli_query($con, $query))["current_long"];
        }
    }

    class BrowserUser extends User {
        public $USER_TYPE = "BROWSER";

        public function __construct($id, $channelId, $conn) {
            parent::__construct($id, $channelId, $conn);
        }
        
        public function sendChannelData() {
            $locationInfo = "";
            $memberIds = getChannelMembers($this->channelId);
            foreach ($memberIds as $id) {
                 $locationInfo .= $id . " " . $this->getCurrentLat($id) . " " 
                                 . $this->getCurrentLong($id) . " " 
                                 . isUserBroadcasting($id, $this->channelId) . " ";
            }
            $this->conn->send($locationInfo);
        }
    }

    // TODO: add functions to this class to assist in the
    // TODO: retrieval of their locations if possible, I will try
    // TODO: to get android users from databasing their information altogether
    class AndroidUser extends User {
        public $USER_TYPE = "ANDROID";
        protected $current_lat;
        protected $current_long;
        protected $is_broadcasting;

        public function __construct($id, $channelId, $conn) {
            parent::__construct($id, $channelId, $conn);
        }
    }