package com.smallc.tinyrpc.common.loadbalancer.impl;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.loadbalancer.LoadBalancerType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.common.loadbalancer.impl
 */
public class IPHashLoadBalancer implements LoadBalancer {

    @Override
    public <T> T select(List<T> serverList) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return serverList.get(ip.hashCode() % serverList.size());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String type() {
        return LoadBalancerType.IP_HASH.getMessage();
    }

}
