package com.smallc.tinyrpc.network.codec;

import com.smallc.tinyrpc.network.protocol.TinyRpcHeader;
import com.smallc.tinyrpc.network.protocol.TinyRpcPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.codec
 */
public class TinyRpcPacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        TinyRpcHeader header = decodeHeader(channelHandlerContext, byteBuf);
        int payloadLength = header.getTotalLength() - header.getHeaderLength();
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        list.add(new TinyRpcPacket(header, payload));
    }

    protected TinyRpcHeader decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        int magicNumber = byteBuf.readInt();
        int version = byteBuf.readInt();
        int headerLength = byteBuf.readInt();
        int totalLength = byteBuf.readInt();
        int messageType = byteBuf.readInt();
        int serializeMethod = byteBuf.readInt();
        int requestId = byteBuf.readInt();
        return new TinyRpcHeader(
                magicNumber, version, headerLength, totalLength, messageType, serializeMethod, requestId
        );
    }

}
