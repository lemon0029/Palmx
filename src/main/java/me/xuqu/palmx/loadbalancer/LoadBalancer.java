package me.xuqu.palmx.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface LoadBalancer {

    InetSocketAddress choose(List<InetSocketAddress> socketAddressList, String serviceName);

}