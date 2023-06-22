package com.smallc.tinyrpc.network.protocol;

import com.smallc.tinyrpc.common.serializer.SerializeType;

import java.nio.charset.StandardCharsets;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.protocol
 */
public class TinyRpcResponseHeader extends TinyRpcHeader {

    private int code;
    private String error;

    public TinyRpcResponseHeader(int messageType, int requestId, Throwable throwable) {
        this(messageType, SerializeType.HESSIAN.getVal(), requestId, TinyRpcCode.UNKNOWN_ERROR.getVal(), throwable.getMessage());
    }

    public TinyRpcResponseHeader(int messageType, int requestId, int code, String error) {
        this(messageType, SerializeType.HESSIAN.getVal(), requestId, code, error);
    }

    public TinyRpcResponseHeader(int messageType, int serializeMethod, int requestId) {
        this(messageType, serializeMethod, requestId, TinyRpcCode.SUCCESS.getVal(), null);
    }

    public TinyRpcResponseHeader(int messageType, int serializeMethod, int requestId, int code, String error) {
        super(messageType, serializeMethod, requestId);
        this.code = code;
        this.error = error;
        this.setHeaderLength(TinyRpcHeader.length() + Integer.BYTES + (error == null ? 0 : error.getBytes(StandardCharsets.UTF_8).length));
    }

    public TinyRpcResponseHeader(TinyRpcHeader header, int code, String error) {
        super(header);
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
