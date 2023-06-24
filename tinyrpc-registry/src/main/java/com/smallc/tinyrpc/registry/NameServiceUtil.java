package com.smallc.tinyrpc.registry;

import com.smallc.tinyrpc.common.spi.ServiceLoader;

import java.net.URI;
import java.util.Collection;

/**
 * 注册中心工具
 *
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.registry
 */
public class NameServiceUtil {

    private static Collection<NameService> nameServices;

    static {
        nameServices = ServiceLoader.loadAll(NameService.class);
    }

    /**
     * 获取注册中心的引用
     *
     * @param nameServiceUri 注册中心URI
     * @return 注册中心引用
     */
    public static NameService getNameService(URI nameServiceUri) {
        for (NameService nameService : nameServices) {
            if (nameService.supportedSchemes().contains(nameServiceUri.getScheme())) {
                nameService.connect(nameServiceUri);
                return nameService;
            }
        }
        return null;
    }

}
