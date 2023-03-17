package com.tobuv.rpc.transport;

import com.tobuv.rpc.serializer.CommonSerializer;

/**
 * 服务器类通用接口
 */
public interface RpcServer {

    void start();
    void setSerializer(CommonSerializer serializer);
    <T> void publishService(T service, Class<T> serviceClass);

}