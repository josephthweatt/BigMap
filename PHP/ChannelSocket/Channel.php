<?php
    class Channel {
        public $channelId;
        public $androidUsers = array(); // [userId] => AndroidUser

        public function __construct($channelId) {
            $this->channelId = $channelId;
        }

        public function addAndroidUser(AndroidUser $user) {
            $this->androidUsers[$user->id] = $user;
        }

        /**
         * @param $userId
         * @return AndroidUser - Android user if the user exists--null otherwise
         */
        public function getAndroidUser($userId) {
            if (isset($this->androidUsers[$userId])) {
                return $this->androidUsers[$userId];
            } else {
                return null;
            }
        }

        // returns android user ids as an array
        public function getAndroidIds() {
            return array_keys($this->androidUsers);
        }
    }