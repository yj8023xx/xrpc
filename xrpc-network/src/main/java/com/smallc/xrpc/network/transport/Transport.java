package com.smallc.xrpc.network.transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.transport
 */
public interface Transport<T> {
    
    /**
     * 发送请求命令
     * @param request 请求命令
     * @return Future
     */
    CompletableFuture<T> send(T request);

}
