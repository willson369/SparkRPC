package com.sparkrpc.client;

import com.sparkrpc.codec.RpcDecoder;
import com.sparkrpc.codec.RpcEncoder;
import com.sparkrpc.common.RpcRequest;
import com.sparkrpc.common.RpcResponse;
import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.RpcMessage;
import com.sparkrpc.serialize.JsonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class RpcClient implements AutoCloseable {
    private final String host;
    private final int port;
    private final long timeoutMs;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final AtomicLong requestIdGen = new AtomicLong(1);
    private volatile Channel channel;
    private volatile RpcClientHandler handler;

    public RpcClient(String host, int port) {
        this(host, port, 5000);
    }

    public RpcClient(String host, int port, long timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    public synchronized void connect() throws InterruptedException {
        if (channel != null && channel.isActive()) {
            return;
        }
        handler = new RpcClientHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS))
                                .addLast(new RpcDecoder())
                                .addLast(new RpcEncoder())
                                .addLast(handler);
                    }
                });
        channel = bootstrap.connect(host, port).sync().channel();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        connect();
        long requestId = requestIdGen.getAndIncrement();
        request.setRequestId(String.valueOf(requestId));

        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        handler.register(requestId, future);

        RpcMessage message = new RpcMessage();
        message.setMessageType(MessageType.REQUEST.getCode());
        message.setSerializeType(JsonSerializer.CODE);
        message.setRequestId(requestId);
        message.setData(request);

        channel.writeAndFlush(message);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            handler.remove(requestId);
            throw new TimeoutException("RPC timeout after " + timeoutMs + "ms");
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}
