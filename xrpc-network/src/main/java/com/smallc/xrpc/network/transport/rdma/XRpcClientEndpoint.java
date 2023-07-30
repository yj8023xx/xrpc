package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.verbs.IbvCQ;
import com.ibm.disni.verbs.IbvWC;
import com.ibm.disni.verbs.RdmaCmId;
import com.ibm.disni.verbs.SVCPollCq;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.InFlightRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import com.smallc.xrpc.network.transport.netty.NettyResponseInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/29
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcClientEndpoint extends XRpcEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NettyResponseInvocation.class);

    private SVCPollCq poll;
    private IbvWC[] wcList;
    private InFlightRequests inFlightRequests;
    private Thread cqProcessor;

    protected XRpcClientEndpoint(XRpcEndpointGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.cqProcessor = new Thread(new CqProcessor());
    }

    @Override
    protected synchronized void init() throws IOException {
        super.init();
        IbvCQ cq = getCqProvider().getCQ();
        this.wcList = new IbvWC[getCqProvider().getCqSize()];
        for (int i = 0; i < wcList.length; i++) {
            wcList[i] = new IbvWC();
        }
        this.poll = cq.poll(wcList, wcList.length);
        this.cqProcessor.start();
    }

    @Override
    public void handleRecvEvent(XRpcMessage response) {
        ResponseFuture<XRpcMessage> future = inFlightRequests.remove(response.getHeader().getRequestId());
        if (null != future) {
            future.getFuture().complete(response);
        } else {
            logger.warn("Drop response: {}", response);
        }
    }

    public void pollOnce() throws IOException {
        int ret = poll.execute().getPolls();
        for (int i = 0; i < ret; i++) {
            IbvWC wc = wcList[i];
            dispatchCqEvent(wc);
        }
    }

    public void setInFlightRequests(InFlightRequests inFlightRequests) {
        this.inFlightRequests = inFlightRequests;
    }

    private class CqProcessor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    pollOnce();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
