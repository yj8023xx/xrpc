package com.smallc.tinyrpc.common.loadbalancer.impl;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.loadbalancer.LoadBalancerType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer.impl
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private AtomicInteger nextServerCounter;

    public RoundRobinLoadBalancer() {
        nextServerCounter = new AtomicInteger(0);
    }

    private int select(int modulo) {
        return nextServerCounter.incrementAndGet() % modulo;
    }

    @Override
    public <T> T select(List<T> serverList) {
        return serverList.get(select(serverList.size()));
    }

    @Override
    public String type() {
        return LoadBalancerType.ROUND_ROBIN.getMessage();
    }

}
