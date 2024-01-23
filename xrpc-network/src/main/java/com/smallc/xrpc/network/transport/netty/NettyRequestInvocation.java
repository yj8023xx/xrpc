package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.RequestHandler;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
public class NettyRequestInvocation extends SimpleChannelInboundHandler<XRpcMessage> {

    private static final Logger logger = LoggerFactory.getLogger(NettyRequestInvocation.class);
    private final RequestHandlerRegistry requestHandlerRegistry;
    NettyRequestInvocation(RequestHandlerRegistry requestHandlerRegistry) {
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XRpcMessage request) throws Exception {
        RequestHandler<XRpcMessage> handler = requestHandlerRegistry.getHandler(request.getHeader().getMessageTypeId());
        if (null != handler) {
            XRpcMessage response = handler.handle(request);
            if (null != response) {
                channelHandlerContext.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                    if (!channelFuture.isSuccess()) {
                        logger.warn("Write response failed!", channelFuture.cause());
                        channelHandlerContext.channel().close();
                    }
                });
            } else {
                logger.warn("Response is null!");
            }
        } else {
            throw new Exception(String.format("No handler for request with type: %d!", request.getHeader().getMessageTypeId()));
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
