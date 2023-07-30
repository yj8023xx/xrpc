package com.smallc.xrpc.network.request;

import java.io.Serializable;

/**
 * RPC请求
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.request
 */
public class RpcRequest implements Serializable {

    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public RpcRequest(String serviceName, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
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

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
