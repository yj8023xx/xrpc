package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaCqProcessor;
import com.ibm.disni.RdmaCqProvider;
import com.ibm.disni.RdmaEndpointGroup;
import com.ibm.disni.verbs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/11
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public abstract class XRpcEndpointGroup<E extends XRpcEndpoint> extends RdmaEndpointGroup<E> {

    private static final Logger logger = LoggerFactory.getLogger(XRpcEndpointGroup.class);

    private int clusterCount;
    private int curCluster;
    private long affinities[];
    private volatile RdmaCqProcessor[] cqProcessors;

    private int maxSendWr = 100;
    private int maxRecvWr = 150;
    private int maxSendSge = 1;
    private int maxRecvSge = 1;
    private int maxInlineData = 64;
    private int bufferSize = 128;

    public XRpcEndpointGroup(int timeout) throws IOException {
        this(timeout, 1);
    }

    public XRpcEndpointGroup(int timeout, int threadCount) throws IOException {
        super(timeout);
        this.clusterCount = threadCount;
        this.curCluster = 0;
        this.affinities = new long[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.affinities[i] = 1 << i;
        }
    }

    public XRpcEndpointGroup(int timeout, long[] affinities) throws IOException {
        super(timeout);
        this.clusterCount = affinities.length;
        this.curCluster = 0;
        this.affinities = affinities;
    }

    protected synchronized IbvQP createQp(RdmaCmId id, IbvPd pd, IbvCQ cq) throws IOException {
        IbvQPInitAttr attr = new IbvQPInitAttr();
        attr.cap().setMax_send_wr(maxSendWr);
        attr.cap().setMax_recv_wr(maxRecvWr);
        attr.cap().setMax_send_sge(maxSendSge);
        attr.cap().setMax_recv_sge(maxRecvSge);
        attr.cap().setMax_inline_data(maxInlineData);
        attr.setQp_type(IbvQP.IBV_QPT_RC);
        attr.setRecv_cq(cq);
        attr.setSend_cq(cq);
        IbvQP qp = id.createQP(pd, attr);
        return qp;
    }

    @Override
    public RdmaCqProvider createCqProvider(E endPoint) throws IOException {
        IbvContext context = endPoint.getIdPriv().getVerbs();
        if (null == context) {
            throw new IOException("No context found!");
        }
        if (null == cqProcessors) {
            createCqProcessors(context);
        }
        return cqProcessors[endPoint.getClusterId()];
    }

    // TODO: Consider the situation of multiple devices
    private synchronized void createCqProcessors(IbvContext context) throws IOException {
        if (null == cqProcessors) {
            cqProcessors = new RdmaCqProcessor[clusterCount];
            int cqSize = maxSendWr + maxRecvWr;
            for (int i = 0; i < clusterCount; i++) {
                cqProcessors[i] = new XRpcCqProcessor(context, cqSize, cqSize, affinities[i], i, 1000, true);
                cqProcessors[i].start();
            }
        }
    }

    @Override
    public IbvQP createQpProvider(E endPoint) throws IOException {
        RdmaCqProcessor cqProcessor = (RdmaCqProcessor) endPoint.getCqProvider();
        if (null == cqProcessor) {
            logger.error("CqProcessor is null.");
        }
        IbvCQ cq = cqProcessor.getCQ();
        IbvQP qp = this.createQp(endPoint.getIdPriv(), endPoint.getPd(), cq);
        cqProcessor.registerQP(qp.getQp_num(), endPoint);
        return qp;
    }

    protected synchronized int newClusterId() {
        int newClusterId = curCluster;
        curCluster = (curCluster + 1) % clusterCount;
        return newClusterId;
    }

    public XRpcEndpointGroup option(RdmaOption option, int value) {
        switch (option) {
            case MAX_SEND_WR:
                maxSendWr = value;
                break;
            case MAX_RECV_WR:
                maxRecvWr = value;
                break;
            case MAX_SEND_SGE:
                maxSendSge = value;
                break;
            case MAX_RECV_SGE:
                maxRecvSge = value;
                break;
            case BUFFER_SIZE:
                bufferSize = value;
                break;
        }
        return this;
    }

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        super.close();
        for (RdmaCqProcessor processor : cqProcessors) {
            processor.close();
        }
    }

    public int getMaxSendWr() {
        return maxSendWr;
    }

    public int getMaxRecvWr() {
        return maxRecvWr;
    }

    public int getMaxSendSge() {
        return maxSendSge;
    }

    public int getMaxRecvSge() {
        return maxRecvSge;
    }

    public int getMaxInlineData() {
        return maxInlineData;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
