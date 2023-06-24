package com.smallc.tinyrpc.common.loadbalancer.impl;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.loadbalancer.LoadBalancerType;

import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer.impl
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {
    @Override
    public <T> T select(List<T> serverList) {
        return null;
    }

    @Override
    public String type() {
        return LoadBalancerType.CONSISTENT_HASH.getMessage();
    }
}
