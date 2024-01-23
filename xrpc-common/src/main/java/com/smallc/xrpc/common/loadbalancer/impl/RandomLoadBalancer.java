package com.smallc.xrpc.common.loadbalancer.impl;

import com.smallc.xrpc.common.loadbalancer.LoadBalanceType;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer.impl
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    private List<T> serverList;

    public RandomLoadBalancer(List<T> serverList) {
        this.serverList = serverList;
    }

    @Override
    public T select() {
        Random selector = new Random();
        int next = selector.nextInt(serverList.size());
        return serverList.get(next);
    }

    @Override
    public void update(List<T> serverList) {
        this.serverList = serverList;
    }

    @Override
    public LoadBalanceType type() {
        return LoadBalanceType.RANDOM;
    }

}
