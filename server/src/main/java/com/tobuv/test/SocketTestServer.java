package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.serializer.CommonSerializer;;
import com.tobuv.rpc.transport.socket.server.SocketServer;

/**
 * 测试：服务提供者
 */
public class SocketTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl2();
        SocketServer socketServer = new SocketServer("127.0.0.1", 9998, CommonSerializer.PROTOBUF_SERIALIZER);
        socketServer.publishService(helloService, HelloService.class);
    }

}