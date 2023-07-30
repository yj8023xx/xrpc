package com.smallc.xrpc.network.codec;

import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcResponseHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.network.codec
 */
public class XRpcResponseDecoder extends XRpcMessageDecoder {

    @Override
    protected XRpcHeader decodeHeader(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        XRpcHeader header = super.decodeHeader(channelHandlerContext, byteBuf);
        int code = byteBuf.readInt();
        int errorLength = header.getHeaderLength() - XRpcHeader.length() - Integer.BYTES;
        byte[] errorBytes = new byte[errorLength];
        byteBuf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);
        return new XRpcResponseHeader(header, code, error);
    }

}
