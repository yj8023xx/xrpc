package com.smallc.xrpc.common.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.serializer
 */
public class SerializationUtil {

    private static final Logger logger = LoggerFactory.getLogger(SerializationUtil.class);

    /**
     * Map
     * Integer    序列化器类型
     * Serializer 序列化器实现
     */
    private static final SerializationType DEFAULT_SERIALIZE_TYPE = SerializationType.HESSIAN;

    public static <T> byte[] serialize(T entry, SerializationType type) {
        return SerializerFactory.getSerializer(type).serialize(entry);
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz, SerializationType type) {
        return (T) SerializerFactory.getSerializer(type).deserialize(bytes, clazz);
    }

    public static <T> byte[] serialize(T entry) {
        return SerializerFactory.getSerializer(DEFAULT_SERIALIZE_TYPE).serialize(entry);
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return (T) SerializerFactory.getSerializer(DEFAULT_SERIALIZE_TYPE).deserialize(bytes, clazz);
    }

}
