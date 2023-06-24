package com.smallc.tinyrpc.network.protocol;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.protocol
 */
public class TinyRpcHeader {

    private int magicNumber = TinyRpcConstant.MAGIC_NUMBER;
    private int version = TinyRpcConstant.VERSION;
    private int headerLength = Integer.BYTES * 7;
    private int totalLength;
    private int messageType;
    private int serializeType;
    private int requestId;

    public TinyRpcHeader(int messageType, int serializeType, int requestId) {
        this.messageType = messageType;
        this.serializeType = serializeType;
        this.requestId = requestId;
    }

    public TinyRpcHeader(int magicNumber, int version, int headerLength, int totalLength, int messageType, int serializeType, int requestId) {
        this.magicNumber = magicNumber;
        this.version = version;
        this.headerLength = headerLength;
        this.totalLength = totalLength;
        this.messageType = messageType;
        this.serializeType = serializeType;
        this.requestId = requestId;
    }

    public TinyRpcHeader(TinyRpcHeader header) {
        this(header.getMagicNumber(), header.getVersion(), header.getHeaderLength(),
                header.getTotalLength(), header.getMessageType(), header.getSerializeType(), header.getRequestId());
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

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(int serializeType) {
        this.serializeType = serializeType;
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
