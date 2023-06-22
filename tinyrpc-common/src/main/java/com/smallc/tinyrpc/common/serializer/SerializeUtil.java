package com.smallc.tinyrpc.common.serializer;

import com.smallc.tinyrpc.common.spi.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
    private static Map<Integer, Serializer> serializerMap = new HashMap<>();
    public static final int DEFAULT_SERIALIZE_TYPE = SerializeType.HESSIAN.getVal();

    static {
        for (Serializer serializer : ServiceLoader.loadAll(Serializer.class)) {
            serializerMap.put(serializer.type(), serializer);
        }
    }

    public static <T> byte[] serialize(T entry, int type) {
        return serializerMap.get(type).serialize(entry);
    }

    public static <T> byte[] serialize(T entry) {
        return serializerMap.get(DEFAULT_SERIALIZE_TYPE).serialize(entry);
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz, int type) {
        return (T) serializerMap.get(type).deserialize(bytes, clazz);
    }

}
