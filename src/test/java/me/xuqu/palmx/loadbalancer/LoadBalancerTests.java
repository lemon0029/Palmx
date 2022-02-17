package me.xuqu.palmx.loadbalancer;

import me.xuqu.palmx.loadbalancer.impl.ConsistentHashLoadBalancer;
import me.xuqu.palmx.loadbalancer.impl.RandomLoadBalancer;
import me.xuqu.palmx.loadbalancer.impl.RoundRobinLoadBalancer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadBalancerTests {

    private static Map<String, List<InetSocketAddress>> serviceMap;
    private static LoadBalancer randomLoadBalancer;
    private static LoadBalancer roundRobinLoadBalancer;
    private static LoadBalancer consistentHasLoadBalancer;


    @BeforeAll
    static void init() {
        serviceMap = new HashMap<>();

        serviceMap.put("service1", List.of(
                new InetSocketAddress("10.10.10.10", 8080),
                new InetSocketAddress("10.10.10.11", 8080),
                new InetSocketAddress("10.10.10.12", 8080),
                new InetSocketAddress("10.10.10.13", 8080),
                new InetSocketAddress("10.10.10.14", 8080)
        ));

        serviceMap.put("service2", List.of(
                new InetSocketAddress("20.10.10.10", 9999),
                new InetSocketAddress("20.10.10.11", 9999),
                new InetSocketAddress("20.10.10.12", 9999)
        ));

        randomLoadBalancer = new RandomLoadBalancer();
        roundRobinLoadBalancer = new RoundRobinLoadBalancer();
        consistentHasLoadBalancer = new ConsistentHashLoadBalancer();
    }

    @Test
    public void consistentHashLoadBalance() {
        for (int i = 0; i < 10; i++) {
            consistentHasLoadBalancer.choose(serviceMap.get("service1"), "service1");
        }
    }

    @Test
    public void randomLoadBalance() {
        for (int i = 0; i < 10; i++) {
            randomLoadBalancer.choose(serviceMap.get("service1"), "service1");
        }
    }

    @Test
    public void roundRobinBalance() {
        for (int i = 0; i < 6; i++) {
            roundRobinLoadBalancer.choose(serviceMap.get("service1"), "service1");
            roundRobinLoadBalancer.choose(serviceMap.get("service2"), "service2");
        }
    }
}
