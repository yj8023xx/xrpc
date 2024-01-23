package com.smallc.xrpc.common.loadbalancer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer
 */
public enum LoadBalanceType {

    RANDOM(0, "random"),
    ROUND_ROBIN(1, "roundrobin"),
    IP_HASH(2, "iphash"),
    CONSISTENT_HASH(3, "consistenthash");

    private static Map<Integer, LoadBalanceType> types = new HashMap<>();
    private static Map<Integer, String> valToStr = new HashMap<>();
    private static Map<String, Integer> strToVal = new HashMap<>();
    private int value;
    private String content;

    static {
        for (LoadBalanceType type : LoadBalanceType.values()) {
            types.put(type.value, type);
            valToStr.put(type.value, type.content);
            strToVal.put(type.content, type.value);
        }
    }

    LoadBalanceType(int value, String content) {
        this.value = value;
        this.content = content;
    }

    public static LoadBalanceType getType(int value) {
        return types.get(value);
    }

    public static LoadBalanceType getType(String str) {
        return types.get(getValue(str));
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
