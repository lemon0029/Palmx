package me.xuqu.palmx.net.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.net.AbstractPalmxServer;

@Slf4j
public class NettyServer extends AbstractPalmxServer {

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerSocketChannel serverSocketChannel;

    public NettyServer() {
    }

    public NettyServer(int port) {
        super(port);
    }

    @Override
    protected void doStart() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                log.info("Accept a new connection{}", socketChannel);
                ChannelPipeline pipeline = socketChannel.pipeline();

                // 通信协议数据包解码器
                pipeline.addLast(new ProtocolFrameDecoder());

                pipeline.addLast(new MessageCodec());

                pipeline.addLast(new RpcInvocationHandler());
                pipeline.addLast(new IdleStateHandler(0, 5, 0));
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                super.channelInactive(ctx);
            }
        });

        try {
            ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
            serverSocketChannel = (ServerSocketChannel) channelFuture.channel();
            log.info("Netty server{} has been started", serverSocketChannel);
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doShutdown() {
        serverSocketChannel.close().syncUninterruptibly();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
