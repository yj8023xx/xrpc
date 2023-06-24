package com.smallc.tinyrpc.common.serializer.impl;

import com.smallc.tinyrpc.common.serializer.Serializer;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.serializer.impl
 */
public class JsonSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        return new byte[0];
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

}
