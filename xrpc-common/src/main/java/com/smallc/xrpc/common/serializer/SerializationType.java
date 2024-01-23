package com.smallc.xrpc.common.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.serializer
 */
public enum SerializationType {

    JSON(0, "json"),
    HESSIAN(1, "hessian"),
    PROTOSTUFF(2, "protostuff"),
    MESSAGE_PACK(3, "msgpack");

    private static Map<Integer, SerializationType> types = new HashMap<>();
    private static Map<Integer, String> valToStr = new HashMap<>();
    private static Map<String, Integer> strToVal = new HashMap<>();
    private int value;
    private String content;

    static {
        for (SerializationType type : SerializationType.values()) {
            types.put(type.value, type);
            valToStr.put(type.value, type.content);
            strToVal.put(type.content, type.value);
        }
    }

    public static SerializationType getType(int value) {
        return types.get(value);
    }

    public static SerializationType getType(String str) {
        return types.get(getValue(str));
    }

    SerializationType(int value, String content) {
        this.value = value;
        this.content = content;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return content;
    }

    public static int getValue(String str) {
        return strToVal.get(str);
    }

    public static String getContent(int value) {
        return valToStr.get(value);
    }

}
