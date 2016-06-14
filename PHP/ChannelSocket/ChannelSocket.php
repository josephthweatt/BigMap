<?php
    include 'ChannelUsers.php';
    use Ratchet\MessageComponentInterface;
    use Ratchet\ConnectionInterface;

    // TODO: build socket to manage each channel and distribute locations amongst members

    // TODO: make the socket to send and receive messages to android users

    class ChannelSocket implements MessageComponentInterface {
        protected $clients;
        private $loop;
        // user object arrays
        protected $browserUsers = array();
        protected $androidUsers = array();

        public function __construct(\React\EventLoop\LoopInterface $loop) {
            $this->clients = new SplObjectStorage();
            $this->loop = $loop;

            $this->loop->addPeriodicTimer(1, function() {
                    $this->sendLocationUpdates();
                });
        }

        // attaches connections as clients of the socket
        public function onOpen(ConnectionInterface $conn) {
            $this->clients->attach($conn);
        }

        /*
         * @param {String} msg - first word of the message is to d
         *                       describe what the user is trying to do
         *      Examples:
         *          "connect-browser [userId] [channelId]" - adds user as a browser
         *          "connect-android [userId] [channelId]" - adds user as an android user
         *
         */
        public function onMessage(ConnectionInterface $conn, $msg) {
            // determines what the message hopes to send
            $data = explode(" ", trim($msg));
            switch ($data[0]) {
                case "connect-browser":
                    $this->browserUsers[] = new BrowserUser($data[1], $data[2], $conn);
                    break;
                case "connect-android":
                    array_push($this->androidUsers, new AndroidUser($data[1], $data[2], $conn));
                    break;
                case "send-location":
                    // TODO: make this when I work on android integration
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
            foreach ($this->browserUsers as $user) {
                if ($user->conn === $conn) {
                    unset($this->browserUsers[$user]);
                    return true;
                }
            }
            // try to delete from androidUsers
            foreach ($this->androidUsers as $user) {
                if ($user->conn === $conn) {
                    unset($this->androidUsers[$user]);
                    return true;
                }
            }
            // other user types will go here...
            return false;
        }
    }