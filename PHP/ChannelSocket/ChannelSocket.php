<?php
    include 'ChannelUsers.php';
    include 'Channel.php';
    use Ratchet\MessageComponentInterface;
    use Ratchet\ConnectionInterface;

    // TODO: build socket to manage each channel and distribute locations amongst members

    // TODO: make the socket to send and receive messages to android users

    class ChannelSocket implements MessageComponentInterface {
        protected $clients;
        // channel object array ([channedId] => Channel)
        protected $channels = array();
        // user object arrays
        protected $browserUsers = array();
        protected $androidUsers = array(); // id => AndroidUser 

        public function __construct() {
            $this->clients = new SplObjectStorage();
        }

        // attaches connections as clients of the socket
        public function onOpen(ConnectionInterface $conn) {
            $this->clients->attach($conn);
        }

        /**
         * @param ConnectionInterface $conn - user who sent the message
         * @param string $msg - first word of the message is to
         *                       describe what the user is trying to do
         *      Examples:
         *          "connect-browser [userId] [channelId]" - adds user as a browser
         *          "connect-android [userId] [channelIds..." - adds user as an android user
         *          "update-location-android [lat] [long] [channelIds..."
         *                                                 - update android users location
         *          "get-all-broadcasters [userId] [channelId]"
         *                                                 - send batch of channel
         *                                                 - broadcasters to the user
         *          "STOP_BROADCASTING" - stops location broadcast on all channels
         *
         * @send string - sends out a locationBroadcast to the user.
         *      Examples:
         *          "[userId] [lat] [long]" - send out location to browser
         *          "[userId] 0" - send stopping request to browser
         *          "[userId] [lat] [long] [channelId]" - send location to android
         *          "[userId] 0 [channelId]" - send stopping request to android
         */
        public function onMessage(ConnectionInterface $conn, $msg) {
            // determines what the message hopes to send
            $data = explode(" ", trim($msg));
            switch ($data[0]) {
                case "connect-browser":
                    $browser= new BrowserUser($data[1], $data[2], $conn);
                    if (!$this->channelInstantiated($data[2])) {
                        $this->channels[$data[2]] = new Channel($data[2]);
                    }
                    $this->channels[$data[2]]->addBrowserUser($browser);
                    // TODO: sendChannelBroadcasterLocations will be switched out with the
                    // TODO: 'batch' concept used in get-all-broadcasters. I will need to update
                    // TODO: GoogleMapHelper before that is done, however
                    $this->sendChannelBroadcasterLocations($browser, $data[2]);
                    $this->browserUsers[] = $browser;
                    break;
                case "connect-android":
                    $channelIds = array_slice($data, 2);
                    $user = new AndroidUser($data[1], $channelIds, $conn);
                    foreach ($channelIds as $channelId) {
                        if (!$this->channelInstantiated($channelId)) {
                            $this->channels[$channelId] = new Channel($channelId);
                        }
                        $this->channels[$channelId]->addAndroidUser($user);
                    }	
                    $this->androidUsers[$user->id] = $user;
                    break;
                case "update-location-android":
                    $channelIds = array_slice($data, 3);
                    if ($user = $this->getAndroidUser($conn)) {
                        $user->updateLocation($data[1], $data[2]);
                        $user->is_broadcasting = true;

                        // check if a channel id should be receiving an update--if not, send id and '0'
                        foreach ($user->channelIds as $channelId) {
                            if (in_array($channelId, $channelIds)) {
                                // loop through users, send out the location to browsers
                                foreach ($this->channels[$channelId]->browserUsers as $browser) {
                                    $browser->conn->send($user->id . " " . $user->current_lat
                                        . " " . $user->current_long);
                                }
                                // send location to android users
                                foreach ($this->channels[$channelId]->androidUsers as $android) {
                                    if ($android->id != $user->id) {
                                        $android->conn->send($user->id . " "
                                            . $user->current_lat . " " . $user->current_long . " " . $channelId);
                                    }
                                }
                            } else {
                                foreach ($this->channels[$channelId]->browserUsers as $browser) {
                                    $browser->conn->send($user->id . " 0"); // 0 == not broadcasting
                                }
                                foreach ($this->channels[$channelId]->androidUsers as $android) {
                                    if ($android->id != $user->id) {
                                        $android->conn->send($user->id . " 0 " . $channelId);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "get-all-broadcasters":
                    /**
                     * the batch should be sent in this format:
                     *          broadcaster-batch\n
                     *          [userId] [lat] [long]\n
                     *          [userId] [lat] [long] [status update]\n
                     *          ...
                     */
                    if ($channel = $this->channels[$data[2]]) {
                        $locationBatch = "broadcaster-batch\n ";
                        foreach ($channel->androidUsers as $androidUser) {
                            // if this is not the user requesting the batch ...
                            if ($androidUser->id != $data[1]) {
                                $locationBatch .= $androidUser->id . " " . $androidUser->current_lat
                                    . " " . $androidUser->current_long;
                                if ($androidUser->status) { // check if they have a status set
                                    $locationBatch .= " " . $androidUser->status . "\n ";
                                } else {
                                    $locationBatch .= "\n ";
                                }
                            }
                        }
                        $conn->send($locationBatch);
                    }
                    break;
                case "STOP_BROADCASTING":
                    if ($user = $this->getAndroidUser($conn)) {
                        // all browser users will get the stop message from this user
                        foreach ($user->channelIds as $channelId) {
                            $browsers = $this->channels[$channelId]->browserUsers; // array of browserUsers
                            foreach ($browsers as $browser) {
                                $browser->conn->send($user->id . " 0");
                            }
                            foreach ($this->channels[$channelId]->androidUsers as $android) {
                                if ($android->id != $user->id) {
                                    $android->conn->send($user->id . " 0 " . $channelId);
                                }
                            }
                        }
                    }
                    break;
            }
        }

        public function onClose(ConnectionInterface $conn) {
            $this->deleteUser($conn);
            $this->clients->detach($conn);
        }

        public function onError(ConnectionInterface $conn, \Exception $e) {
            echo "Error: " . $e->getMessage();
        }

        /************** NON-ABSTRACT FUNCTIONS *****************/
        
        /**
         * @param ConnectionInterface $conn
         * @return AndroidUser user - returns user object with 
         *                            matching connection
         * @return false- returns false if there are no
         *                              users with that connection
         */
        private function getAndroidUser(ConnectionInterface $conn) {
            foreach ($this->androidUsers as $user) {
                if ($user->conn === $conn) {
                    return $user;
                }
            }
            return false;
        }

        /**
         * gives the client the saved location info of users in the channel
         * @param AndroidUser|BrowserUser $user - can be any type of user
         * @param int $channelId - channel id number
         */
        // TODO: make sure each type of user gets their special message
        private function sendChannelBroadcasterLocations($user, $channelId) {
            foreach(array_keys($this->channels[$channelId]->androidUsers) as $androidId) {
                $androidUser = $this->androidUsers[$androidId];
                $user->conn->send($androidId ." ". $androidUser->current_lat
                    ." ". $androidUser->current_long);
            }
        }

        // returns true if the channel object exists
        private function channelInstantiated($channelId) {
            return isset($this->channels[$channelId]);
        }

        // return true if user was found and deleted
        public function deleteUser(ConnectionInterface $conn) {
            // try to delete from browserUsers
            foreach ($this->browserUsers as $key => $user) {
                if ($user->conn === $conn) {
                    unset($this->browserUsers[$key]);
                    return true;
                }
            }
            // try to delete from androidUsers
            foreach ($this->androidUsers as $key =>$user) {
                if ($user->conn === $conn) {
                    unset($this->androidUsers[$key]);
                    return true;
                }
            }
            // other user types will go here...
            return false;
        }
    }
