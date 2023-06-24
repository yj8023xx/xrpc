package com.smallc.tinyrpc.common.loadbalancer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer
 */
public enum LoadBalancerType {

    RANDOM(0, "random"),
    ROUND_ROBIN(1, "roundrobin"),
    IP_HASH(2, "iphash"),
    CONSISTENT_HASH(3, "consistenthash");

    private static Map<Integer, LoadBalancerType> types = new HashMap<>();
    private int code;
    private String message;

    static {
        for (LoadBalancerType type : LoadBalancerType.values()) {
            types.put(type.code, type);
        }
    }

    LoadBalancerType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static LoadBalancerType valueOf(int code) {
        return types.get(code);
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
