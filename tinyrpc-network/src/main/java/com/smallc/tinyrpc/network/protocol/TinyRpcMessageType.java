package com.smallc.tinyrpc.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.protocol
 */
public enum TinyRpcMessageType {

    RPC_REQUEST(0, "RPC REQUEST"),
    RPC_RESPONSE(1, "RPC RESPONSE");

    private static Map<Integer, TinyRpcMessageType> types = new HashMap<>();
    private int code;
    private String message;

    static {
        for (TinyRpcMessageType type : TinyRpcMessageType.values()) {
            types.put(type.code, type);
        }
    }

    TinyRpcMessageType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TinyRpcMessageType valueOf(int code) {
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
