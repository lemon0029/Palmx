package me.xuqu.palmx.common;

import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConstants.PropertyKey;
import me.xuqu.palmx.loadbalancer.LoadBalancer;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static me.xuqu.palmx.common.PalmxConstants.*;

@Slf4j
public class PalmxConfig {

    private static final Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = new ClassPathResource("palmx.properties").getInputStream()) {
            properties.load(inputStream);
            log.info("Load config file success: classpath:/palmx.properties");
        } catch (IOException e) {
            if (e.getClass() == FileNotFoundException.class) {
                log.warn("Cannot found config file: classpath:/palmx.properties");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static LoadBalancerType getLoadBalanceType() {
        String property = getProperty(PropertyKey.LOAD_BALANCE_TYPE);
        return switch (property) {
            case "round-robin" -> LoadBalancerType.ROUND_ROBIN;
            case "consistent-hash" -> LoadBalancerType.CONSISTENT_HASH;
            default -> LoadBalancerType.RANDOM;
        };
    }

    public static int getPalmxServerPort() {
        String property = getProperty(PropertyKey.PALMX_SERVER_PORT);
        return property == null ? DEFAULT_PALMX_SERVER_PORT : Integer.parseInt(property);
    }

    /**
     * 获取当前项目所使用的 Zookeeper 根节点
     *
     * @return 根节点，不以 / 开头，默认时 palmx
     */
    public static String getZookeeperRootNode() {
        String property = getProperty(PropertyKey.ZOOKEEPER_ROOT_NODE);
        return property == null ? DEFAULT_ZOOKEEPER_ROOT_NODE : property;
    }

    public static String getZookeeperAddress() {
        String zookeeperHost = getProperty(PropertyKey.ZOOKEEPER_HOST);
        String zookeeperPort = getProperty(PropertyKey.ZOOKEEPER_PORT);
        if (zookeeperHost != null && zookeeperPort != null) {
            return "%s:%s".formatted(zookeeperHost, zookeeperPort);
        } else {
            return DEFAULT_ZOOKEEPER_ADDRESS;
        }
    }

    /**
     * 获取配置的序列化类型，默认使用 Java 对象流实现
     *
     * @return 序列化类型
     */
    public static SerializationType getSerializationType() {
        String property = getProperty(PropertyKey.SERIALIZATION_TYPE);
        if (property != null) {
            return switch (property) {
                case "json" -> SerializationType.JSON;
                case "kryo" -> SerializationType.KRYO;
                case "protostuff" -> SerializationType.PROTOSTUFF;
                default -> SerializationType.JAVA;
            };
        }

        return SerializationType.JAVA;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
