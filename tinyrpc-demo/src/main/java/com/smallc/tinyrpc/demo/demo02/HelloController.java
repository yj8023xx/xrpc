package com.smallc.tinyrpc.demo.demo02;

import com.smallc.tinyrpc.api.hello.HelloService;
import com.smallc.tinyrpc.common.annotation.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.demo.demo02
 */
@Component
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    @RpcReference(loadbalance = "random", serialize = "hessian")
    private HelloService helloService;

    public void say() {
        String name = "World!";
        logger.info("请求服务, name: {}...", name);
        String response = helloService.hello(name);
        logger.info("收到响应: {}.", response);
    }

}
