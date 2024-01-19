package com.smallc.xrpc.network.transport.rdma;

import com.smallc.xrpc.network.transport.TransportClient;
import com.smallc.xrpc.network.transport.PendingRequests;
import com.smallc.xrpc.network.transport.Transport;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/2
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class RdmaClient implements TransportClient {

    private XRpcClientGroup clientGroup;
    private PendingRequests pendingRequests;
    private int threadCount = 4;

    public RdmaClient() {
        try {
            clientGroup = XRpcClientGroup.createClientGroup(1000, threadCount)
                    .option(RdmaOption.MAX_SEND_WR, 100)
                    .option(RdmaOption.MAX_RECV_WR, 100)
                    .option(RdmaOption.BUFFER_SIZE, 256)
                    .option(RdmaOption.BUFFER_COUNT, 20)
                    .option(RdmaOption.MAX_INLINE_DATA, 64);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pendingRequests = new PendingRequests();
    }

    @Override
    public Transport createTransport(SocketAddress address, long connectionTimeout) {
        XRpcClientEndpoint clientEndPoint;
        try {
            clientEndPoint = clientGroup.createEndpoint();
            clientEndPoint.connect(address, 1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new RdmaTransport(clientEndPoint, pendingRequests);
    }

    @Override
    public void close() {
        try {
            clientGroup.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
