package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.registry.DefaultServiceRegistry;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.socket.server.SocketServer;

/**
 * 测试：服务提供者
 */
public class SocketTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(helloService);
        SocketServer socketServer = new SocketServer(registry);
        socketServer.start(9000);
    }

}