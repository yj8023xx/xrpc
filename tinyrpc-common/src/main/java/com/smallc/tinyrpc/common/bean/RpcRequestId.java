package com.smallc.tinyrpc.common.bean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.bean
 */
public class RpcRequestId {

    private final static AtomicInteger nextRequestId = new AtomicInteger(0);

    public static int next() {
        return nextRequestId.getAndIncrement();
    }

}
