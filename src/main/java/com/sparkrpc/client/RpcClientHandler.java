package com.sparkrpc.client;

import com.sparkrpc.common.RpcResponse;
import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private static final Logger log = LoggerFactory.getLogger(RpcClientHandler.class);
    private final Map<Long, CompletableFuture<RpcResponse>> pending = new ConcurrentHashMap<>();

    public void register(long requestId, CompletableFuture<RpcResponse> future) {
        pending.put(requestId, future);
    }

    public void remove(long requestId) {
        pending.remove(requestId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        if (msg.getMessageType() == MessageType.HEARTBEAT.getCode()) {
            return;
        }
        RpcResponse response = (RpcResponse) msg.getData();
        CompletableFuture<RpcResponse> future = pending.remove(msg.getRequestId());
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.WRITER_IDLE) {
            RpcMessage heartbeat = new RpcMessage();
            heartbeat.setMessageType(MessageType.HEARTBEAT.getCode());
            heartbeat.setSerializeType((byte) 1);
            heartbeat.setRequestId(0L);
            ctx.writeAndFlush(heartbeat);
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client channel error", cause);
        ctx.close();
    }
}
