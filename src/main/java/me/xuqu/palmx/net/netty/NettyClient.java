package me.xuqu.palmx.net.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.exception.RpcInvocationException;
import me.xuqu.palmx.loadbalancer.LoadBalancerHolder;
import me.xuqu.palmx.net.AbstractPalmxClient;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.net.RpcMessage;
import me.xuqu.palmx.registry.ServiceRegistry;
import me.xuqu.palmx.registry.impl.ZookeeperServiceRegistry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyClient extends AbstractPalmxClient {

    private final Map<String, SocketChannel> connections = new ConcurrentHashMap<>();

    @Override
    protected Object doSend(RpcMessage rpcMessage) {
        String serviceName = ((RpcInvocation) rpcMessage.getData()).getInterfaceName();

        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry();
        List<InetSocketAddress> socketAddresses = serviceRegistry.lookup(serviceName);

        // load balance
        InetSocketAddress socketAddress = LoadBalancerHolder.get().choose(socketAddresses, serviceName);

        SocketChannel channel = getConnection(socketAddress);

        channel.writeAndFlush(rpcMessage);

        // 准备一个 Promise，并将其加入到 RpcResponsePacketHandler 的集合中，以该请求的序列化为键
        DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
        RpcResponseHandler.map.put(rpcMessage.getSequenceId(), promise);

        try {
            // 同步等待结果
            promise.await();

            // 取出结果
            if (promise.isSuccess()) {
                Object result = promise.getNow();
                log.debug("Send a packet[{}], get result = {}", rpcMessage, result);
                return result;
            } else {
                log.warn("Method invocation failed, with exception");
                throw ((RpcInvocationException) promise.cause());
            }
        } catch (InterruptedException e) {
            // 远程调用的过程出现了异常
            log.error("Remote call exception", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个连接
     *
     * @param socketAddress 远程服务地址
     * @return 可复用的连接
     */
    private SocketChannel getConnection(InetSocketAddress socketAddress) {
        String socketAddressKey = socketAddress.toString();
        SocketChannel channel = connections.get(socketAddressKey);
        if (channel == null) {
            synchronized (NettyClient.class) {
                channel = newConnection(socketAddress);
                connections.put(socketAddressKey, channel);
            }
        }

        return channel;
    }

    private SocketChannel newConnection(SocketAddress socketAddress) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new ProtocolFrameDecoder());
                pipeline.addLast(new MessageCodec());
                pipeline.addLast(new RpcResponseHandler());
                pipeline.addLast(new IdleStateHandler(0, 5, 0));
            }
        });

        Channel channel = bootstrap.connect(socketAddress).syncUninterruptibly().channel();
        log.info("Channel{} has been connected", channel);

        channel.closeFuture().addListener((ChannelFutureListener)
                channelFuture -> {
                    eventLoopGroup.shutdownGracefully();
                    connections.values().removeIf(next -> next == channel);
                });

        return (SocketChannel) channel;
    }
}
