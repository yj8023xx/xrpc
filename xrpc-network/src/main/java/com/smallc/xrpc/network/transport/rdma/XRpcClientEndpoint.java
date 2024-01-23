package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.verbs.RdmaCmId;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.PendingRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/29
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class XRpcClientEndpoint extends XRpcEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(XRpcClientEndpoint.class);

    private PendingRequests pendingRequests;

    protected XRpcClientEndpoint(XRpcEndpointGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
    }

    @Override
    public void onMessageComplete(XRpcMessage response) {
        ResponseFuture<XRpcMessage> future = pendingRequests.remove(response.getHeader().getRequestId());
        if (null != future) {
            future.getFuture().complete(response);
        } else {
            logger.warn("Drop response: {}", response);
        }
    }

    public void setPendingRequests(PendingRequests pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

}
