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
    private boolean flag = false;

    @Override
    public void start(String host, int port, RequestHandlerRegistry requestHandlerRegistry) throws Exception {
        long[] affinity = new long[10];
        serverGroup = XRpcServerGroup.createServerGroup(1000, affinity, requestHandlerRegistry)
                .option(RdmaOption.MAX_SEND_WR, 100)
                .option(RdmaOption.MAX_RECV_WR, 100)
                .option(RdmaOption.BUFFER_SIZE, 256)
                .option(RdmaOption.BUFFER_COUNT, 20);
        serverEndPoint = serverGroup.createServerEndpoint();
        InetSocketAddress address = new InetSocketAddress(host, port);
        serverEndPoint.bind(address, 100);
        flag = true;
        while (flag) {
            serverEndPoint.accept();
        }
    }

    @Override
    public void stop() {
        flag = false;
        try {
            serverEndPoint.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
