package com.smallc.xrpc.server.handler;

import com.smallc.xrpc.common.annotation.RpcSingleton;
import com.smallc.xrpc.common.serializer.SerializationType;
import com.smallc.xrpc.common.serializer.SerializationUtil;
import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.protocol.XRpcMessageType;
import com.smallc.xrpc.network.protocol.XRpcStatus;
import com.smallc.xrpc.network.protocol.XRpcRequest;
import com.smallc.xrpc.network.transport.RequestHandler;
import com.smallc.xrpc.server.ServiceProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.handler
 */
@RpcSingleton
public class RpcRequestHandler implements RequestHandler<XRpcMessage>, ServiceProviderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    /**
     * Map
     * String 服务名
     * Object 服务提供者
     */
    private Map<String, Object> serviceProviders = new ConcurrentHashMap<>();

    @Override
    public XRpcMessage handle(XRpcMessage message) {
        XRpcHeader header = message.getHeader();
        // 从payload中反序列化RpcRequest
        SerializationType type = SerializationType.getType(header.getSerializationId());
        XRpcRequest XRpcRequest = SerializationUtil.deserialize(message.getPayload(), XRpcRequest.class, type);
        try {
            // 查找所有已注册的服务提供方，寻找rpcRequest中需要的服务
            Object serviceProvider = serviceProviders.get(XRpcRequest.getServiceName());
            logger.info("Get interfaceName: {} methodName: {}.", XRpcRequest.getServiceName(), XRpcRequest.getMethodName());
            if (serviceProvider != null) {
                // 找到服务提供者，利用Java反射机制调用服务的对应方法
                String methodName = XRpcRequest.getMethodName();
                Map<String, Object> argMap = XRpcRequest.getArgMap();
                Object[] args = argMap.values().toArray();
                Class<?>[] parameterTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                }
                Method method = serviceProvider.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceProvider, args);
                // 把结果封装成响应命令并返回
                return new XRpcMessage(new XRpcHeader((byte) XRpcMessageType.RPC_RESPONSE.getValue(), header.getSerializationId(), header.getRequestId(), (byte) XRpcStatus.SUCCESS.getValue()), SerializationUtil.serialize(result, type));
            }
            // 如果没找到，返回NO_PROVIDER错误响应
            logger.warn("No service provider of {}#{}(String)!", XRpcRequest.getServiceName(), XRpcRequest.getMethodName());
            return new XRpcMessage(new XRpcHeader((byte) XRpcMessageType.RPC_RESPONSE.getValue(), header.getSerializationId(), header.getRequestId(), (byte) XRpcStatus.NO_PROVIDER.getValue()), new byte[0]);
        } catch (Throwable t) {
            // 发生异常，返回UNKNOWN_ERROR错误响应
            logger.warn("Exception: ", t);
            return new XRpcMessage(new XRpcHeader((byte) XRpcMessageType.RPC_RESPONSE.getValue(), header.getSerializationId(), header.getRequestId(), (byte) XRpcStatus.UNKNOWN_ERROR.getValue()), new byte[0]);
        }
    }

    @Override
    public int type() {
        return XRpcMessageType.RPC_REQUEST.getValue();
    }

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        logger.info("Add service: {}, provider: {}.",
                serviceClass.getCanonicalName(),
                serviceProvider.getClass().getCanonicalName());
    }

    @Override
    public <T> void removeServiceProvider(Class<? extends T> serviceClass) {
        serviceProviders.remove(serviceClass.getCanonicalName());
    }

}
