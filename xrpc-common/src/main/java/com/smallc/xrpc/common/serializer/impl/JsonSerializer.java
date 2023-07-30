package com.smallc.xrpc.common.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.smallc.xrpc.common.serializer.SerializationType;
import com.smallc.xrpc.common.serializer.Serializer;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.serializer.impl
 */
public class JsonSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }

    @Override
    public SerializationType type() {
        return SerializationType.JSON;
    }

}
