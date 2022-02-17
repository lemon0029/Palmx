package me.xuqu.palmx.spring;

import me.xuqu.palmx.locator.DefaultServiceLocator;
import me.xuqu.palmx.locator.ServiceLocator;
import me.xuqu.palmx.net.PalmxServer;
import me.xuqu.palmx.provider.DefaultServiceProvider;
import me.xuqu.palmx.registry.ServiceRegistry;
import me.xuqu.palmx.registry.impl.ZookeeperServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

@Component
public class PalmxBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private final static ServiceLocator serviceLocator = new DefaultServiceLocator();
    private final static ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry();

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NonNull String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        // 若该类被 @PalmxService 注解所标识，则将对象加入到容器并且往注册中心注册该服务
        if (aClass.isAnnotationPresent(PalmxService.class)) {
            String serviceName = aClass.getInterfaces()[0].getName();
            DefaultServiceProvider.getInstance().addService(serviceName, bean);

            // 获取单例池中的 PalmxServer 对象，然后获取当前服务的地址
            PalmxServer palmxServer = applicationContext.getBean(PalmxServer.class);
            serviceRegistry.register(serviceName, palmxServer.getAddress());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @Nonnull String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 若该字段被 @PalmxClient 注解所标识，则创建一个代理对象来注入
            if (field.isAnnotationPresent(PalmxClient.class)) {
                Object proxyObject = serviceLocator.lookup(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, proxyObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return bean;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
