package com.smallc.tinyrpc.registry;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.registry
 */

/**
 * Map
 *  String    服务名
 *  List<URI> 服务提供者URI列表
 */
public class Metadata extends HashMap<String, List<URI>> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metadata:").append("\n");
        for (Entry<String, List<URI>> entry : entrySet()) {
            sb.append("\t").append("Classname: ")
                    .append(entry.getKey()).append("\n");
            sb.append("\t").append("URIs:").append("\n");
            for (URI uri : entry.getValue()) {
                sb.append("\t\t").append(uri).append("\n");
            }
        }
        return sb.toString();
    }

}
