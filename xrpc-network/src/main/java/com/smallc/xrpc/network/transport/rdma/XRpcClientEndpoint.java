package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.verbs.IbvCQ;
import com.ibm.disni.verbs.IbvWC;
import com.ibm.disni.verbs.RdmaCmId;
import com.ibm.disni.verbs.SVCPollCq;
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
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class XRpcClientEndpoint extends XRpcEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(XRpcClientEndpoint.class);

    private SVCPollCq poll;
    private IbvWC[] wcList;
    private PendingRequests pendingRequests;
    private CqProcessor cqProcessor;

    protected XRpcClientEndpoint(XRpcEndpointGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.cqProcessor = new CqProcessor();
    }

    public void setPendingRequests(PendingRequests pendingRequests) {
        this.pendingRequests = pendingRequests;
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
    public synchronized void close() throws IOException, InterruptedException {
        super.close();
        cqProcessor.close();
    }

    @Override
    public void handleRecvEvent(XRpcMessage response) {
        ResponseFuture<XRpcMessage> future = pendingRequests.remove(response.getHeader().getRequestId());
        if (null != future) {
            future.getFuture().complete(response);
        } else {
            logger.warn("Drop response: {}", response);
        }
    }

    private void pollOnce() throws IOException {
        int ret = poll.execute().getPolls();
        for (int i = 0; i < ret; i++) {
            IbvWC wc = wcList[i];
            dispatchCqEvent(wc);
        }
    }

    private class CqProcessor implements Runnable {
        private boolean running;
        private Thread thread;

        public CqProcessor() {
            this.running = false;
            this.thread = new Thread();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    pollOnce();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void start() {
            this.running = true;
            thread.start();
        }

        public void close() throws InterruptedException {
            this.running = false;
            thread.join();
        }
    }

}
