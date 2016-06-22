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
        static public $userArray = array(); // array of ALL users: id => User

        public function __construct($id, ConnectionInterface $conn) {
            $this->id = $id;
            $this->conn = $conn;
        }
    }

    class BrowserUser extends User {
        public $USER_TYPE = "BROWSER";
        public $channelId;

        public function __construct($id, $channelId, $conn) {
            parent::__construct($id, $conn);
            $this->channelId = $channelId;
        }

        /**
         * @param Channel $channel - passes in the channel object that the user is looking at
         */
        public function sendChannelData($channel) {
            $locationInfo = "";
            $memberIds = $channel->getAndroidIds();
            foreach ($memberIds as $id) {
                 $locationInfo .= $id . " " . $channel->getAndroidUser($id)->current_lat . " " 
                                 . $channel->getAndroidUser($id)->current_long . " "
                                 . $channel->getAndroidUser($id)->is_broadcasting . " ";
            }
            $this->conn->send($locationInfo);
        }
    }

    class AndroidUser extends User {
        public $USER_TYPE = "ANDROID";
        public $channelIds = array();
        public $current_lat;
        public $current_long;
        public $is_broadcasting;

        /**
         * AndroidUser constructor.
         * @param int $id - user id #
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
