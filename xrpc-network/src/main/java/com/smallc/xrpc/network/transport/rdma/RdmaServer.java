package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaServerEndpoint;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import com.smallc.xrpc.network.transport.TransportServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/2
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class RdmaServer implements TransportServer {

    private XRpcServerGroup serverGroup;
    private RdmaServerEndpoint<XRpcServerEndpoint> serverEndPoint;
    private int threadCount = 4;
    private boolean running = false;

    @Override
    public void start(String host, int port, RequestHandlerRegistry requestHandlerRegistry) throws Exception {
        serverGroup = XRpcServerGroup.createServerGroup(1000, threadCount, requestHandlerRegistry)
                .option(RdmaOption.MAX_SEND_WR, 100)
                .option(RdmaOption.MAX_RECV_WR, 100)
                .option(RdmaOption.BUFFER_SIZE, 256)
                .option(RdmaOption.BUFFER_COUNT, 20)
                .option(RdmaOption.MAX_INLINE_DATA, 64);
        serverEndPoint = serverGroup.createServerEndpoint();
        InetSocketAddress address = new InetSocketAddress(host, port);
        serverEndPoint.bind(address, 100);
        running = true;
        while (running) {
            serverEndPoint.accept();
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            serverGroup.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
