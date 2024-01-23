package com.smallc.xrpc.common.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.bean
 */
public class RpcRequestId {

    private final static AtomicLong nextRequestId = new AtomicLong(0);

    public static long next() {
        return nextRequestId.getAndIncrement();
    }

}
