package com.smallc.xrpc.common.serializer;

import com.smallc.xrpc.common.spi.ServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.serializer
 */
public class SerializerFactory {

    private static Map<SerializationType, Serializer> serializerMap = new HashMap<>();

    static {
        for (Serializer serializer : ServiceLoader.loadAll(Serializer.class)) {
            serializerMap.put(serializer.type(), serializer);
        }
    }

    public static Serializer getDefaultSerializer() {
        return serializerMap.get(SerializationType.HESSIAN);
    }

    public static Serializer getSerializer(SerializationType type) {
        return serializerMap.get(type);
    }

}
