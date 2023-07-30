package com.smallc.xrpc.network.codec;

import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.codec
 */
public class XRpcMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        XRpcHeader header = decodeHeader(channelHandlerContext, byteBuf);
        int payloadLength = header.getTotalLength() - header.getHeaderLength();
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        list.add(new XRpcMessage(header, payload));
    }

    protected XRpcHeader decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        int magicNumber = byteBuf.readInt();
        int version = byteBuf.readInt();
        int headerLength = byteBuf.readInt();
        int totalLength = byteBuf.readInt();
        int messageType = byteBuf.readInt();
        int serializeMethod = byteBuf.readInt();
        int requestId = byteBuf.readInt();
        return new XRpcHeader(
                magicNumber, version, headerLength, totalLength, messageType, serializeMethod, requestId
        );
    }

}
