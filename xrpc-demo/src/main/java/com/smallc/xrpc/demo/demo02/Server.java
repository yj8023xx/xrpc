package com.smallc.xrpc.demo.demo02;

import com.smallc.xrpc.demo.HelloServiceImpl;
import com.smallc.xrpc.server.XRpcServer;
import com.smallc.xrpc.api.hello.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.demo01
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        // Check if enough command-line arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java Server <registry-ip-address> <registry-port>");
            System.exit(1); // Exit the program indicating insufficient parameters
        }

        // Retrieve command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        URI registryUri = URI.create("nacos://" + host + ":" + port);

        logger.info("Create xRPC server instance...");
        XRpcServer server = new XRpcServer(8090, registryUri);

        logger.info("Register service providers...");
        HelloService helloService = new HelloServiceImpl();
        server.addServiceProvider(HelloService.class, helloService);

        logger.info("Start providing services...");
        server.start();
    }

}
