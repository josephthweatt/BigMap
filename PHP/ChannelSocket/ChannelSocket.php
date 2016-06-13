<?php
    use Ratchet\MessageComponentInterface;
    use Ratchet\ConnectionInterface;

    // TODO: build socket to manage each channel and distribute locations amongst members

    // TODO: make the socket to send and receive messages to android users

    class ChannelSocket implements MessageComponentInterface {
        protected $clients;
        
        // user object arrays
        protected $browserUsers;
        protected $androidUsers;

        public function __construct() {
            $this->clients = \SplObjectStorage;
        }

        // TODO: make a check to see if the client is an android user
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
                    array_push($browserUsers, new BrowserUser($data[1], $data[2], $conn));
                    break;
                case "connect-android":
                    array_push($androidUsers, new AndroidUser($data[1], $data[2], $conn));
                    break;
                case "send-location":
                    // TODO: make this when I work on android integration
                    break;
            }
        }

        public function onClose(ConnectionInterface $conn) {
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

        }
    }