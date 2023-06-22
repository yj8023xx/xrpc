package com.smallc.tinyrpc.network.transport.netty;

import com.smallc.tinyrpc.network.protocol.TinyRpcPacket;
import com.smallc.tinyrpc.network.transport.InFlightRequests;
import com.smallc.tinyrpc.network.transport.ResponseFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LiYue
 * Date: 2019/9/20
 */
@ChannelHandler.Sharable
public class NettyResponseInvocation extends SimpleChannelInboundHandler<TinyRpcPacket> {

    private static final Logger logger = LoggerFactory.getLogger(NettyResponseInvocation.class);
    private final InFlightRequests inFlightRequests;

    NettyResponseInvocation(InFlightRequests inFlightRequests) {
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TinyRpcPacket response) {
        ResponseFuture<TinyRpcPacket> future = inFlightRequests.remove(response.getHeader().getRequestId());
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
