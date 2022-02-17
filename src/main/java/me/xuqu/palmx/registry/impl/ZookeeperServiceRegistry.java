package me.xuqu.palmx.registry.impl;

import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.registry.AbstractServiceRegistry;
import me.xuqu.palmx.util.CuratorUtils;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZookeeperServiceRegistry extends AbstractServiceRegistry {

    @Override
    protected void doRegister(String serviceName, String serviceAddress) {
        CuratorUtils.createEphemeralNode(serviceName, serviceAddress);
        log.info("Register a service[{}, {}] to zookeeper", serviceName, serviceAddress);
    }

    @Override
    public void unregister(String serviceName, InetSocketAddress inetSocketAddress) {

    }

    @Override
    protected List<String> doLookup(String serviceName) {
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(serviceName);

        log.debug("Get services[name = {}] from zookeeper, {}", serviceName, childrenNodes);
        return childrenNodes;
    }
}
