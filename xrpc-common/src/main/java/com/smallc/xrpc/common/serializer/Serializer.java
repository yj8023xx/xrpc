package com.smallc.xrpc.common.serializer;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.serializer
 */
public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);

    SerializationType type();

}
