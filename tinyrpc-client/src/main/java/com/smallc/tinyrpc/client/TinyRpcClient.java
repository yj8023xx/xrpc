package com.smallc.tinyrpc.client;

import com.smallc.tinyrpc.client.stub.impl.ProxyStubFactory;
import com.smallc.tinyrpc.common.annotation.RpcReference;
import com.smallc.tinyrpc.network.transport.Transport;
import com.smallc.tinyrpc.network.transport.TransportClient;
import com.smallc.tinyrpc.client.stub.StubFactory;
import com.smallc.tinyrpc.network.transport.netty.NettyClient;
import com.smallc.tinyrpc.registry.NameService;
import com.smallc.tinyrpc.registry.NameServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.client
 */
public class TinyRpcClient implements BeanPostProcessor, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TinyRpcClient.class);

    private final Map<URI, Transport> clientMap = new ConcurrentHashMap<>();

    private TransportClient client = new NettyClient();

    private final StubFactory stubFactory = new ProxyStubFactory();

    private NameService nameService;

    /**
     * 创建TinyRpc客户端实例
     *
     * @param nameServiceUri 注册中心地址
     */
    public TinyRpcClient(URI nameServiceUri) {
        this.nameService = NameServiceUtil.getNameService(nameServiceUri);
    }


    /**
     * 客户端获取远程服务的引用
     *
     * @param serviceClass 服务的接口类的Class
     * @param <T>          服务接口的类型
     * @return 远程服务引用
     */
    public <T> T getRemoteService(Class<T> serviceClass) {
        T stub = null;
        try {
            URI uri = nameService.getServiceAddress(serviceClass.getCanonicalName());
            Transport transport = clientMap.computeIfAbsent(uri, this::createTransport);
            stub = stubFactory.createStub(transport, serviceClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("get remote service {}.", serviceClass.getCanonicalName());
        return stub;
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
        logger.debug("bean name: {}.", beanName);
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                field.setAccessible(true);
                try {
                    field.set(bean, getRemoteService(field.getType()));
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
