package com.smallc.xrpc.network.protocol;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public class XRpcHeader {

    private short magicNumber = XRpcConstant.MAGIC_NUMBER;
    private byte version = XRpcConstant.VERSION;
    private short headerLength = XRpcConstant.FIXED_HEADER_LENGTH;
    private short totalLength;
    private byte messageTypeId;
    private byte serializationId;
    private long requestId;
    private byte status;

    public XRpcHeader() {
    }

    public XRpcHeader(byte messageTypeId, byte serializationId, long requestId, byte status) {
        this.messageTypeId = messageTypeId;
        this.serializationId = serializationId;
        this.requestId = requestId;
        this.status = status;
    }

    public XRpcHeader(short magicNumber, byte version, short headerLength, short totalLength, byte messageTypeId, byte serializationId, long requestId, byte status) {
        this.magicNumber = magicNumber;
        this.version = version;
        this.headerLength = headerLength;
        this.totalLength = totalLength;
        this.messageTypeId = messageTypeId;
        this.serializationId = serializationId;
        this.requestId = requestId;
        this.status = status;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setHeaderLength(short headerLength) {
        this.headerLength = headerLength;
    }

    public short getHeaderLength() {
        return headerLength;
    }

    public short getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(short totalLength) {
        this.totalLength = totalLength;
    }

    public byte getMessageTypeId() {
        return messageTypeId;
    }

    public void setMessageTypeId(byte messageTypeId) {
        this.messageTypeId = messageTypeId;
    }

    public byte getSerializationId() {
        return serializationId;
    }

    public void setSerializationId(byte serializationId) {
        this.serializationId = serializationId;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

}
