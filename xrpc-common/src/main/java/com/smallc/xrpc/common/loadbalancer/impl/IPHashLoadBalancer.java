package com.smallc.xrpc.common.loadbalancer.impl;

import com.smallc.xrpc.common.loadbalancer.LoadBalanceType;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer.impl
 */
public class IPHashLoadBalancer<T> implements LoadBalancer<T> {

    private List<T> serverList;

    @Override
    public T select() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return serverList.get(ip.hashCode() % serverList.size());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(List serverList) {
        this.serverList = serverList;
    }

    @Override
    public LoadBalanceType type() {
        return LoadBalanceType.IP_HASH;
    }

}
