package com.smallc.xrpc.common.loadbalancer;

import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.common.loadbalancer
 */
public interface LoadBalancer<T> {

    T select();

    void update(List<T> serverList);

    LoadBalanceType type();

}
