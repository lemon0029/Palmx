package me.xuqu.palmx.spring;

import me.xuqu.palmx.net.PalmxServer;
import me.xuqu.palmx.net.netty.NettyServer;
import org.springframework.beans.factory.FactoryBean;

public class PalmxServerFactoryBean implements FactoryBean<PalmxServer> {

    @Override
    public PalmxServer getObject() {
        PalmxServer server;
        server = new NettyServer();
        new Thread(server::start, "palmx-server").start();
        return server;
    }

    @Override
    public Class<?> getObjectType() {
        return PalmxServer.class;
    }
}
