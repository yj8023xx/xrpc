package com.smallc.tinyrpc.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.protocol
 */
public enum TinyRpcCode {

    SUCCESS(0, "SUCCESS"),
    NO_PROVIDER(-2, "NO PROVIDER"),
    UNKNOWN_ERROR(-1, "UNKNOWN ERROR");

    private static Map<Integer, TinyRpcCode> codes = new HashMap<>();
    private int code;
    private String message;

    static {
        for (TinyRpcCode code : TinyRpcCode.values()) {
            codes.put(code.code, code);
        }
    }

    TinyRpcCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TinyRpcCode valueOf(int code) {
        return codes.get(code);
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
