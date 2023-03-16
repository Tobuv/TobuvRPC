package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.netty.server.NettyServer;
import com.tobuv.rpc.registry.DefaultServiceRegistry;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.serializer.HessianSerializer;

/**
 * 测试：服务提供者
 */
public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(helloService);
        NettyServer nettyServer = new NettyServer();
        nettyServer.setSerializer(new HessianSerializer());
        nettyServer.start(9999);
    }

}