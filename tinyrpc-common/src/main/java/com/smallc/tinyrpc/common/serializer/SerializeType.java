package com.smallc.tinyrpc.common.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.serializer
 */
public enum SerializeType {

    HESSIAN(0, "hessian"),
    JSON(1, "json"),
    CUSTOM(2, "custom");

    private static Map<Integer, SerializeType> types = new HashMap<>();
    private static Map<String, Integer> messageToCode = new HashMap<>();
    private int code;
    private String message;

    static {
        for (SerializeType type : SerializeType.values()) {
            types.put(type.code, type);
            messageToCode.put(type.message, type.code);
        }
    }

    SerializeType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SerializeType valueOf(int code) {
        return types.get(code);
    }

    public static int codeOf(String message) {
        return messageToCode.get(message);
    }

    public static String messageOf(int code) {
        return types.get(code).message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage(Object... args) {
        if (args.length < 1) {
            return message;
        }
        return String.format(message, args);
    }

}
