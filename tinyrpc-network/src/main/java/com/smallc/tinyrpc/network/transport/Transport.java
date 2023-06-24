package com.smallc.tinyrpc.network.transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport
 */
public interface Transport<T> {
    
    /**
     * 发送请求命令
     * @param request 请求命令
     * @return 返回值是一个Future，Future
     */
    CompletableFuture<T> send(T request);

}
