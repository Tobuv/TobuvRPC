package com.tobuv.rpc.socket.client;

import com.tobuv.rpc.RpcClient;
import com.tobuv.rpc.entity.RpcRequest;
import com.tobuv.rpc.entity.RpcResponse;
import com.tobuv.rpc.enumeration.ResponseCode;
import com.tobuv.rpc.enumeration.RpcError;
import com.tobuv.rpc.exception.RpcException;
import com.tobuv.rpc.serializer.CommonSerializer;
import com.tobuv.rpc.socket.util.ObjectReader;
import com.tobuv.rpc.socket.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 远程方法调用的消费者（客户端）
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private CommonSerializer serializer;

    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (Socket socket = new Socket(host, port)) {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            if(rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            return rpcResponse.getData();
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            //return null;
            throw new RpcException("服务调用失败: ", e);
        }
    }
    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

}