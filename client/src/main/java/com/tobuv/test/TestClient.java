package com.tobuv.test;

import com.tobuv.rpc.api.HelloObject;
import com.tobuv.rpc.api.HelloService;
import com.tobuv.rpc.client.RpcClientProxy;

/**
 * 测试：服务消费者
 */
public class TestClient {

    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }

}