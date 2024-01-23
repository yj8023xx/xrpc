package com.smallc.xrpc.common.loadbalancer;

import com.smallc.xrpc.common.loadbalancer.impl.RandomLoadBalancer;
import com.smallc.xrpc.common.loadbalancer.impl.RoundRobinLoadBalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer
 */
public class LoadBalancerFactory {

    private static Map<LoadBalanceType, LoadBalancer> loadBalancerMap = new HashMap<>();

//    static {
//        Collection<LoadBalancer> loadBalancers = ServiceLoader.loadAll(LoadBalancer.class);
//        for (LoadBalancer loadBalancer : loadBalancers) {
//            loadBalancerMap.put(loadBalancer.type(), loadBalancer);
//        }
//    }

//    public static LoadBalancer getDefaultLoadBalancer() {
//        return loadBalancerMap.get(LoadBalanceType.RANDOM);
//    }

    public static LoadBalancer getLoadBalancer(LoadBalanceType type, List serverList) {
        switch (type) {
            case RANDOM:
                return new RandomLoadBalancer(serverList);
            case ROUND_ROBIN:
                return new RoundRobinLoadBalancer(serverList);
        }
        return null;
    }

}
