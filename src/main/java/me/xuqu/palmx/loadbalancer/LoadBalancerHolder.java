package me.xuqu.palmx.loadbalancer;

import me.xuqu.palmx.common.LoadBalancerType;
import me.xuqu.palmx.common.PalmxConfig;
import me.xuqu.palmx.loadbalancer.impl.ConsistentHashLoadBalancer;
import me.xuqu.palmx.loadbalancer.impl.RandomLoadBalancer;
import me.xuqu.palmx.loadbalancer.impl.RoundRobinLoadBalancer;

public class LoadBalancerHolder {

    private static LoadBalancer loadBalancer;

    public static synchronized LoadBalancer get() {
        if (loadBalancer == null) {
            LoadBalancerType loadBalanceType = PalmxConfig.getLoadBalanceType();
            loadBalancer = switch (loadBalanceType) {
                case RANDOM -> new RandomLoadBalancer();
                case ROUND_ROBIN -> new RoundRobinLoadBalancer();
                case CONSISTENT_HASH -> new ConsistentHashLoadBalancer();
            };
        }

        return loadBalancer;
    }
}
