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
public class XRpcResponseEncoder extends XRpcMessageEncoder {

    @Override
    protected void encodeHeader(ChannelHandlerContext channelHandlerContext, XRpcHeader header, ByteBuf byteBuf) throws Exception {
        super.encodeHeader(channelHandlerContext, header, byteBuf);
        if (header instanceof XRpcResponseHeader) {
            XRpcResponseHeader responseHeader = (XRpcResponseHeader) header;
            byteBuf.writeInt(responseHeader.getCode());
            byteBuf.writeBytes(responseHeader.getError() == null ? new byte[0] : responseHeader.getError().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new Exception(String.format("Invalid header type: %s!", header.getClass().getCanonicalName()));
        }
    }

}
