package me.xuqu.palmx.locator;

import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConfig;
import me.xuqu.palmx.exception.RpcInvocationException;
import me.xuqu.palmx.net.PalmxClient;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.net.netty.NettyClient;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * 使用动态代理技术创建代理对象来远程调用相关的服务
 */
@Slf4j
public class DefaultServiceLocator implements ServiceLocator {

    private final static PalmxClient CLIENT = new NettyClient();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        T proxyObject = (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz}, (proxy, method, args) -> {
            for (int i = 0; i < 3; i++) {
                RpcInvocation rpcInvocation = new RpcInvocation();
                rpcInvocation.setInterfaceName(clazz.getName());
                rpcInvocation.setMethodName(method.getName());
                rpcInvocation.setParameterTypes(method.getParameterTypes());
                rpcInvocation.setArguments(args);
                try {
                    return CLIENT.sendAndExpect(rpcInvocation);
                } catch (Exception e) {
                    log.info(e.getMessage());
                    TimeUnit.SECONDS.sleep(2);
                }
            }

            throw new RpcInvocationException("Remote call exception");
        });

        log.info("RPC Client proxy create success, serializer is {}, load balancer is {}",
                PalmxConfig.getSerializationType(), PalmxConfig.getLoadBalanceType());

        return proxyObject;
    }
}
