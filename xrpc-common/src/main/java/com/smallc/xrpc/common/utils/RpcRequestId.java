package com.smallc.xrpc.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.bean
 */
public class RpcRequestId {

    private final static AtomicInteger nextRequestId = new AtomicInteger(0);

    public static int next() {
        return nextRequestId.getAndIncrement();
    }

}
