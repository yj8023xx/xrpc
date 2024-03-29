package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaEndpointFactory;
import com.ibm.disni.verbs.RdmaCmId;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.RequestHandler;
import com.smallc.xrpc.network.transport.RequestHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/11
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class XRpcServerGroup extends XRpcEndpointGroup<XRpcServerEndpoint> {

    private static final Logger logger = LoggerFactory.getLogger(XRpcServerGroup.class);

    private XRpcResourceManager resourceManager;
    private RequestHandlerRegistry requestHandlerRegistry;

    private XRpcServerGroup(int timeout, int clusterCount, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        super(timeout, clusterCount);
        this.resourceManager = new XRpcResourceManager(timeout, clusterCount);
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    public static XRpcServerGroup createServerGroup(int timeout, int clusterCount, RequestHandlerRegistry requestHandlerRegistry) throws IOException {
        XRpcServerGroup group = new XRpcServerGroup(timeout, clusterCount, requestHandlerRegistry);
        group.init(new XRpcServerFactory(group));
        return group;
    }

    @Override
    public void allocateResources(XRpcServerEndpoint endPoint) throws Exception {
        // Avoid blocking the main thread
        resourceManager.allocateResources(endPoint);
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

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        super.close();
        this.resourceManager.close();
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
