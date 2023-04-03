package com.tobuv.test;

import com.tobuv.rpc.annotation.ServiceScan;
import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.serializer.CommonSerializer;
import com.tobuv.rpc.transport.RpcServer;
import com.tobuv.rpc.transport.netty.server.NettyServer;

/**
 * 测试：服务提供者
 */
@ServiceScan
public class NettyTestServer {

    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999);
        server.start();
    }

}