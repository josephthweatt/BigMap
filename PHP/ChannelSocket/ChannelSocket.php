<?php
    include 'ChannelUsers.php';
    use Ratchet\MessageComponentInterface;
    use Ratchet\ConnectionInterface;

    // TODO: build socket to manage each channel and distribute locations amongst members

    // TODO: make the socket to send and receive messages to android users

    class ChannelSocket implements MessageComponentInterface {
        protected $clients;
        private $loop;
        // channel object array ([channedId] => Channel)
        protected $channels = array(); // TODO: find a way to instant. channels
        // user object arrays
        protected $browserUsers = array();
        protected $androidUsers = array();

        public function __construct(\React\EventLoop\LoopInterface $loop) {
            $this->clients = new SplObjectStorage();
            $this->loop = $loop;
        }

        // attaches connections as clients of the socket
        public function onOpen(ConnectionInterface $conn) {
            $this->clients->attach($conn);
        }

        /**
         * @param ConnectionInterface $conn - user who sent the message
         * @param string $msg - first word of the message is to d
         *                       describe what the user is trying to do
         *      Examples:
         *          "connect-browser [userId] [channelId]" - adds user as a browser
         *          "connect-android [userId] [channelIds..." - adds user as an android user
         *          "update-location-android [lat] [long] [channelIds..."
         *                                                 - update android users location
         *          "STOP_BROADCASTING"
         */
        public function onMessage(ConnectionInterface $conn, $msg) {
            // determines what the message hopes to send
            $data = explode(" ", trim($msg));
            switch ($data[0]) {
                case "connect-browser":
                    if (!$this->channelInstantiated($data[2])) {
                        $this->channels[$data[2]] = new Channel($data[2]);
                    }
                    $this->browserUsers[] = new BrowserUser($data[1], $data[2], $conn);
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
                    $this->androidUsers[] = $user;
                    break;
                case "update-location-android":
                    $channelIds = array_slice($data, 3);
                    $user = $this->getAndroidUser($conn);
                    $user->updateLocation($data[1], $channelIds);
                    $user->is_broadcasting = true;

                    // check if a channel id should be receiving an update
                    foreach ($user->channelIds as $channelId) {
                        if (in_array($channelId, $channelIds)) {
                            // overwrite assoc array of android users with this current user
                            $this->channels[$channelId]->addAndroidUser($user);
                        } else {
                            $this->channels[$channelId]->getAndroidUser($user->id)->is_broadcasting = false;
                        }
                    }
                    break;
                case "STOP_BROADCASTING":
                    $user = $this->getAndroidUser($conn);
                    $user->is_broadcasting = false;
                    foreach ($user->channelIds as $channelId) {
                        $this->channels[$channelId]->getAndroidUser($user->id)->is_broadcasting = false;
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

        // returns true if the channel object exists
        private function channelInstantiated($channelId) {
            return isset($this->channels[$channelId]);
        }

        /**********************************************************
         * Instead of sending out location updates immediately,
         * this socket will wait a second before sending out
         * location updates to channels. This way the server will
         * not have to react to every momentary location change.
         **********************************************************/
        public function sendLocationUpdates() {
            /*
             * NOTE: This is all temporary code. It will be replaced
             *       once android users are able to join the socket.
             *       For now, the locations will be taken out of the
             *       mySQL database, which is what we've been doing
             */
            foreach ($this->browserUsers as $user) {
                $user->sendChannelData();
            }
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