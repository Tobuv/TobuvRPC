package com.tobuv.rpc.transport.socket.client;

import com.tobuv.rpc.registry.NacosServiceDiscovery;
import com.tobuv.rpc.registry.NacosServiceRegistry;
import com.tobuv.rpc.registry.ServiceDiscovery;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.transport.RpcClient;
import com.tobuv.rpc.entity.RpcRequest;
import com.tobuv.rpc.entity.RpcResponse;
import com.tobuv.rpc.enumeration.ResponseCode;
import com.tobuv.rpc.enumeration.RpcError;
import com.tobuv.rpc.exception.RpcException;
import com.tobuv.rpc.serializer.CommonSerializer;
import com.tobuv.rpc.transport.socket.util.ObjectReader;
import com.tobuv.rpc.transport.socket.util.ObjectWriter;
import com.tobuv.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 远程方法调用的消费者（客户端）
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private final CommonSerializer serializer;
    private final ServiceDiscovery serviceDiscovery;

    public SocketClient() {
        this(DEFAULT_SERIALIZER);
    }

    public SocketClient(Integer serializer) {
        this.serviceDiscovery = new NacosServiceDiscovery();
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
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
            //检查请求号是否一致
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse;
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            //return null;
            throw new RpcException("服务调用失败: ", e);
        }
    }

}