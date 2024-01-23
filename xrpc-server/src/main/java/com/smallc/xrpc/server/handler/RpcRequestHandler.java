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
 * @since com.smallc.xrpc.network.transport.TransportServer.server.handler
 */
@RpcSingleton
public class RpcRequestHandler implements RequestHandler<XRpcMessage>, ServiceProviderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    /**
     * Map
     * String - service name
     * Object - service provider
     */
    private Map<String, Object> serviceProviders = new ConcurrentHashMap<>();

    @Override
    public XRpcMessage handle(XRpcMessage message) {
        XRpcHeader header = message.getHeader();
        // Deserialize rpc request from payload
        SerializationType type = SerializationType.getType(header.getSerializationId());
        XRpcRequest request = SerializationUtil.deserialize(message.getPayload(), XRpcRequest.class, type);
        try {
            // Search for required service in rpc request
            Object serviceProvider = serviceProviders.get(request.getServiceName());
            // logger.info("Get serviceName: {} methodName: {}.", request.getServiceName(), request.getMethodName());
            if (serviceProvider != null) {
                // Use Java reflection mechanism to call the corresponding method of the service
                String methodName = request.getMethodName();
                Map<String, Object> argMap = request.getArgMap();
                Object[] args = argMap.values().toArray();
                Class<?>[] parameterTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                }
                Method method = serviceProvider.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceProvider, args);
                // Encapsulate the result into a message and return it
                return new XRpcMessage(new XRpcHeader((byte) XRpcMessageType.RPC_RESPONSE.getValue(), header.getSerializationId(), header.getRequestId(), (byte) XRpcStatus.SUCCESS.getValue()), SerializationUtil.serialize(result, type));
            }
            // Return NO_PROVIDER response if the service cannot be found
            logger.warn("No service provider of {}#{}(String)!", request.getServiceName(), request.getMethodName());
            return new XRpcMessage(new XRpcHeader((byte) XRpcMessageType.RPC_RESPONSE.getValue(), header.getSerializationId(), header.getRequestId(), (byte) XRpcStatus.NO_PROVIDER.getValue()), new byte[0]);
        } catch (Throwable t) {
            // Return UNKNOWN_ERROR response when an exception occurs
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
