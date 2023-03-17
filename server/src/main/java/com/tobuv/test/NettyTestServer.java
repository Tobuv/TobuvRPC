package com.tobuv.test;

import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.transport.netty.server.NettyServer;
import com.tobuv.rpc.provider.ServiceProviderImpl;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.serializer.ProtobufSerializer;

/**
 * 测试：服务提供者
 */
public class NettyTestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer server = new NettyServer("127.0.0.1", 9999);
        server.setSerializer(new ProtobufSerializer());
        server.publishService(helloService, HelloService.class);
    }

}