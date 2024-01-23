package com.smallc.xrpc.demo.demo01;

import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.common.serializer.SerializationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/21
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.demo3
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        // Check if enough command-line arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java Client <ip-address> <port>");
            System.exit(1); // Exit the program indicating insufficient parameters
        }

        // Retrieve command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        URI serviceUri = URI.create("rpc://" + host + ":" + port);

        logger.info("Create xRPC client instance...");
        XRpcClient client = new XRpcClient();

        logger.info("Create service stub...");
        HelloService helloService = client.getRemoteService(HelloService.class, serviceUri, SerializationType.JSON);
        assert helloService != null;

        String name = "World!";
        logger.info("Request: name: {}", name);
        String response = helloService.hello(name);
        logger.info("Response: {}.", response);
    }

}
