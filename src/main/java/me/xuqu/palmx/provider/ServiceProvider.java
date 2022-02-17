package me.xuqu.palmx.provider;

public interface ServiceProvider {

    /**
     * 往容器中添加一个服务实现类实例
     *
     * @param serviceName 服务名
     * @param service     服务实现类的实例
     */
    void addService(String serviceName, Object service);

    /**
     * 根据服务名获取对应的实例
     *
     * @param serviceName 服务名
     * @return 实例
     */
    Object getService(String serviceName);
}
