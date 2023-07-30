package com.smallc.xrpc.common.loadbalancer.impl;

import com.smallc.xrpc.common.loadbalancer.LoadBalanceType;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer.impl
 */
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

    private AtomicInteger nextServerCounter;
    private List<T> serverList;

    public RoundRobinLoadBalancer(List<T> serverList) {
        this.nextServerCounter = new AtomicInteger(0);
        this.serverList = serverList;
    }

    private int select(int size) {
        return nextServerCounter.incrementAndGet() % size;
    }

    @Override
    public T select() {
        return serverList.get(select(serverList.size()));
    }

    @Override
    public void update(List<T> serverList) {
        this.serverList = serverList;
    }

    @Override
    public LoadBalanceType type() {
        return LoadBalanceType.ROUND_ROBIN;
    }

}
