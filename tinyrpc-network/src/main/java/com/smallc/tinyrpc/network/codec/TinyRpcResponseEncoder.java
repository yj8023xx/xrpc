package com.smallc.tinyrpc.network.codec;

import com.smallc.tinyrpc.network.codec.TinyRpcPacketEncoder;
import com.smallc.tinyrpc.network.protocol.TinyRpcHeader;
import com.smallc.tinyrpc.network.protocol.TinyRpcResponseHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.codec
 */
public class TinyRpcResponseEncoder extends TinyRpcPacketEncoder {

    @Override
    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, TinyRpcHeader header, ByteBuf byteBuf) throws Exception {
        super.encodeHeader(channelHandlerContext, header, byteBuf);
        if (header instanceof TinyRpcResponseHeader) {
            TinyRpcResponseHeader responseHeader = (TinyRpcResponseHeader) header;
            byteBuf.writeInt(responseHeader.getCode());
            byteBuf.writeBytes(responseHeader.getError() == null ? new byte[0] : responseHeader.getError().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new Exception(String.format("Invalid header type: %s!", header.getClass().getCanonicalName()));
        }
    }

}
