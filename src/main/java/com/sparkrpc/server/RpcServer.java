package com.sparkrpc.server;

import com.sparkrpc.codec.RpcDecoder;
import com.sparkrpc.codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RpcServer {
    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    private final int port;
    private final ServiceRegistry registry = new ServiceRegistry();
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RpcServer(int port) {
        this.port = port;
    }

    public RpcServer register(Object serviceImpl) {
        registry.register(serviceImpl);
        return this;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                .addLast(new RpcDecoder())
                                .addLast(new RpcEncoder())
                                .addLast(new RpcServerHandler(registry));
                    }
                });
        ChannelFuture future = bootstrap.bind(port).sync();
        log.info("SparkRPC server started on port {}", port);
        future.channel().closeFuture().sync();
    }

    public void startAsync() {
        Thread t = new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "sparkrpc-server");
        t.setDaemon(true);
        t.start();
    }

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
