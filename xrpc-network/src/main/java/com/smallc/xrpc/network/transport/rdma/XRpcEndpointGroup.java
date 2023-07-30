package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaEndpointGroup;
import com.ibm.disni.verbs.*;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/11
 * @since com.smallc.xrpc.network.transport.rdma
 */
public abstract class XRpcEndpointGroup<E extends XRpcEndpoint> extends RdmaEndpointGroup<E> {

    private int maxSendWr;
    private int maxRecvWr;
    private int maxSendSge;
    private int maxRecvSge;
    private int maxInlineData;
    private int bufferSize;
    private int bufferCount;

    public XRpcEndpointGroup(int timeout) throws IOException {
        this(100, 100, 10, 10, 128, 256, 10, timeout);
    }

    public XRpcEndpointGroup(int maxSendWr, int maxRecvWr, int maxSendSge, int maxRecvSge, int maxInlineData, int bufferSize, int bufferCount, int timeout) throws IOException {
        super(timeout);

        this.maxSendWr = maxSendWr;
        this.maxRecvWr = maxRecvWr;
        this.maxSendSge = maxSendSge;
        this.maxRecvSge = maxRecvSge;
        this.maxInlineData = maxInlineData;
        this.bufferSize = bufferSize;
        this.bufferCount = bufferCount;
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

    public XRpcEndpointGroup option(RdmaOption option, int value) {
        switch (option) {
            case MAX_SEND_WR:
                maxSendWr = value;
            case MAX_RECV_WR:
                maxRecvWr = value;
            case MAX_SEND_SGE:
                maxSendSge = value;
            case MAX_RECV_SGE:
                maxRecvSge = value;
            case BUFFER_SIZE:
                bufferSize = value;
            case BUFFER_COUNT:
                bufferCount = value;
        }
        return this;
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

    public int getBufferCount() {
        return bufferCount;
    }

}
