package com.smallc.xrpc.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public enum XRpcMessageType {

    RPC_REQUEST(0, "RPC Request"),
    RPC_RESPONSE(1, "RPC Response");

    private static Map<Integer, String> valToStr = new HashMap<>();
    private static Map<String, Integer> strToVal = new HashMap<>();
    private int value;
    private String content;

    static {
        for (XRpcMessageType type : XRpcMessageType.values()) {
            valToStr.put(type.value, type.content);
            strToVal.put(type.content, type.value);
        }
    }

    XRpcMessageType(int value, String content) {
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

    public static String toString(int value) {
        return valToStr.get(value);
    }

    public static int getValue(String str) {
        return strToVal.get(str);
    }

}
