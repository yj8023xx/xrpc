package com.smallc.tinyrpc.demo;

import com.smallc.tinyrpc.api.hello.HelloService;
import com.smallc.tinyrpc.common.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.demo
 */
@RpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("HelloServiceImpl收到: {}.", name);
        String ret = "Hello " + name;
        logger.info("HelloServiceImpl返回: {}.", ret);
        return ret;
    }

}
