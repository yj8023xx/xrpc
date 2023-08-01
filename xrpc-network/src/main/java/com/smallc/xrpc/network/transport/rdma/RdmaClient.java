package com.smallc.xrpc.network.transport.rdma;

import com.smallc.xrpc.network.transport.TransportClient;
import com.smallc.xrpc.network.transport.InFlightRequests;
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
    private InFlightRequests inFlightRequests;

    public RdmaClient() {
        try {
            clientGroup = XRpcClientGroup.createClientGroup(1000)
                    .option(RdmaOption.MAX_SEND_WR, 100)
                    .option(RdmaOption.MAX_RECV_WR, 100)
                    .option(RdmaOption.BUFFER_SIZE, 256)
                    .option(RdmaOption.BUFFER_COUNT, 10);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        inFlightRequests = new InFlightRequests();
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
        clientEndPoint.setInFlightRequests(inFlightRequests);
        return new RdmaTransport(clientEndPoint, inFlightRequests);
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
