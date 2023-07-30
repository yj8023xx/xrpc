package com.smallc.xrpc.network.codec;

import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.codec
 */
public class XRpcMessageEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (!(o instanceof XRpcMessage)) {
            throw new Exception(String.format("Unknown type: %s!", o.getClass().getCanonicalName()));
        }

        XRpcMessage packet = (XRpcMessage) o;
        encodeHeader(channelHandlerContext, packet.getHeader(), byteBuf);
        byteBuf.writeBytes(packet.getPayload());
    }

    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, XRpcHeader header, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(header.getMagicNumber());
        byteBuf.writeInt(header.getVersion());
        byteBuf.writeInt(header.getHeaderLength());
        byteBuf.writeInt(header.getTotalLength());
        byteBuf.writeInt(header.getMessageTypeId());
        byteBuf.writeInt(header.getSerializationId());
        byteBuf.writeInt(header.getRequestId());
    }

}
