package com.tobuv.test;

import com.tobuv.rpc.transport.RpcClientProxy;
import com.tobuv.rpc.api.HelloObject;
import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.transport.netty.client.NettyClient;
import com.tobuv.rpc.serializer.ProtobufSerializer;


/**
 * 测试：服务消费者
 */
public class NettyTestClient {

    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.setSerializer(new ProtobufSerializer());
        RpcClientProxy proxy = new RpcClientProxy(nettyClient);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }

}