package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaCqProcessor;
import com.ibm.disni.util.NativeAffinity;
import com.ibm.disni.verbs.IbvContext;
import com.ibm.disni.verbs.IbvWC;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/18
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class XRpcCqProcessor<E extends XRpcEndpoint> extends RdmaCqProcessor<E> {

    private static final Logger logger = LoggerFactory.getLogger(XRpcCqProcessor.class);

    private LinkedBlockingQueue<SendTask> pendingSendTask;

    public XRpcCqProcessor(IbvContext context, int cqSize, int wrSize, long affinity, int clusterId, int timeout, boolean polling) throws IOException {
        super(context, cqSize, wrSize, affinity, clusterId, timeout, polling);
        this.pendingSendTask = new LinkedBlockingQueue<>();
    }

    public synchronized void addTask(SendTask task) {
        pendingSendTask.add(task);
    }

    @Override
    public void dispatchCqEvent(E e, IbvWC ibvWC) throws IOException {
        e.dispatchCqEvent(ibvWC);
    }

    public static class SendTask {
        private XRpcEndpoint endpoint;
        private XRpcMessage message;

        public SendTask(XRpcEndpoint endpoint, XRpcMessage message) {
            this.endpoint = endpoint;
            this.message = message;
        }
    }

}
