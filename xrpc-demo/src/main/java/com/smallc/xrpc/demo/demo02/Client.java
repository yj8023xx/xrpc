package com.smallc.xrpc.demo.demo02;

import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.common.serializer.SerializationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.demo01
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        // Check if enough command-line arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java Client <registry-ip-address> <registry-port>");
            System.exit(1); // Exit the program indicating insufficient parameters
        }

        // Retrieve command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        URI registryUri = URI.create("nacos://" + host + ":" + port);

        logger.info("Create xRPC client instance...");
        XRpcClient client = new XRpcClient(registryUri);

        logger.info("Create service stub...");
        HelloService helloService = client.getRemoteService(HelloService.class, SerializationType.JSON);
        assert helloService != null;

        String name = "World!";
        logger.info("Request: name: {}", name);
        String response = helloService.hello(name);
        logger.info("Response: {}.", response);
    }

}
