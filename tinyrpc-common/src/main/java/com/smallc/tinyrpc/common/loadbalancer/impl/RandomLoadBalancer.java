package com.smallc.tinyrpc.common.loadbalancer.impl;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.loadbalancer.LoadBalancerType;

import java.util.List;
import java.util.Random;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer.impl
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public <T> T select(List<T> serverList) {
        Random selector = new Random();
        int next = selector.nextInt(serverList.size());
        return serverList.get(next);
    }

    @Override
    public String type() {
        return LoadBalancerType.RANDOM.getMessage();
    }

}
