package me.xuqu.palmx.net;

import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public abstract class AbstractPalmxServer implements PalmxServer {

    protected InetSocketAddress inetSocketAddress;
    protected String host = "127.0.0.1";
    protected int port = PalmxConfig.getPalmxServerPort();

    public AbstractPalmxServer() {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public AbstractPalmxServer(int port) {
        this();
        this.port = port;
    }

    @Override
    public void start() {
        inetSocketAddress = new InetSocketAddress(host, port);
        doStart();
    }

    @Override
    public void shutdown() {
        doShutdown();
        log.info("Palmx server has been shutdown");
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public InetSocketAddress getAddress() {
        return inetSocketAddress;
    }

    protected abstract void doStart();
    protected abstract void doShutdown();
}
