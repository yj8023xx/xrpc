package com.smallc.tinyrpc.network.transport;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport
 */
public interface TransportClient extends Closeable {

    Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException;
   
    @Override
    void close();
    
}