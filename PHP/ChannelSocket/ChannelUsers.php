<?php
    // required for all user classes
    abstract class User {
        public $id;
        protected $channelIds = array();
        protected $conn;

        static public $connArray = array(); // array of ALL connections

        public function __construct($id, $channelIds, $conn) {
            $this->id = $id;
            if (is_int($channelIds)) {
                array_push($this->channelIds, $channelIds);
            } else {
                $this->channelIds = $channelIds;
            }
            $this->conn = $conn;
            
            $connArray[] = $conn;
        }
    }

    class BrowserUser extends User {
        public $USER_TYPE = "BROWSER";

        public function __construct($id, $channelIds, $conn) {
            parent::__construct($id, $channelIds, $conn);
        }
    }

    // TODO: add functions to this class to assist in the
    // TODO: retrieval of their locations if possible, I will try
    // TODO: to get android users from databasing their information altogether
    class AndroidUser extends User {
        public $USER_TYPE = "ANDROID";
        protected $current_lat;
        protected $current_long;
        // TODO: add map for channelIds: [channelId] [isBroadcasting]

        public function __construct($id, $channelIds, $conn) {
            parent::__construct($id, $channelIds, $conn);
        }
    }