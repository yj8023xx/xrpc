package com.smallc.tinyrpc.demo.demo02;

import com.smallc.tinyrpc.api.hello.HelloService;
import com.smallc.tinyrpc.common.annotation.RpcReference;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.demo.demo02
 */
public class Client {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-client.xml");
        HelloController controller = applicationContext.getBean(HelloController.class);
        controller.say();
    }

}
