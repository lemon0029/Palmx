# Palmx

> Just a simple rpc-framework implementation [toy]

基于 Netty 实现的一个非常简单的 RPC 框架，使用 Zookeeper 作为服务注册中心，手动实现了几个简单的负载均衡算法（轮询、随机），然后封装了多种序列算法（Object Stream、Jackson、Kryo、protostuff），与 Spring Framework 集成，实现了基于注解驱动的服务自动注册和一键式启动服务器的功能。

## 配置文件
在类路径下添加 `palmx.properties`，可以配置 Zookeeper 相关的连接信息、序列化方式、负载均衡算法、服务器端口号，具体如下所示：

```properties
palmx.zookeeper.host=ubuntu.qaab8h9.wsl
palmx.zookeeper.port=2181
palmx.zookeeper.root-node=palmx
palmx.serialization.type=kryo
palmx.load-balancer=round-robin
palmx.server.port=8081
```

## 服务提供者

只需要编写一个配置类，添加组件扫描注解和启动服务器注解即可。
```java
@EnablePalmx
@Configuration
@ComponentScan
public class AppConfig {
}
```

在服务实现类上添加 @PalmxService 注解帮助自动扫描和注册服务
```java
@PalmxService
public class FooServiceImpl implements FooService {
    @Override
    public String sayHello(String name) {
        return "Hello, %s".formatted(name);
    }

    @Override
    public int sum(int a, int b) {
        return a + b;
    }
}
```
然后启动 Spring ApplicationContext 即可：



![image-20220217152038958](https://yec-dev.oss-cn-guangzhou.aliyuncs.com/image-20220217152038958.png)



## 服务消费者

只需要在需要使用服务时添加 @PalmxClient 注解即可，框架会自动注入该字段（由 Spring 的 BeanPostProcessor 实现）

```java
@Component
public class FooController {
    
    @PalmxClient
    public FooService fooService;

    public void test() {
        String hello = fooService.sayHello("jack");
        System.out.println(hello);
        int sum = fooService.sum(1, 2);
        System.out.println(sum);
        System.out.println(fooService.sum(12626, 4548));
    }
}
```

同样需要一个配置类扫描组件（Spring 项目）

```java
@Configuration
@ComponentScan
public class AppConfig {
}
```

![image-20220217152505604](https://yec-dev.oss-cn-guangzhou.aliyuncs.com/image-20220217152505604.png)



## 手动使用

也可以不借助 Spring Framework 来完成 RPC 服务端和客户端的编写。

```java
public static void client() {
    DefaultServiceLocator serviceLocator = new DefaultServiceLocator();
    FooService fooService = serviceLocator.lookup(FooService.class);
    System.out.println(fooService.sayHello("jack"));
    System.out.println(fooService.sum(1, 2));
    System.out.println(fooService.sum(12626, 4548));
}

public static void server() {
    // 启动一个服务器
    PalmxServer server = new NettyServer();
    new Thread(server::start, "palmx-server").start();

    // 创建单个服务的实现类实例，并将其添加到容器中管理
    String serviceName = FooService.class.getName();
    FooService fooService = new FooServiceImpl();

    DefaultServiceProvider.getInstance().addService(serviceName, fooService);

    // 将指定服务注册到 Zookeeper
    ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry();
    serviceRegistry.register(serviceName, server.getAddress());
}
```

## ServiceLocator

ServiceLocator 是通过动态代理技术实现的服务调用，内部封装了一个 Palmx 客户端去调用远程的服务。

```java
@Slf4j
public class DefaultServiceLocator implements ServiceLocator {

    private final static PalmxClient CLIENT = new NettyClient();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> clazz) {
        T proxyObject = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
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
```

