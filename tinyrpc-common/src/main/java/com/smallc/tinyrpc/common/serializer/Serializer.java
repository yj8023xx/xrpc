package com.smallc.tinyrpc.common.serializer;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.serializer
 */
public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);

    String type();

}
