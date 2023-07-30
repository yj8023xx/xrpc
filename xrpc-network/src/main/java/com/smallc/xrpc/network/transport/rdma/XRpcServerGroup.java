package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaActiveCqProcessor;
import com.ibm.disni.RdmaCqProcessor;
import com.ibm.disni.RdmaCqProvider;
import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.IbvCQ;
import com.ibm.disni.verbs.IbvContext;
import com.ibm.disni.verbs.IbvQP;
import com.ibm.disni.verbs.RdmaCmId;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.RequestHandler;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import com.smallc.xrpc.network.transport.netty.NettyRequestInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/11
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcServerGroup extends XRpcEndpointGroup<XRpcServerEndpoint> {

    private static final Logger logger = LoggerFactory.getLogger(NettyRequestInvocation.class);

    private int clusterCount;
    private int curCluster;
    private volatile RdmaCqProcessor[] cqProcessors;
    private long[] affinity;
    private RequestHandlerRegistry requestHandlerRegistry;

    private XRpcServerGroup(int timeout, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        super(timeout);
        this.clusterCount = 1;
        this.curCluster = 0;
        this.affinity = new long[]{0};
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    private XRpcServerGroup(int timeout, long[] affinity, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        super(timeout);
        this.clusterCount = affinity.length;
        this.curCluster = 0;
        this.affinity = affinity;
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    public static XRpcServerGroup createServerGroup(int timeout, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        XRpcServerGroup group = new XRpcServerGroup(timeout, requestHandlerRegistry);
        group.init(new XRpcServerFactory(group));
        return group;
    }

    public static XRpcServerGroup createServerGroup(int timeout, long[] affinity, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        XRpcServerGroup group = new XRpcServerGroup(timeout, affinity, requestHandlerRegistry);
        group.init(new XRpcServerFactory(group));
        return group;
    }

    @Override
    public RdmaCqProvider createCqProvider(XRpcServerEndpoint endPoint) throws IOException {
        IbvContext context = endPoint.getIdPriv().getVerbs();
        if (null == context) {
            throw new IOException("No context found!");
        }
        if (null == cqProcessors) {
            createCqProcessors(context);
        }
        return cqProcessors[endPoint.getClusterId()];
    }

    private synchronized void createCqProcessors(IbvContext context) throws IOException {
        if (null == cqProcessors) {
            cqProcessors = new RdmaCqProcessor[clusterCount];
            int cqSize = getMaxSendWr() + getMaxRecvWr();
            int wrSize = getMaxSendWr() + getMaxRecvWr();
            for (int i = 0; i < clusterCount; i++) {
                cqProcessors[i] = new RdmaActiveCqProcessor(context, cqSize, wrSize, affinity[i], i, 1000, true);
                cqProcessors[i].start();
            }
        }
    }

    @Override
    public IbvQP createQpProvider(XRpcServerEndpoint endPoint) throws IOException {
        IbvCQ cq = endPoint.getCqProvider().getCQ();
        IbvQP qp = this.createQp(endPoint.getIdPriv(), endPoint.getPd(), cq);
        cqProcessors[endPoint.getClusterId()].registerQP(qp.getQp_num(), endPoint);
        return qp;
    }

    @Override
    public void allocateResources(XRpcServerEndpoint endPoint) throws Exception {
        endPoint.allocateResources();
    }

    synchronized int newClusterId() {
        int newClusterId = curCluster;
        curCluster = (curCluster + 1) % clusterCount;
        return newClusterId;
    }

    @Override
    public XRpcServerGroup option(RdmaOption option, int value) {
        return (XRpcServerGroup) super.option(option, value);
    }

    public void invoke(XRpcServerEndpoint endpoint, XRpcMessage request) throws Exception {
        RequestHandler<XRpcMessage> handler = requestHandlerRegistry.getHandler(request.getHeader().getMessageTypeId());
        if (null != handler) {
            XRpcMessage response = handler.handle(request);
            if (null != response) {
                try {
                    endpoint.send(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.warn("Response is null!");
            }
        } else {
            throw new Exception(String.format("No handler for request with type: %d!", request.getHeader().getMessageTypeId()));
        }
    }

    public static class XRpcServerFactory implements RdmaEndpointFactory<XRpcServerEndpoint> {
        private XRpcServerGroup group;

        public XRpcServerFactory(XRpcServerGroup group) {
            this.group = group;
        }

        @Override
        public XRpcServerEndpoint createEndpoint(RdmaCmId id, boolean serverSide) throws IOException {
            return new XRpcServerEndpoint(group, id, serverSide);
        }
    }

}
