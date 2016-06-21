<?php
    include_once 'MemberUtilities.php';
    use Ratchet\ConnectionInterface;

if (!isset($con)) {
    $con = mysqli_connect("localhost", "db_friend", "dolTAP3B");
}

    // required for all user classes
    abstract class User {
        public $id;
        public $conn;
        static public $connArray = array(); // array of ALL connections

        public function __construct($id, ConnectionInterface $conn) {
            $this->id = $id;
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
        public $channelId;

        public function __construct($id, $channelId, $conn) {
            parent::__construct($id, $conn);
            $this->channelId = $channelId;
        }

        // TODO: a channel should only need to extract members from MySQL once. Channel should be made an object
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
        public $channelIds = array();
        protected $current_lat;
        protected $current_long;
        public $is_broadcasting;

        /**
         * AndroidUser constructor.
         * @param $id - user id #
         * @param array $channelIds - an array of the users affiliated channels
         * @param ConnectionInterface $conn
         */
        public function __construct($id, $channelIds, ConnectionInterface $conn) {
            parent::__construct($id, $conn);
            $this->channelIds = $channelIds;
        }

        public function updateLocation($currentLat, $currentLong) {
            $this->current_lat = $currentLat;
            $this->current_long = $currentLong;
            $this->is_broadcasting = true;
        }
    }
