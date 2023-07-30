package com.smallc.xrpc.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public enum XRpcCode {

    SUCCESS(0, "Success"),
    NO_PROVIDER(-2, "No Provider"),
    UNKNOWN_ERROR(-1, "Unknown Error");

    private static Map<Integer, String> valToStr = new HashMap<>();
    private static Map<String, Integer> strToVal = new HashMap<>();
    private int value;
    private String str;

    static {
        for (XRpcCode code : XRpcCode.values()) {
            valToStr.put(code.value, code.str);
            strToVal.put(code.str, code.value);
        }
    }

    XRpcCode(int value, String str) {
        this.value = value;
        this.str = str;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return str;
    }

    public static int getValue(String str) {
        return strToVal.get(str);
    }

    public static String toString(int value) {
        return valToStr.get(value);
    }

}
