package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.server.RpcServer;

/**
 * 测试：服务提供者
 */
public class TestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9000);
    }

}