package com.tobuv.rpc;

import com.tobuv.rpc.entity.RpcRequest;
import com.tobuv.rpc.serializer.CommonSerializer;

/**
 * 客户端类通用接口
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);
    void setSerializer(CommonSerializer serializer);

}