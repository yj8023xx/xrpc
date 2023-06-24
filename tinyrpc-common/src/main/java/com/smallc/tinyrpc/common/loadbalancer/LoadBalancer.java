package com.smallc.tinyrpc.common.loadbalancer;

import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer
 */
public interface LoadBalancer {

    public <T> T select(List<T> serverList);

    public String type();

}
