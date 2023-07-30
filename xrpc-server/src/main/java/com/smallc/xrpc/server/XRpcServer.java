package com.smallc.xrpc.server;

import com.smallc.xrpc.common.annotation.RpcService;
import com.smallc.xrpc.common.spi.ServiceLoader;
import com.smallc.xrpc.network.transport.netty.NettyServer;
import com.smallc.xrpc.registry.Registry;
import com.smallc.xrpc.registry.RegistryFactory;
import com.smallc.xrpc.network.transport.TransportServer;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server
 */
public class XRpcServer implements BeanPostProcessor, InitializingBean, DisposableBean {

    private final String host;
    private final int port;
    private final URI uri;
    private TransportServer server = null;
    private final ServiceProviderRegistry serviceProviderRegistry = ServiceLoader.load(ServiceProviderRegistry.class);
    private Registry registry;

    /**
     * 创建xRPC服务端实例
     *
     * @param port            监听端口号
     * @param registryUri  注册中心地址
     */
    public XRpcServer(int port, URI registryUri) {
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.port = port;
        this.uri = URI.create("rpc://" + host + ":" + port);
        this.registry = RegistryFactory.getRegistry(registryUri);
    }

    public XRpcServer(String host, int port, URI nameServiceUri) {
        this.host = host;
        this.port = port;
        this.uri = URI.create("rpc://" + host + ":" + port);
        this.registry = RegistryFactory.getRegistry(nameServiceUri);
    }

    /**
     * 服务端注册服务的实现实例
     *
     * @param service      实现实例
     * @param serviceClass 服务接口类的Class
     * @param <T>          服务接口的类型
     */
    public <T> void addServiceProvider(Class<?> serviceClass, T service) {
        serviceProviderRegistry.addServiceProvider(serviceClass, service);
        try {
            registry.registerService(serviceClass.getCanonicalName(), uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Closeable start() throws Exception {
        if (null == server) {
            server = new NettyServer();
            server.start(host, port, RequestHandlerRegistry.getInstance());
        }
        return () -> {
            if (null != server) {
                server.stop();
            }
        };
    }

    public void stop() {
        if (null != server) {
            server.stop();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Annotation[] annotations = bean.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RpcService) {
                RpcService rpcService = (RpcService) annotation;
                // 注解处理器
                Class<?> clazz = rpcService.value();
                addServiceProvider(clazz, bean);
            }
        }
        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

}
