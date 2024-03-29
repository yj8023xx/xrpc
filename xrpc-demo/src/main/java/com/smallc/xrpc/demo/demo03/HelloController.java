package com.smallc.xrpc.demo.demo03;

import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.common.annotation.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.demo02
 */
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
