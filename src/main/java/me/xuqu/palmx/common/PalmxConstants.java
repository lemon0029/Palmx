package me.xuqu.palmx.common;

public class PalmxConstants {

    static class PropertyKey {
        static final String PREFIX = "palmx";
        static final String SERIALIZATION_TYPE = PREFIX + ".serialization.type";
        static final String ZOOKEEPER_HOST = PREFIX + ".zookeeper.host";
        static final String ZOOKEEPER_PORT = PREFIX + ".zookeeper.port";
        static final String ZOOKEEPER_ROOT_NODE = PREFIX + ".zookeeper.root-node";
        static final String PALMX_SERVER_PORT = PREFIX + ".server.port";
        static final String LOAD_BALANCE_TYPE = PREFIX + ".load-balancer";
    }

    public static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:8121";
    public static final String DEFAULT_ZOOKEEPER_ROOT_NODE = "palmx";

    public static final int DEFAULT_PALMX_SERVER_PORT = 8080;

    public static final int NETTY_MESSAGE_HEADER_LENGTH = 16;
    public static final int NETTY_MESSAGE_LENGTH_FIELD_LENGTH = 4;
    public static final int NETTY_MAX_FRAME_LENGTH = 4096;

    public static final int NETTY_MESSAGE_MAGIC_NUMBER = 0x1A1A;
    public static final byte NETTY_MESSAGE_VERSION = 1;

    public static final byte NETTY_RPC_INVOCATION_MESSAGE = 0;
    public static final byte NETTY_RPC_RESPONSE_MESSAGE = 1;

    public static final byte NETTY_RPC_RESPONSE_STATUS_OK = 1;
    public static final byte NETTY_RPC_RESPONSE_STATUS_ERROR = -1;
}
