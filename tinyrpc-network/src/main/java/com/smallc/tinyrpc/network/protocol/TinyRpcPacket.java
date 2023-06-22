package com.smallc.tinyrpc.network.protocol;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.protocol
 */
public class TinyRpcPacket {

    private TinyRpcHeader header;
    private byte[] payload;

    public TinyRpcPacket(TinyRpcHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
        this.header.setTotalLength(header.getHeaderLength() + payload.length);
    }

    public TinyRpcHeader getHeader() {
        return header;
    }

    public void setHeader(TinyRpcHeader header) {
        this.header = header;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

}
