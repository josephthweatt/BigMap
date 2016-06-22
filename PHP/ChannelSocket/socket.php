<?php
    require __DIR__ . '/../../vendor/autoload.php';

    use Ratchet\Server\IoServer;
    use Ratchet\Http\HttpServer;
    use Ratchet\WebSocket\WsServer;

    $loop = \React\EventLoop\Factory::create();
    $channelSocket = new ChannelSocket($loop);

    $server = IoServer::factory(new HttpServer(new WsServer($channelSocket)), 2000);
    $server->loop->addPeriodicTimer(.5, function() {
        global $channelSocket;
        $channelSocket->sendLocationUpdates();
    });
    $server->run();
