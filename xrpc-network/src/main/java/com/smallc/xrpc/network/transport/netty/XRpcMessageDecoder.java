package com.smallc.xrpc.network.transport.netty;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.codec
 */

import com.smallc.xrpc.network.codec.XRpcHeaderCodec;
import com.smallc.xrpc.network.protocol.XRpcConstant;
import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class XRpcMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // If the readable bytes are not enough to parse the header
        if (in.readableBytes() < XRpcConstant.FIXED_HEADER_LENGTH) {
            return;
        }

        // Mark the current read position
        in.markReaderIndex();

        // Read the header information
        byte[] headerBytes = new byte[XRpcConstant.FIXED_HEADER_LENGTH];
        in.readBytes(headerBytes);
        XRpcHeader header = XRpcHeaderCodec.decode(headerBytes);

        // If the readable bytes are not enough to parse the complete message
        if (in.readableBytes() < header.getTotalLength() - XRpcConstant.FIXED_HEADER_LENGTH) {
            // Reset the read position, wait for more data
            in.resetReaderIndex();
            return;
        }

        // Read the payload
        byte[] payload = new byte[header.getTotalLength() - XRpcConstant.FIXED_HEADER_LENGTH];
        in.readBytes(payload);

        // Add the parsed message to the output list
        out.add(new XRpcMessage(header, payload));
    }

}


