package com.smallc.tinyrpc.network.codec;

import com.smallc.tinyrpc.network.codec.TinyRpcPacketDecoder;
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
public class TinyRpcResponseDecoder extends TinyRpcPacketDecoder {

    @Override
    protected TinyRpcHeader decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        TinyRpcHeader header = super.decodeHeader(channelHandlerContext, byteBuf);
        int code = byteBuf.readInt();
        int errorLength = header.getHeaderLength() - TinyRpcHeader.length() - Integer.BYTES;
        byte[] errorBytes = new byte[errorLength];
        byteBuf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);
        return new TinyRpcResponseHeader(header, code, error);
    }

}
