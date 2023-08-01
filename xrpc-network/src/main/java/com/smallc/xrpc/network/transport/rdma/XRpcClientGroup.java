package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaCqProvider;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.IbvCQ;
import com.ibm.disni.verbs.IbvQP;
import com.ibm.disni.verbs.RdmaCmId;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/11
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcClientGroup extends XRpcEndpointGroup<XRpcClientEndpoint> {

    private XRpcClientGroup(int timeout) throws IOException {
        super(timeout);
    }

    public static XRpcClientGroup createClientGroup(int timeout) throws IOException {
        XRpcClientGroup group = new XRpcClientGroup(timeout);
        group.init(new XRpcClientFactory(group));
        return group;
    }

    @Override
    public RdmaCqProvider createCqProvider(XRpcClientEndpoint endPoint) throws IOException {
        return new RdmaCqProvider(endPoint.getIdPriv().getVerbs(), getMaxSendWr() + getMaxRecvWr());
    }

    @Override
    public IbvQP createQpProvider(XRpcClientEndpoint endPoint) throws IOException {
        RdmaCqProvider cqProvider = endPoint.getCqProvider();
        IbvCQ cq = cqProvider.getCQ();
        IbvQP qp = this.createQp(endPoint.getIdPriv(), endPoint.getPd(), cq);
        return qp;
    }

    @Override
    public void allocateResources(XRpcClientEndpoint endPoint) throws Exception {
        endPoint.allocateResources();
    }

    @Override
    public XRpcClientGroup option(RdmaOption option, int value) {
        return (XRpcClientGroup) super.option(option, value);
    }

    public static class XRpcClientFactory implements RdmaEndpointFactory<XRpcClientEndpoint> {
        private XRpcClientGroup group;

        public XRpcClientFactory(XRpcClientGroup group) {
            this.group = group;
        }

        @Override
        public XRpcClientEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
            return new XRpcClientEndpoint(group, id, serverSide);
        }
    }

}
