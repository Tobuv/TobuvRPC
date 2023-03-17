package com.tobuv.rpc.transport;

import com.tobuv.rpc.entity.RpcRequest;
import com.tobuv.rpc.serializer.CommonSerializer;

/**
 * 客户端类通用接口
 */
public interface RpcClient {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    Object sendRequest(RpcRequest rpcRequest);

}