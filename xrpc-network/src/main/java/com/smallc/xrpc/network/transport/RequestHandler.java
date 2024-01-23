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
     * Handle request
     *
     * @param request
     * @return response
     */
    T handle(T request);

    /**
     *
     * @return the type of handler
     */
    int type();
    
}
