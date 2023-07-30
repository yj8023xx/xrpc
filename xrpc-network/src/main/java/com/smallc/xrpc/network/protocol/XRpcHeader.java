package com.smallc.xrpc.network.protocol;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public class XRpcHeader {

    private int magicNumber = XRpcConstant.MAGIC_NUMBER;
    private int version = XRpcConstant.VERSION;
    private int headerLength = Integer.BYTES * 7;
    private int totalLength;
    private int messageTypeId;
    private int serializationId;
    private int status;
    private int requestId;

    public XRpcHeader(int messageTypeId, int serializationId, int requestId) {
        this.messageTypeId = messageTypeId;
        this.serializationId = serializationId;
        this.requestId = requestId;
    }

    public XRpcHeader(int magicNumber, int version, int headerLength, int totalLength, int messageTypeId, int serializationId, int requestId) {
        this.magicNumber = magicNumber;
        this.version = version;
        this.headerLength = headerLength;
        this.totalLength = totalLength;
        this.messageTypeId = messageTypeId;
        this.serializationId = serializationId;
        this.requestId = requestId;
    }

    public XRpcHeader(XRpcHeader header) {
        this(header.getMagicNumber(), header.getVersion(), header.getHeaderLength(),
                header.getTotalLength(), header.getMessageTypeId(), header.getSerializationId(), header.getRequestId());
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public int getMessageTypeId() {
        return messageTypeId;
    }

    public void setMessageTypeId(int messageTypeId) {
        this.messageTypeId = messageTypeId;
    }

    public int getSerializationId() {
        return serializationId;
    }

    public void setSerializationId(int serializationId) {
        this.serializationId = serializationId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public static int length() {
        return Integer.BYTES * 7;
    }

}
