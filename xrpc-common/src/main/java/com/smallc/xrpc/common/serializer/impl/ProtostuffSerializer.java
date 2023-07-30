package com.smallc.xrpc.common.serializer.impl;

import com.smallc.xrpc.common.serializer.ProtostuffSerializerUtil;
import com.smallc.xrpc.common.serializer.SerializationType;
import com.smallc.xrpc.common.serializer.Serializer;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/6/25
 * @since com.smallc.xrpc.common.serializer.impl
 */
public class ProtostuffSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        return ProtostuffSerializerUtil.serialize(obj);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return ProtostuffSerializerUtil.deserialize(bytes, clazz);
    }

    @Override
    public SerializationType type() {
        return SerializationType.PROTOSTUFF;
    }

}
