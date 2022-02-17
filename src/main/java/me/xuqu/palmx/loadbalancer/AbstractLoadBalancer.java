package me.xuqu.palmx.loadbalancer;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public InetSocketAddress choose(List<InetSocketAddress> socketAddressList, String serviceName) {
        if (socketAddressList == null || socketAddressList.size() == 0) {
            log.warn("No servers available for service: " + serviceName);
            return null;
        }

        if (socketAddressList.size() == 1) {
            return socketAddressList.get(0);
        }

        InetSocketAddress inetSocketAddress = doChoose(socketAddressList, serviceName);
        log.debug("Choose a server[{}] for service[name = {}] with services = {}", inetSocketAddress, serviceName, socketAddressList);

        return inetSocketAddress;
    }

    protected abstract InetSocketAddress doChoose(List<InetSocketAddress> socketAddressList, String serviceName);
}
