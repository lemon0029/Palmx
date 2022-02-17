package me.xuqu.palmx.util;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CuratorFrameworkTests {

    private static final String testServiceName = "me.xuqu.FooService";
    private static final String testServiceAddress = "192.168.12.13:8080";

    @Test
    public void connect() {
        CuratorFramework client = CuratorUtils.getClient();
        CuratorFramework client1 = CuratorUtils.getClient();

        Assertions.assertEquals(client1, client);
    }

    @Test
    public void createEphemeralNode() {
        CuratorUtils.createEphemeralNode(testServiceName, testServiceAddress);
        CuratorFramework framework = CuratorUtils.getClient();
        framework.close();

        // 连接已关闭，建立新连接校验临时结点应该被自动删除了
        Assertions.assertFalse(CuratorUtils.existsNode(testServiceName, testServiceAddress));
    }

    @Test
    public void createPersistentNode() {
        CuratorUtils.createPersistentNode(testServiceName, testServiceAddress);
        CuratorFramework framework = CuratorUtils.getClient();
        framework.close();

        // 重新连接校验前面创建的永久性节点是否存在
        Assertions.assertTrue(CuratorUtils.existsNode(testServiceName, testServiceAddress));
    }

    @Test
    public void deleteByServiceName() {
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(testServiceName);

        // 这里的 childNode 就是 serviceAddress
        for (String childNode : childrenNodes) {
            CuratorUtils.deleteNode(testServiceName, childNode);
        }

        Assertions.assertEquals(0, CuratorUtils.getChildrenNodes(testServiceName).size());
    }

    @Test
    public void findByServiceName() {

        // 测试之前先把之前的数据删除了
        deleteByServiceName();

        CuratorUtils.createEphemeralNode(testServiceName, "192.138.24.10:8080");
        CuratorUtils.createEphemeralNode(testServiceName, "192.138.24.20:8080");
        CuratorUtils.createEphemeralNode(testServiceName, "192.138.24.30:8080");

        List<String> childrenNodes = CuratorUtils.getChildrenNodes(testServiceName);

        Assertions.assertEquals(3, childrenNodes.size());
    }
}
