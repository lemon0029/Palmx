package me.xuqu.palmx.net;

import java.net.InetSocketAddress;

public interface PalmxServer {

    void start();

    void shutdown();

    int getPort();

    InetSocketAddress getAddress();
}
