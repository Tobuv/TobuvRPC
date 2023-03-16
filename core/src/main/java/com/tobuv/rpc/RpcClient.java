package com.tobuv.rpc;

import com.tobuv.rpc.entity.RpcRequest;

/**
 * 客户端类通用接口
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);

}