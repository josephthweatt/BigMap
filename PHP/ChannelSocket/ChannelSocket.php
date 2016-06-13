<?php
    use Ratchet\MessageComponentInterface;
    use Ratchet\ConnectionInterface;

    // TODO: build socket to manage each channel and distribute locations amongst members

    class ChannelSocket implements MessageComponentInterface {
        protected $clients;

        public function __construct() {
            $this->clients = \SplObjectStorage;
        }

        // attaches connections as clients of the socket
        public function onOpen(ConnectionInterface $conn) {
            $this->clients->attach($conn);
        }

        public function onMessage(ConnectionInterface $conn, $msg) {
            foreach ($this->clients as $client) {
                if ($client !== $conn) {
                    $client->send($msg);
                }
            }
        }

        public function onClose(ConnectionInterface $conn) {
            $this->clients->detach($conn);
        }

        public function onError(ConnectionInterface $conn, \Exception $e) {
            echo "Error: " . $e->getMessage();
        }
    }
