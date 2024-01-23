# xRPC

xRPC is a lightweight, high-throughput, low-latency RPC framework that provides ultra-low-latency RPC (5~10us) for applications supporting RDMA (Remote Direct Memory Access) network interfaces. This framework seamlessly integrates with the Spring framework, allowing existing Spring applications to integrate xRPC effortlessly, thereby achieving more efficient and faster RPC communication, further enhancing application performance and user experience.



## Architecture

![registry](./img/registry.svg)



## Features

- Interface-oriented programming for easy extensibility.
- Incorporates various design patterns such as Singleton, Factory Method, Flyweight, and Proxy.
- Supports automatic service registration and discovery.
- Configurable serialization protocols, registry centers, and load balancing strategies.
- Supports cross-language communication (currently supports Java and Go communication).
- Seamless integration with the Spring framework.
- Supports RDMA communication protocol, achieving extremely low transmission latency, and transparent to upper-layer applications.

[What is RDMA?](https://www.fibermall.com/blog/what-is-rdma.htm)



## Modules

The framework is divided into 5 modules based on different roles: client, server, network, registry, and common.

|    Module     |                           Function                           |                         Description                          |
| :-----------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|  xrpc-client  |    Mainly used to create xRPC client instances and stubs     | Currently supports creating stubs using JDK dynamic proxy, with other creation methods to be added in the future. |
|  xrpc-server  | Mainly used to create xRPC server instances and design request handlers |                              -                               |
| xrpc-network  | Responsible for network transmission, submitting requests to different request handlers | Currently supports RDMA and Netty network transmission methods. |
| xrpc-registry |     Used for registering and obtaining service instances     | Currently supports ZooKeeper and Nacos as registry centers.  |
|  xrpc-common  | Common interfaces, including serialization protocols, load balancing strategies, SPI mechanism, etc. | Currently supports serialization methods such as Hessian, JSON, and Protostuff, and load balancing strategies such as Random, RoundRobin, and IPHash. |



## Examples

**Manual Programming Approach**

```java
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        logger.info("Create xRPC server instance...");
        XRpcServer server = new XRpcServer("127.0.0.1", 8090);

        logger.info("Register service providers...");
        HelloService helloService = new HelloServiceImpl();
        server.addServiceProvider(HelloService.class, helloService);

        logger.info("Start providing services...");
        server.start();
    }

}

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        logger.info("Create xRPC client instance...");
        XRpcClient client = new XRpcClient();

        String host = "127.0.0.1";
        int port = 8090;
        URI serviceUri = URI.create("rpc://" + host + ":" + port);

        logger.info("Create service stub...");
        HelloService helloService = client.getRemoteService(HelloService.class, serviceUri, SerializationType.JSON);
        assert helloService != null;

        String name = "World!";
        logger.info("Request: name: {}", name);
        String response = helloService.hello(name);
        logger.info("Response: {}.", response);
    }

}
```

**Integration with Spring**

```java
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-server.xml");
    }

}

public class Client {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-client.xml");
        HelloController controller = applicationContext.getBean(HelloController.class);
        controller.say();
    }

}

@Component
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    @RpcReference(loadbalance = "roundrobin", serialize = "protostuff")
    private HelloService helloService;

    public void say() {
        String name = "World!";
        logger.info("Request: name: {}", name);
        String response = helloService.hello(name);
        logger.info("Response: {}.", response);
    }

}
```



## Testing

**Configuration**

- RDMA NIC: ConnectX-3
- CPU: Intel(R) Xeon(R) Gold 6230 CPU @ 2.10GHz
- OS: CentOS Linux 7
- GCC: 10.1.0

![rdma_rpc_latency](./img/rdma_rpc_latency.svg)



## Support

Support for other languages in this project:

- **[xrpc-go](https://github.com/yj8023xx/xrpc-go)**
