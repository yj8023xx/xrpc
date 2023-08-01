package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.verbs.RdmaCmId;
import com.smallc.xrpc.network.protocol.XRpcMessage;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/29
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcServerEndpoint extends XRpcEndpoint {

    private XRpcServerGroup serverGroup;
    private int clusterId;

    protected XRpcServerEndpoint(XRpcServerGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.serverGroup = group;
        this.clusterId = group.newClusterId();
    }

    @Override
    public void handleRecvEvent(XRpcMessage request) {
        try {
            serverGroup.invoke(this, request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getClusterId() {
        return clusterId;
    }

}
