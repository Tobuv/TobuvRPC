package com.tobuv.rpc.transport.socket.server;

import com.tobuv.rpc.hook.ShutdownHook;
import com.tobuv.rpc.provider.ServiceProvider;
import com.tobuv.rpc.provider.ServiceProviderImpl;
import com.tobuv.rpc.registry.NacosServiceRegistry;
import com.tobuv.rpc.transport.AbstractRpcServer;
import com.tobuv.rpc.transport.RpcServer;
import com.tobuv.rpc.enumeration.RpcError;
import com.tobuv.rpc.exception.RpcException;
import com.tobuv.rpc.registry.ServiceRegistry;
import com.tobuv.rpc.handler.RequestHandler;
import com.tobuv.rpc.serializer.CommonSerializer;
import com.tobuv.rpc.factory.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 远程方法调用的提供者（服务端）
 */
public class SocketServer extends AbstractRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private final RequestHandler requestHandler = new RequestHandler();
    private final CommonSerializer serializer;
    private final ExecutorService threadPool;;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }
}
