package me.xuqu.palmx.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务注册中心
 */
public interface ServiceRegistry {

    void register(String serviceName, InetSocketAddress inetSocketAddress);

    void unregister(String serviceName, InetSocketAddress inetSocketAddress);

    List<InetSocketAddress> lookup(String serviceName);

}
