package com.tobuv.test;

import com.tobuv.rpc.serializer.CommonSerializer;
import com.tobuv.rpc.transport.RpcClientProxy;
import com.tobuv.rpc.api.HelloObject;
import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.serializer.KryoSerializer;
import com.tobuv.rpc.transport.socket.client.SocketClient;


/**
 * 测试：服务消费者
 */
public class SocketTestClient {

    public static void main(String[] args) {
        SocketClient client = new SocketClient(CommonSerializer.PROTOBUF_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }

}