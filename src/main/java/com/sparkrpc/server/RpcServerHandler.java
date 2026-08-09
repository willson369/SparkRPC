package com.sparkrpc.server;

import com.sparkrpc.common.RpcRequest;
import com.sparkrpc.common.RpcResponse;
import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.RpcMessage;
import com.sparkrpc.serialize.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private static final Logger log = LoggerFactory.getLogger(RpcServerHandler.class);
    private final ServiceRegistry registry;

    public RpcServerHandler(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        if (msg.getMessageType() == MessageType.HEARTBEAT.getCode()) {
            RpcMessage pong = new RpcMessage();
            pong.setMessageType(MessageType.HEARTBEAT.getCode());
            pong.setSerializeType(JsonSerializer.CODE);
            pong.setRequestId(msg.getRequestId());
            ctx.writeAndFlush(pong);
            return;
        }

        RpcRequest request = (RpcRequest) msg.getData();
        RpcResponse response;
        try {
            Object service = registry.get(request.getInterfaceName());
            if (service == null) {
                response = RpcResponse.fail(request.getRequestId(),
                        "Service not found: " + request.getInterfaceName());
            } else {
                Class<?>[] types = request.resolveParameterTypes();
                Method method = service.getClass().getMethod(request.getMethodName(), types);
                Object[] args = normalizeArgs(request.getParameters(), types);
                Object result = method.invoke(service, args);
                response = RpcResponse.success(request.getRequestId(), result);
            }
        } catch (Exception e) {
            log.error("Invoke failed", e);
            response = RpcResponse.fail(request.getRequestId(), e.getMessage());
        }

        RpcMessage out = new RpcMessage();
        out.setMessageType(MessageType.RESPONSE.getCode());
        out.setSerializeType(msg.getSerializeType());
        out.setRequestId(msg.getRequestId());
        out.setData(response);
        ctx.writeAndFlush(out);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.READER_IDLE) {
            log.warn("Reader idle, close channel {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server channel error", cause);
        ctx.close();
    }

    private static Object[] normalizeArgs(Object[] args, Class<?>[] types) {
        if (args == null) {
            return new Object[0];
        }
        Object[] normalized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> type = types[i];
            if (arg instanceof Number number) {
                if (type == int.class || type == Integer.class) {
                    normalized[i] = number.intValue();
                } else if (type == long.class || type == Long.class) {
                    normalized[i] = number.longValue();
                } else if (type == double.class || type == Double.class) {
                    normalized[i] = number.doubleValue();
                } else if (type == float.class || type == Float.class) {
                    normalized[i] = number.floatValue();
                } else {
                    normalized[i] = arg;
                }
            } else {
                normalized[i] = arg;
            }
        }
        return normalized;
    }
}
