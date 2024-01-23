package com.smallc.xrpc.network.protocol;

import java.io.Serializable;
import java.util.Map;

/**
 * RPC请求
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.request
 */
public class XRpcRequest implements Serializable {

    private String serviceName;
    private String methodName;
    private Map<String, Object> argMap;

    public XRpcRequest(String serviceName, String methodName, Map<String, Object> argMap) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.argMap = argMap;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, Object> getArgMap() {
        return argMap;
    }

    public void setArgMap(Map<String, Object> argMap) {
        this.argMap = argMap;
    }

}
