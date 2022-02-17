package me.xuqu.palmx.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于操作 Zookeeper 的工具类
 */
@Slf4j
@UtilityClass
public class CuratorUtils {

    /**
     * Zookeeper Client
     */
    private CuratorFramework curatorFramework;

    /**
     * 判断节点是否存在
     *
     * @param serviceName    服务名
     * @param ServiceAddress 服务的地址
     * @return 是否存在布尔值
     */
    public boolean existsNode(String serviceName, String ServiceAddress) {
        try {
            return getClient().checkExists().forPath(buildNodePath(serviceName, ServiceAddress)) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据服务名和地址创建一个结点
     *
     * @param serviceName    服务名
     * @param ServiceAddress 服务的地址
     * @param createMode     创建结点的模式
     */
    public void createNode(String serviceName, String ServiceAddress, CreateMode createMode) {
        // 节点的绝对路径，比如 /palmx/me.xuqu.service.FooService/192.168.13.13:8080
        String nodePath = buildNodePath(serviceName, ServiceAddress);

        try {
            // 若当前要创建的节点已经存在了则直接返回
            if (existsNode(serviceName, ServiceAddress)) {
                log.warn("Node exists for {}", nodePath);
                return;
            }

            // 创建节点
            getClient().create()
                    .creatingParentsIfNeeded()
                    .withMode(createMode)
                    .forPath(nodePath);

            log.debug("Create node[{}], path = {} success", createMode, nodePath);

        } catch (Exception e) {
            log.error("Create node[{}], path = {} failed {}", createMode, nodePath, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建临时的节点
     */
    public void createEphemeralNode(String serviceName, String ServiceAddress) {
        createNode(serviceName, ServiceAddress, CreateMode.EPHEMERAL);
    }

    /**
     * 永久性的创建节点
     */
    public void createPersistentNode(String serviceName, String ServiceAddress) {
        createNode(serviceName, ServiceAddress, CreateMode.PERSISTENT);
    }

    /**
     * 根据服务名和地址删除节点
     */
    public void deleteNode(String serviceName, String ServiceAddress) {
        try {
            String nodePath = buildNodePath(serviceName, ServiceAddress);
            getClient().delete().forPath(nodePath);
            log.info("Delete node, path = {}", nodePath);
        } catch (Exception e) {
            log.warn("{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据服务名获取相关节点
     *
     * @param serviceName 服务名
     * @return 节点列表
     */
    public List<String> getChildrenNodes(String serviceName) {
        // TODO 结果缓存

        try {
            String nodePath = buildNodePath(serviceName);
            List<String> services = getClient().getChildren().forPath(nodePath);
            log.debug("Get children nodes from zookeeper, path = {}, result = {}", nodePath, services);
            return services;
        } catch (Exception e) {
            if (e.getClass() == KeeperException.NoNodeException.class) {
                return Collections.emptyList();
            }
            log.error("Get children nodes failed, {}", e.getMessage());
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * 关闭当前在使用的连接
     */
    public void close() {
        if (curatorFramework != null && curatorFramework.getState() == CuratorFrameworkState.STARTED) {
            curatorFramework.close();
        }
        log.info("Zookeeper connection has been close");
    }

    /**
     * 获取 Curator Framework 对象
     *
     * @return 单例对象（不严谨）
     */
    public CuratorFramework getClient() {

        if (curatorFramework == null || curatorFramework.getState() == CuratorFrameworkState.STOPPED) {
            // 最多尝试三次
            RetryNTimes retryNTimes = new RetryNTimes(3, 3000);
            curatorFramework = CuratorFrameworkFactory.newClient(PalmxConfig.getZookeeperAddress(), retryNTimes);
            curatorFramework.start();

            log.info("Starting Curator Framework");

            try {
                if (!curatorFramework.blockUntilConnected(30, TimeUnit.SECONDS)) {
                    log.error("Zookeeper[{}, Timeout] has connect failed", PalmxConfig.getZookeeperAddress());
                    throw new RuntimeException("Timeout waiting to connect ZooKeeper");
                }

                log.info("Zookeeper[{}] has connected, {}", PalmxConfig.getZookeeperAddress(), curatorFramework.getState());

                Runtime.getRuntime().addShutdownHook(new Thread(CuratorUtils::close));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return curatorFramework;
    }

    public String buildNodePath(String serviceName) {
        String zookeeperRootNode = PalmxConfig.getZookeeperRootNode();
        String path = "%s/%s".formatted(zookeeperRootNode, serviceName);
        if (path.startsWith("/")) {
            return path;
        }

        // 把前缀 / 加上
        return "/" + path;
    }

    /**
     * 通过服务名和地址生成节点路径
     *
     * @param serviceName    服务名，如 me.xuqu.service.FooService
     * @param serviceAddress 服务地址，如 192.168.16.16:8080
     * @return 绝对路径
     */
    private String buildNodePath(String serviceName, String serviceAddress) {
        String zookeeperRootNode = PalmxConfig.getZookeeperRootNode();
        String path = "%s/%s/%s".formatted(zookeeperRootNode, serviceName, serviceAddress);
        if (path.startsWith("/")) {
            return path;
        }

        // 把前缀 / 加上
        return "/" + path;
    }
}
