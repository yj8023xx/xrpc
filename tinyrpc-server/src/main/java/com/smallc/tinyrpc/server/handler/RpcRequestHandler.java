package com.smallc.tinyrpc.server.handler;

import com.smallc.tinyrpc.common.annotation.RpcSingleton;
import com.smallc.tinyrpc.common.serializer.ProtostuffSerializerUtil;
import com.smallc.tinyrpc.common.handler.RequestHandler;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.network.protocol.*;
import com.smallc.tinyrpc.common.bean.RpcRequest;
import com.smallc.tinyrpc.common.serializer.SerializeUtil;
import com.smallc.tinyrpc.server.ServiceProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.server.handler
 */
@RpcSingleton
public class RpcRequestHandler implements RequestHandler<TinyRpcPacket>, ServiceProviderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    /**
     * Map
     * String 服务名
     * Object 服务提供者
     */
    private Map<String, Object> serviceProviders = new ConcurrentHashMap<>();

    @Override
    public TinyRpcPacket handle(TinyRpcPacket packet) {
        TinyRpcHeader header = packet.getHeader();
        // 从payload中反序列化RpcRequest
        String serializeType = SerializeType.messageOf(header.getSerializeType());
        RpcRequest rpcRequest = SerializeUtil.deserialize(packet.getPayload(), RpcRequest.class, serializeType);
        try {
            // 查找所有已注册的服务提供方，寻找rpcRequest中需要的服务
            Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());
            logger.info("interfaceName: {} methodName: {}.", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            if (serviceProvider != null) {
                // 找到服务提供者，利用Java反射机制调用服务的对应方法
                String methodName = rpcRequest.getMethodName();
                Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
                Object[] parameters = rpcRequest.getParameters();
                Method method = serviceProvider.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceProvider, parameters);
                // 把结果封装成响应命令并返回
                return new TinyRpcPacket(new TinyRpcResponseHeader(TinyRpcMessageType.RPC_RESPONSE.getCode(), header.getSerializeType(), header.getRequestId()), SerializeUtil.serialize(result, serializeType));
            }
            // 如果没找到，返回NO_PROVIDER错误响应
            logger.warn("No service provider of {}#{}(String)!", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            return new TinyRpcPacket(new TinyRpcResponseHeader(TinyRpcMessageType.RPC_RESPONSE.getCode(), header.getSerializeType(), header.getRequestId(), TinyRpcCode.NO_PROVIDER.getCode(), "No Provider!"), new byte[0]);
        } catch (Throwable t) {
            // 发生异常，返回UNKNOWN_ERROR错误响应
            logger.warn("Exception: ", t);
            return new TinyRpcPacket(new TinyRpcResponseHeader(TinyRpcMessageType.RPC_RESPONSE.getCode(), header.getSerializeType(), header.getRequestId(), t), new byte[0]);
        }
    }

    @Override
    public int type() {
        return TinyRpcMessageType.RPC_REQUEST.getCode();
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
