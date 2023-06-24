package com.smallc.tinyrpc.common.loadbalancer;

import com.smallc.tinyrpc.common.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.smallc.tinyrpc.common.loadbalancer.impl.RoundRobinLoadBalancer;
import com.smallc.tinyrpc.common.spi.ServiceLoader;

import java.util.*;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer
 */
public class LoadBalancerFactory {

    private static Map<String, LoadBalancer> loadBalancerMap = new HashMap<>();

    static {
        Collection<LoadBalancer> loadBalancers = ServiceLoader.loadAll(LoadBalancer.class);
        for (LoadBalancer loadBalancer : loadBalancers) {
            loadBalancerMap.put(loadBalancer.type(), loadBalancer);
        }
    }

    public static LoadBalancer getDefaultLoadBalancer() {
        return loadBalancerMap.get(LoadBalancerType.RANDOM.getMessage());
    }

    public static LoadBalancer getLoadBalancer(String type) {
        switch (type) {
            case "random":
            case "iphash":
                return loadBalancerMap.get(type);
            case "roundrobin":
                return new RoundRobinLoadBalancer();
            case "consistenthash":
                return new ConsistentHashLoadBalancer();
        }
        return null;
    }

}
