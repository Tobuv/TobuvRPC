# Tobuv-RPC

这是一个基于netty + nacos + kyro(hessian...)实现的轻量级rpc框架。业界知名的dubbo，grpc，motan等，大家只停留在对框架的使用，这里实现一个轻量级rpc框架用于加深自己对rpc的理解，同时推动自己去读源码去学习。

## 一个基础的rpc框架

<img src="C:\Users\chengsongren\AppData\Roaming\Typora\typora-user-images\image-20230810155218119.png" alt="image-20230810155218119" style="zoom:50%;" />

一个基础的rpc框架由三部分组成：注册中心，服务端，消费端

- 注册中心：用来保存服务的信息
- 服务端：提供服务的一端，即提供者provider
- 服务端：消费鼓舞的一端，即消费者consumer

流程：

- 服务端将服务的信息注册到注册中心，通常包括服务的地址、全类名、方法
- 客服端从注册中心获取服务的信息
- 客服端根据获得的信息，通过网络传输调用服务

## 设计

一个基本的 RPC 框架，需要包含以下部分：

1. **注册中心**：注册中心负责服务信息的注册与查找。服务端在启动的时候，扫描所有的服务，然后将自己的服务地址和服务名注册到注册中心。客户端在调用服务之前，通过注册中心查找到服务的地址，就可以通过服务的地址调用到服务啦。常见的注册中心有 `Zookeeper`、`Eureka`、`nacos` 等。
2. **动态代理**：客户端调用接口，需要框架能自己根据接口去远程调用服务，这一步是用户无感知的。这样一来，就需要使用到动态代理，用户调用接口，实际上是在调用动态生成的代理类。常见的动态代理有：`JDK Proxy`，`CGLib`，`Javassist` 等。
3. **网络传输**：RPC 远程调用实际上就是网络传输，所以网络传输是 RPC 框架中必不可少的部分。网络框架有 `Java NIO`、`Netty` 框架等。
4. **自定义协议**：网络传输需要制定好协议，一个良好的协议能提高传输的效率。
5. **序列化**：网络传输肯定会涉及到序列化，常见的序列化有`Json`、`Protostuff`、`Kyro` 等。
6. **负载均衡**：当请求调用量大的时候，需要增加服务端的数量，一旦增加，就会涉及到符合选择服务的问题，这就是负载均衡。常见的负载均衡策略有：轮询、随机、加权轮询、加权随机、一致性哈希等等。
7. **集群容错**：当请求服务异常的时候，我们是应该直接报错呢？还是重试？还是请求其他服务？这个就是集群容错策略啦。

## bio实现网络传输

### 先定义公共的接口HelloService

```java
public interface HelloService {

    String hello(HelloObject object);

}
```

```java
@Data
@AllArgsConstructor
public class HelloObject implements Serializable {

    private Integer id;
    private String message;

}
```

接着我们在服务端对这个接口进行实现，实现的方式也很简单，返回一个字符串就行：

```java
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：{}", object.getMessage());
        return "这是掉用的返回值，id=" + object.getId();
    }
}

```

### 传输协议

传输协议就是定义请求和响应的格式，如客服端要发送调用请求那么应该包括：接口名称、调用的方法、参数、参数类型。具体定义如下

```java
@Data
@Builder
public class RpcRequest implements Serializable {
    /**
     * 待调用接口名称
     */
    private String interfaceName;
    /**
     * 待调用方法名称
     */
    private String methodName;
    /**
     * 调用方法的参数
     */
    private Object[] parameters;
    /**
     * 调用方法的参数类型
     */
    private Class<?>[] paramTypes;
}

```

响应包的个数如下：

```java
@Data
public class RpcResponse<T> implements Serializable {
    /**
     * 响应状态码
     */
    private Integer statusCode;
    /**
     * 响应状态补充信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;
  
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }
    public static <T> RpcResponse<T> fail(ResponseCode code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
```

### 客户端的实现——动态代理
客户端方面，由于在客户端这一侧我们并没有接口的具体实现类，就没有办法直接生成实例对象。这时，我们可以通过**动态代理的方式生成实例**，并且调用方法时生成需要的RpcRequest对象并且发送给服务端。

这里我们采用JDK动态代理，代理类是需要实现InvocationHandler接口的。

```java
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }
}

```

我们需要传递host和port来指明服务端的位置。并且使用getProxy()方法来生成代理对象。

InvocationHandler接口需要实现invoke()方法，来指明代理对象的方法被调用时的动作。在这里，我们显然就需要生成一个RpcRequest对象，发送出去，然后返回从服务端接收到的结果即可：

```java
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse) rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
```



生成RpcRequest很简单，我使用Builder模式来生成这个对象。发送的逻辑我使用了一个RpcClient对象来实现，这个对象的作用，就是将一个对象发过去，并且接受返回的对象。

```java
public class RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRequest(RpcRequest rpcRequest, String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用时有错误发生：", e);
            return null;
        }
    }
}

```

我的实现很简单，直接使用Java的序列化方式，通过Socket传输。创建一个Socket，获取ObjectOutputStream对象，然后把需要发送的对象传进去即可，接收时获取ObjectInputStream对象，readObject()方法就可以获得一个返回的对象。

### 服务端的实现——反射调用
服务端的实现就简单多了，使用一个ServerSocket监听某个端口，循环接收连接请求，如果发来了请求就创建一个线程，在新线程中处理调用。这里创建线程采用线程池：

```java
public class RpcServer {

    private final ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer() {
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workingQueue, threadFactory);
    }
}
```

这里简化了一下，RpcServer暂时只能注册一个接口，即对外提供一个接口的调用服务，添加register方法，在注册完一个服务后立刻开始监听：

```java
    public void register(Object service, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器正在启动...");
            Socket socket;
            while((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！Ip为：" + socket.getInetAddress());
                threadPool.execute(new WorkerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("连接时有错误发生：", e);
        }
    }

```



这里向工作线程WorkerThread传入了socket和用于服务端实例service。

WorkerThread实现了Runnable接口，用于接收RpcRequest对象，解析并且调用，生成RpcResponse对象并传输回去。run方法如下：

```java
public class WorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    private Socket socket;
    private Object service;

    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }

}
```

其中，通过class.getMethod方法，传入方法名和方法参数类型即可获得Method对象。如果你上面RpcRequest中使用String数组来存储方法参数类型的话，这里你就需要通过反射生成对应的Class数组了。通过method.invoke方法，传入对象实例和参数，即可调用并且获得返回值。

测试
服务端侧，我们已经在上面实现了一个HelloService的实现类HelloServiceImpl的实现类了，我们只需要创建一个RpcServer并且把这个实现类注册进去就行了：

```java
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9000);
    }
}
```

服务端开放在9000端口。

客户端方面，我们需要通过动态代理，生成代理对象，并且调用，动态代理会自动帮我们向服务端发送请求的：

```java
public class TestClient {
    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
```

