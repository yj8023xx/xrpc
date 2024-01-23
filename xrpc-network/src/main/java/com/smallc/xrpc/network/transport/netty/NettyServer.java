package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import com.smallc.xrpc.network.transport.TransportServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.netty
 */
public class NettyServer implements TransportServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private String host;
    private int port;
    private EventLoopGroup acceptEventGroup;
    private EventLoopGroup ioEventGroup;
    private Channel channel;
    private RequestHandlerRegistry requestHandlerRegistry;

    public NettyServer() {
    }

    @Override
    public void start(String host, int port, RequestHandlerRegistry requestHandlerRegistry) throws Exception {
        this.host = host;
        this.port = port;
        this.requestHandlerRegistry = requestHandlerRegistry;

        EventLoopGroup acceptEventGroup = newEventLoopGroup();
        EventLoopGroup ioEventGroup = newEventLoopGroup();
        ChannelHandler channelHandlerPipeline = newChannelHandlerPipeline();
        ServerBootstrap serverBootstrap = newBootstrap(channelHandlerPipeline, acceptEventGroup, ioEventGroup);
        Channel channel = doBind(serverBootstrap);

        this.acceptEventGroup = acceptEventGroup;
        this.ioEventGroup = ioEventGroup;
        this.channel = channel;
    }

    @Override
    public void stop() {
        if (null != acceptEventGroup) {
            acceptEventGroup.shutdownGracefully();
        }
        if (null != ioEventGroup) {
            ioEventGroup.shutdownGracefully();
        }
        if (null != channel) {
            channel.close();
        }
    }

    private Channel doBind(ServerBootstrap serverBootstrap) throws Exception {
        return serverBootstrap.bind(host, port)
                .sync()
                .channel();
    }

    private EventLoopGroup newEventLoopGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup();
        } else {
            return new NioEventLoopGroup();
        }
    }

    private ChannelHandler newChannelHandlerPipeline() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline()
                        .addLast(new XRpcMessageDecoder())
                        .addLast(new XRpcMessageEncoder())
                        .addLast(new NettyRequestInvocation(requestHandlerRegistry));
            }
        };
    }

    private ServerBootstrap newBootstrap(ChannelHandler channelHandler, EventLoopGroup acceptEventGroup, EventLoopGroup ioEventGroup) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .group(acceptEventGroup, ioEventGroup)
                .childHandler(channelHandler)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return serverBootstrap;
    }

}
