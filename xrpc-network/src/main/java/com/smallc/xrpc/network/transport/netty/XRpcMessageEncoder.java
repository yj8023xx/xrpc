package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.codec.XRpcHeaderCodec;
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

        XRpcMessage message = (XRpcMessage) o;
        byte[] headerBytes = XRpcHeaderCodec.encode(message.getHeader());
        byteBuf.writeBytes(headerBytes);
        byteBuf.writeBytes(message.getPayload());
    }

}
