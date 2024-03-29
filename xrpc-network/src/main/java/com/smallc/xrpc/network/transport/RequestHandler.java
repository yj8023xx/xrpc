package com.smallc.xrpc.network.transport;

/**
 * 请求处理器
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.handler
 */
public interface RequestHandler<T> {

    /**
     * 处理请求
     * @param request 请求命令
     * @return 响应命令
     */
    T handle(T request);

    /**
     * 支持的请求类型
     */
    int type();
    
}
