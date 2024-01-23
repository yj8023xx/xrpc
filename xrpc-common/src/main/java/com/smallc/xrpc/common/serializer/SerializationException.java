package com.smallc.xrpc.common.serializer;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.serializer
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(Throwable throwable) {
        super(throwable);
    }

}
