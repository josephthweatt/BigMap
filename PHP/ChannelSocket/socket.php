<?php
    require __DIR__ . '/../vendor/autoload.php';

    use Ratchet\Server\IoServer;
    use Ratchet\http\HttpServer;
    use Ratchet\WebSocket\WsServer;

    $loop = \React\EventLoop\Factory::create();
    $channelSocket = new ChannelSocket($loop);

    $server = IoServer::factory(new HttpServer(new WsServer($channelSocket)), 2000);
    $server->loop->addPeriodicTimer(1, function() {
        global $channelSocket;
        $channelSocket->sendLocationUpdates();
    });
    $server->run();