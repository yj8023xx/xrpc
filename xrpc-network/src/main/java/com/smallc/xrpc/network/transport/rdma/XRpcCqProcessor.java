package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaCqProcessor;
import com.ibm.disni.verbs.IbvContext;
import com.ibm.disni.verbs.IbvWC;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/18
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class XRpcCqProcessor<E extends XRpcEndpoint> extends RdmaCqProcessor<E> {
    public XRpcCqProcessor(IbvContext context, int cqSize, int wrSize, long affinity, int clusterId, int timeout, boolean polling) throws IOException {
        super(context, cqSize, wrSize, affinity, clusterId, timeout, polling);
    }

    @Override
    public void dispatchCqEvent(E e, IbvWC ibvWC) throws IOException {
        e.dispatchCqEvent(ibvWC);
    }
}
