package com.smallc.xrpc.network.protocol;

import java.nio.charset.StandardCharsets;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public class XRpcResponseHeader extends XRpcHeader {

    private int code;
    private String error;

    public XRpcResponseHeader(int messageTypeId, int serializationId, int requestId, Throwable throwable) {
        this(messageTypeId, serializationId, requestId, XRpcCode.UNKNOWN_ERROR.getValue(), throwable.getMessage());
    }

    public XRpcResponseHeader(int messageTypeId, int serializationId, int requestId) {
        this(messageTypeId, serializationId, requestId, XRpcCode.SUCCESS.getValue(), null);
    }

    public XRpcResponseHeader(int messageTypeId, int serializationId, int requestId, int code, String error) {
        super(messageTypeId, serializationId, requestId);
        this.code = code;
        this.error = error;
        this.setHeaderLength(XRpcHeader.length() + Integer.BYTES + (error == null ? 0 : error.getBytes(StandardCharsets.UTF_8).length));
    }

    public XRpcResponseHeader(XRpcHeader header, int code, String error) {
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
