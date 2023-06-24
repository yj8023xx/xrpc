package com.smallc.tinyrpc.network.codec;

import com.smallc.tinyrpc.network.protocol.TinyRpcHeader;
import com.smallc.tinyrpc.network.protocol.TinyRpcPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.codec
 */
public class TinyRpcPacketEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (!(o instanceof TinyRpcPacket)) {
            throw new Exception(String.format("Unknown type: %s!", o.getClass().getCanonicalName()));
        }

        TinyRpcPacket packet = (TinyRpcPacket) o;
        encodeHeader(channelHandlerContext, packet.getHeader(), byteBuf);
        byteBuf.writeBytes(packet.getPayload());
    }

    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, TinyRpcHeader header, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(header.getMagicNumber());
        byteBuf.writeInt(header.getVersion());
        byteBuf.writeInt(header.getHeaderLength());
        byteBuf.writeInt(header.getTotalLength());
        byteBuf.writeInt(header.getMessageType());
        byteBuf.writeInt(header.getSerializeType());
        byteBuf.writeInt(header.getRequestId());
    }

}
