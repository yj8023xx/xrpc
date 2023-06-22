package com.smallc.tinyrpc.common.serializer;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.serializer
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String msg) {
        super(msg);
    }

    public SerializeException(Throwable throwable) {
        super(throwable);
    }

}
