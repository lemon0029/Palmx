package me.xuqu.palmx.loadbalancer.impl;

import me.xuqu.palmx.loadbalancer.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    private final Map<String, AtomicInteger> positions = new ConcurrentHashMap<>();

    @Override
    protected InetSocketAddress doChoose(List<InetSocketAddress> socketAddressList, String serviceName) {
        if (!positions.containsKey(serviceName)) {
            positions.put(serviceName, new AtomicInteger((new Random()).nextInt(1000)));
        }

        AtomicInteger position = positions.get(serviceName);
        int pos = Math.abs(position.incrementAndGet());
        return socketAddressList.get(pos % socketAddressList.size());
    }
}
