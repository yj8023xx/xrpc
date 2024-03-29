package com.smallc.xrpc.demo;

import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.common.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.TransportServer.demo
 */
@RpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("HelloServiceImpl receive: {}.", name);
        String ret = "Hello " + name;
        logger.info("HelloServiceImpl return: {}.", ret);
        return ret;
    }

}
