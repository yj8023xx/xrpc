package com.smallc.xrpc.demo.demo01;

import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.demo.HelloServiceImpl;
import com.smallc.xrpc.server.XRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/21
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.demo3
 */
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        // Check if enough command-line arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java Server <ip-address> <port>");
            System.exit(1); // Exit the program indicating insufficient parameters
        }

        // Retrieve command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        logger.info("Create xRPC server instance...");
        XRpcServer server = new XRpcServer(host, port);

        logger.info("Register service providers...");
        HelloService helloService = new HelloServiceImpl();
        server.addServiceProvider(HelloService.class, helloService);

        logger.info("Start providing services...");
        server.start();
    }

}
