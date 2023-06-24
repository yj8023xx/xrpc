package com.smallc.tinyrpc.common.serializer;

import com.smallc.tinyrpc.common.spi.ServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.serializer
 */
public class SerializerFactory {

    private static Map<String, Serializer> serializerMap = new HashMap<>();

    static {
        for (Serializer serializer : ServiceLoader.loadAll(Serializer.class)) {
            serializerMap.put(serializer.type(), serializer);
        }
    }

    public static Serializer getDefaultSerializer() {
        return serializerMap.get(SerializeType.HESSIAN.getMessage());
    }

    public static Serializer getSerializer(String type) {
        return serializerMap.get(type);
    }

}
