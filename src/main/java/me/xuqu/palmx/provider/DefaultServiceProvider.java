package me.xuqu.palmx.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_CACHE = new ConcurrentHashMap<>();

    private static volatile DefaultServiceProvider defaultServiceProvider;

    /**
     * 获取单例对象，一个进程只需要一个服务容器
     *
     * @return 服务容器实现
     */
    public static DefaultServiceProvider getInstance() {
        if (defaultServiceProvider == null) {
            synchronized (DefaultServiceProvider.class) {
                if (defaultServiceProvider == null) {
                    defaultServiceProvider = new DefaultServiceProvider();
                }
            }
        }

        return defaultServiceProvider;
    }

    @Override
    public void addService(String serviceName, Object service) {
        SERVICE_CACHE.put(serviceName, service);
    }

    @Override
    public Object getService(String serviceName) {
        return SERVICE_CACHE.get(serviceName);
    }
}
