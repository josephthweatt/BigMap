<?php
    require __DIR__ . '/../vendor/autoload.php';

    use Ratchet\Server\IoServer;
    use Ratchet\http\HttpServer;
    use Ratchet\WebSocket\WsServer;

    $loop = \React\EventLoop\Factory::create();    

    $server = IoServer::factory(new HttpServer(new WsServer(new ChannelSocket($loop))), 2000);
    $server->run();