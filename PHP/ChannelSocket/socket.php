<?php
    require __DIR__ . '/../../vendor/autoload.php';

    use Ratchet\Server\IoServer;
    use Ratchet\Http\HttpServer;
    use Ratchet\WebSocket\WsServer;

    $channelSocket = new ChannelSocket($loop);

    $server = IoServer::factory(new HttpServer(new WsServer($channelSocket)), 2000);
    $server->run();
