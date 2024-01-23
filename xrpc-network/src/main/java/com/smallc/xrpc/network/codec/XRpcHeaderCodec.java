package com.smallc.xrpc.network.codec;

import com.smallc.xrpc.network.protocol.XRpcHeader;

import java.nio.ByteBuffer;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/30
 * @since com.smallc.xrpc.server.transport.TransportServer.network.codec.impl
 */
public class XRpcHeaderCodec {

    public static byte[] encode(XRpcHeader header) {
        ByteBuffer buffer = ByteBuffer.allocate(header.getHeaderLength());
        buffer.putShort(header.getMagicNumber());
        buffer.put(header.getVersion());
        buffer.putShort(header.getHeaderLength());
        buffer.putShort(header.getTotalLength());
        buffer.put(header.getMessageTypeId());
        buffer.put(header.getSerializationId());
        buffer.putLong(header.getRequestId());
        buffer.put(header.getStatus());
        return buffer.array();
    }

    public static XRpcHeader decode(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        short magicNumber = buffer.getShort();
        byte version = buffer.get();
        short headerLength = buffer.getShort();
        short totalLength = buffer.getShort();
        byte messageTypeId = buffer.get();
        byte serializationId = buffer.get();
        long requestId = buffer.getLong();
        byte status = buffer.get();
        return new XRpcHeader(magicNumber, version, headerLength, totalLength, messageTypeId, serializationId, requestId, status);
    }

}
