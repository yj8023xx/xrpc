package com.smallc.xrpc.network.protocol;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.protocol
 */
public class XRpcMessage {

    private XRpcHeader header;
    private byte[] payload;

    public XRpcMessage(XRpcHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
        this.header.setTotalLength(header.getHeaderLength() + payload.length);
    }

    public XRpcHeader getHeader() {
        return header;
    }

    public void setHeader(XRpcHeader header) {
        this.header = header;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

}
