package com.smallc.xrpc.server;

import com.smallc.xrpc.common.annotation.RpcService;
import com.smallc.xrpc.common.spi.ServiceLoader;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import com.smallc.xrpc.network.transport.TransportServer;
import com.smallc.xrpc.registry.Registry;
import com.smallc.xrpc.registry.RegistryFactory;
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
 * @since com.smallc.xrpc.network.transport.TransportServer.server
 */
public class XRpcServer implements BeanPostProcessor, InitializingBean, DisposableBean {

    private final String host;
    private final int port;
    private final URI uri;
    private TransportServer server = null;
    private final ServiceProviderRegistry serviceProviderRegistry = ServiceLoader.load(ServiceProviderRegistry.class);
    private Registry registry = null;

    public XRpcServer(int port) {
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.port = port;
        this.uri = URI.create("rpc://" + host + ":" + port);
    }

    public XRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.uri = URI.create("rpc://" + host + ":" + port);
    }

    /**
     * Create xRPC server instance
     *
     * @param port        listen port
     * @param registryUri registry address
     */
    public XRpcServer(int port, URI registryUri) {
        this(port);
        this.registry = RegistryFactory.getRegistry(registryUri);
    }

    public XRpcServer(String host, int port, URI registryUri) {
        this(host, port);
        this.registry = RegistryFactory.getRegistry(registryUri);
    }

    /**
     * Register service implement
     *
     * @param service      service implement
     * @param serviceClass Class of service interface
     * @param <T>          Type of service interface
     */
    public <T> void addServiceProvider(Class<?> serviceClass, T service) {
        serviceProviderRegistry.addServiceProvider(serviceClass, service);
        try {
            if (registry != null) {
                registry.registerService(serviceClass.getCanonicalName(), uri);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Closeable start() throws Exception {
        if (null == server) {
            server = ServiceLoader.load(TransportServer.class);
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
                // annotation processor
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
