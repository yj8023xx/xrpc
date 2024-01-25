package com.smallc.xrpc.client;

import com.smallc.xrpc.client.stub.StubFactory;
import com.smallc.xrpc.client.stub.impl.ProxyStubFactory;
import com.smallc.xrpc.common.annotation.RpcReference;
import com.smallc.xrpc.common.loadbalancer.LoadBalanceType;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;
import com.smallc.xrpc.common.loadbalancer.LoadBalancerFactory;
import com.smallc.xrpc.common.serializer.SerializationType;
import com.smallc.xrpc.common.serializer.Serializer;
import com.smallc.xrpc.common.serializer.SerializerFactory;
import com.smallc.xrpc.common.spi.ServiceLoader;
import com.smallc.xrpc.network.transport.Transport;
import com.smallc.xrpc.network.transport.TransportClient;
import com.smallc.xrpc.registry.Registry;
import com.smallc.xrpc.registry.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.client
 */
public class XRpcClient implements BeanPostProcessor, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(XRpcClient.class);

    private final Map<URI, Transport> transportMap = new ConcurrentHashMap<>();

    private TransportClient client = ServiceLoader.load(TransportClient.class);

    private final StubFactory stubFactory = new ProxyStubFactory();

    private Registry registry;

    public XRpcClient() {
    }

    /**
     * 创建xRPC客户端实例
     *
     * @param registryUri 注册中心地址
     */
    public XRpcClient(URI registryUri) {
        this.registry = RegistryFactory.getRegistry(registryUri);
    }

    public <T> T getRemoteService(Class<T> serviceClass, URI serviceUri, SerializationType type) {
        List<Transport> transports = new ArrayList<>();
        Transport transport = transportMap.computeIfAbsent(serviceUri, this::createTransport);
        transports.add(transport);
        LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer(LoadBalanceType.RANDOM, transports);
        Serializer serializer = SerializerFactory.getSerializer(type);
        T stub = stubFactory.createStub(serviceClass, this, loadBalancer, serializer);
        return stub;
    }

    /**
     * 客户端获取远程服务的引用
     *
     * @param serviceClass 服务的接口类的Class
     * @param <T>          服务接口的类型
     * @return 远程服务引用
     */
    public <T> T getRemoteService(Class<T> serviceClass, LoadBalanceType loadBalanceType, SerializationType serializationType) {
        T stub = null;
        try {
            List<URI> uris = registry.getServiceAddress(serviceClass.getCanonicalName());
            List<Transport> transports = new ArrayList<>();
            for (URI uri : uris) {
                Transport transport = transportMap.computeIfAbsent(uri, this::createTransport);
                transports.add(transport);
            }
            LoadBalancer loadBalancer = LoadBalancerFactory.getLoadBalancer(loadBalanceType, transports);
            Serializer serializer = SerializerFactory.getSerializer(serializationType);
            stub = stubFactory.createStub(serviceClass, this, loadBalancer, serializer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.debug("Get remote service {}.", serviceClass.getCanonicalName());
        return stub;
    }

    public <T> T getRemoteService(Class<T> serviceClass) {
        return getRemoteService(serviceClass, LoadBalanceType.RANDOM, SerializationType.JSON);
    }

    public <T> T getRemoteService(Class<T> serviceClass, LoadBalanceType loadBalanceType) {
        return getRemoteService(serviceClass, loadBalanceType, SerializationType.JSON);
    }

    public <T> T getRemoteService(Class<T> serviceClass, SerializationType serializationType) {
        return getRemoteService(serviceClass, LoadBalanceType.RANDOM, serializationType);
    }

    private Transport createTransport(URI uri) {
        try {
            return client.createTransport(new InetSocketAddress(uri.getHost(), uri.getPort()), 30000L);
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        logger.debug("Process bean name: {}.", beanName);
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                field.setAccessible(true);
                try {
                    field.set(bean, getRemoteService(field.getType(),
                            LoadBalanceType.getType(rpcReference.loadbalance()),
                            SerializationType.getType(rpcReference.serialize())));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        client.close();
    }

}
