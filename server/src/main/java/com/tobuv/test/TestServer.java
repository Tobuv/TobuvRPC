package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.registry.DefaultServiceRegistry;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.server.RpcServer;

/**
 * 测试：服务提供者
 */
public class TestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(helloService);
        RpcServer rpcServer = new RpcServer(registry);
        rpcServer.start(9000);
    }

}