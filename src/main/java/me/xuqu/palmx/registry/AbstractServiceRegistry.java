package me.xuqu.palmx.registry;

import me.xuqu.palmx.exception.ServiceNotFoundException;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class AbstractServiceRegistry implements ServiceRegistry {

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        String serviceAddress = "%s:%d".formatted(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        doRegister(serviceName, serviceAddress);
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        List<String> serviceAddresses = doLookup(serviceName);

        List<InetSocketAddress> inetSocketAddresses = serviceAddresses.stream().map(s -> {
            String[] strings = s.split(":");
            return new InetSocketAddress(strings[0], Integer.parseInt(strings[1]));
        }).toList();

        if (inetSocketAddresses.size() == 0) {
            throw new ServiceNotFoundException(serviceName);
        }

        return inetSocketAddresses;
    }

    protected abstract void doRegister(String serviceName, String serviceAddress);

    protected abstract List<String> doLookup(String serviceName);
}
