package com.smallc.xrpc.demo.demo01;

import com.smallc.xrpc.demo.HelloServiceImpl;
import com.smallc.xrpc.server.XRpcServer;
import com.smallc.xrpc.api.hello.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.demo.demo01
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 2181;
        URI registryUri = URI.create("zookeeper://" + host + ":" + port);

        logger.info("创建xRPC服务端实例...");
        XRpcServer server = new XRpcServer(8090, registryUri);

        logger.info("创建服务提供者...");
        HelloService helloService = new HelloServiceImpl();

        logger.info("向RPC框架注册服务提供者...");
        server.addServiceProvider(HelloService.class, helloService);

        logger.info("开始提供服务...");
        server.start();
    }

}
