package me.xuqu.palmx.loadbalancer.impl;

import me.xuqu.palmx.loadbalancer.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected InetSocketAddress doChoose(List<InetSocketAddress> socketAddressList, String serviceName) {
        int size = socketAddressList.size();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return socketAddressList.get(random.nextInt(size));
    }

}
