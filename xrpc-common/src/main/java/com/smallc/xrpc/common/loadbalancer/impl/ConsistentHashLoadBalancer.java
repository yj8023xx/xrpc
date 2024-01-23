package com.smallc.xrpc.common.loadbalancer.impl;

import com.smallc.xrpc.common.loadbalancer.LoadBalanceType;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;

import java.util.List;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer.impl
 */
public class ConsistentHashLoadBalancer<T> implements LoadBalancer<T> {
    @Override
    public T select() {
        return null;
    }

    @Override
    public void update(List<T> serverList) {

    }

    @Override
    public LoadBalanceType type() {
        return LoadBalanceType.CONSISTENT_HASH;
    }

}
