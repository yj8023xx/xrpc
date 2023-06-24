package com.smallc.tinyrpc.common.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.serializer
 */
public class SerializeUtil {

    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    /**
     * Map
     *  Integer    序列化器类型
     *  Serializer 序列化器实现
     */
    public static final String DEFAULT_SERIALIZE_TYPE = SerializeType.HESSIAN.getMessage();

    public static <T> byte[] serialize(T entry, String type) {
        return SerializerFactory.getSerializer(type).serialize(entry);
    }

    public static <T> byte[] serialize(T entry) {
        return SerializerFactory.getSerializer(DEFAULT_SERIALIZE_TYPE).serialize(entry);
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz, String type) {
        return (T) SerializerFactory.getSerializer(type).deserialize(bytes, clazz);
    }

}
