<?php
    require __DIR__ . '/../vendor/autoload.php';

    use Ratchet\Server\IoServer;
    use Ratchet\http\HttpServer;
    use Ratchet\WebSocket\WsServer;

    $server = IoServer::factory(new HttpServer(new WsServer(new ChannelSocket)), 2000);
    $server->run();