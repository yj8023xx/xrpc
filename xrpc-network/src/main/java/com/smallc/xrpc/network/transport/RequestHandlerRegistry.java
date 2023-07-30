package com.smallc.xrpc.network.transport;

import com.smallc.xrpc.common.spi.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.handler
 */
public class RequestHandlerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerRegistry.class);
    private Map<Integer, RequestHandler> handlerMap = new HashMap<>();
    private volatile static RequestHandlerRegistry instance = null;

    public synchronized static RequestHandlerRegistry getInstance() {
        if (null == instance) {
            synchronized (RequestHandlerRegistry.class) {
                if (null == instance) {
                    instance = new RequestHandlerRegistry();
                }
            }
        }
        return instance;
    }

    private RequestHandlerRegistry() {
        Collection<RequestHandler> requestHandlers = ServiceLoader.loadAll(RequestHandler.class);
        for (RequestHandler requestHandler : requestHandlers) {
            handlerMap.put(requestHandler.type(), requestHandler);
            logger.info("Load request handler, type: {}, class: {}.", requestHandler.type(), requestHandler.getClass().getCanonicalName());
        }
    }

    public RequestHandler getHandler(int type) {
        return handlerMap.get(type);
    }

}
