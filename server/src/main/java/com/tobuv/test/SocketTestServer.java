package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.provider.ServiceProviderImpl;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.serializer.KryoSerializer;
import com.tobuv.rpc.transport.socket.server.SocketServer;

/**
 * 测试：服务提供者
 */
public class SocketTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketServer socketServer = new SocketServer("127.0.0.1", 9998);
        socketServer.setSerializer(new KryoSerializer());
        socketServer.publishService(helloService, HelloService.class);
    }

}