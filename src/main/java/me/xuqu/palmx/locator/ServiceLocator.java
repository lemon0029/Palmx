package me.xuqu.palmx.locator;

public interface ServiceLocator {

    /**
     * 根据接口类型获取一个实现类实例
     *
     * @param clazz 接口类
     * @param <T>   接口类型
     * @return 接口实现类的实例
     */
    <T> T lookup(Class<T> clazz);
}
