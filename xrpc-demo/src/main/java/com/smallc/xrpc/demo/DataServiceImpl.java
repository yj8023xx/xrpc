package com.smallc.xrpc.demo;

import com.smallc.xrpc.api.hello.DataService;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/16
 * @since com.smallc.xrpc.network.transport.TransportServer.demo
 */
public class DataServiceImpl implements DataService {

    @Override
    public String send(String data) {
        return new byte[1].toString();
    }

}
