package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.verbs.RdmaCmId;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/29
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcServerEndpoint extends XRpcEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(XRpcServerEndpoint.class);

    private XRpcServerGroup serverGroup;

    protected XRpcServerEndpoint(XRpcServerGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.serverGroup = group;
    }

    @Override
    public void onMessageComplete(XRpcMessage request) {
        try {
            serverGroup.invoke(this, request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
