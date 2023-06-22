package com.smallc.tinyrpc.common.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.serializer
 */
public enum SerializeType {

    HESSIAN(0, "HESSIAN"),
    PROTOCOL(1, "PROTOCOL"),
    CUSTOM(2, "CUSTOM");

    private static Map<Integer, SerializeType> types = new HashMap<>();
    private int code;
    private String message;

    static {
        for (SerializeType type : SerializeType.values()) {
            types.put(type.code, type);
        }
    }

    SerializeType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SerializeType valueOf(int code) {
        return types.get(code);
    }

    public int getVal() {
        return code;
    }

    public String getMessage(Object... args) {
        if (args.length < 1) {
            return message;
        }
        return String.format(message, args);
    }

}
