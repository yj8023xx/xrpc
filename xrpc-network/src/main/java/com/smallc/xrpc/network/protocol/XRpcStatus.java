package com.smallc.xrpc.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public enum XRpcStatus {

    SUCCESS(0, "Success"),
    NO_PROVIDER(-2, "No Provider"),
    UNKNOWN_ERROR(-1, "Unknown Error");

    private static Map<Integer, String> valToStr = new HashMap<>();
    private static Map<String, Integer> strToVal = new HashMap<>();
    private int value;
    private String content;

    static {
        for (XRpcStatus code : XRpcStatus.values()) {
            valToStr.put(code.value, code.content);
            strToVal.put(code.content, code.value);
        }
    }

    XRpcStatus(int value, String content) {
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
