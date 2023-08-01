package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.InFlightRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport.netty
 */
@ChannelHandler.Sharable
public class NettyResponseInvocation extends SimpleChannelInboundHandler<XRpcMessage> {

    private static final Logger logger = LoggerFactory.getLogger(NettyResponseInvocation.class);
    private final InFlightRequests inFlightRequests;

    NettyResponseInvocation(InFlightRequests inFlightRequests) {
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XRpcMessage response) {
        ResponseFuture<XRpcMessage> future = inFlightRequests.remove(response.getHeader().getRequestId());
        if (null != future) {
            future.getFuture().complete(response);
        } else {
            logger.warn("Drop response: {}", response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception: ", cause);
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) ctx.close();
    }
    
}
