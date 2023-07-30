package com.smallc.xrpc.network.transport;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.transport
 */
public interface TransportClient extends Closeable {

    Transport createTransport(SocketAddress address, long connectionTimeout) throws InterruptedException, TimeoutException;

    @Override
    void close();
    
}