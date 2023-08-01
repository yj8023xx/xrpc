package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.codec.XRpcHeaderCodec;
import com.smallc.xrpc.network.protocol.XRpcConstant;
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
        byte[] headerBytes = new byte[XRpcConstant.FIXED_HEADER_LENGTH];
        byteBuf.readBytes(headerBytes);
        XRpcHeader header = XRpcHeaderCodec.decode(headerBytes);
        byte[] payload = new byte[header.getTotalLength() - header.getHeaderLength()];
        byteBuf.readBytes(payload);
        list.add(new XRpcMessage(header, payload));
    }

}
