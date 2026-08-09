package com.sparkrpc.codec;

import com.sparkrpc.common.RpcRequest;
import com.sparkrpc.common.RpcResponse;
import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.ProtocolConstants;
import com.sparkrpc.protocol.RpcMessage;
import com.sparkrpc.serialize.Serializer;
import com.sparkrpc.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder() {
        super(ProtocolConstants.MAX_FRAME_LENGTH, 13, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            short magic = frame.readShort();
            if (magic != ProtocolConstants.MAGIC) {
                throw new IllegalArgumentException("Invalid magic: " + magic);
            }
            byte version = frame.readByte();
            if (version != ProtocolConstants.VERSION) {
                throw new IllegalArgumentException("Unsupported version: " + version);
            }
            byte messageType = frame.readByte();
            byte serializeType = frame.readByte();
            long requestId = frame.readLong();
            int length = frame.readInt();
            byte[] body = new byte[length];
            if (length > 0) {
                frame.readBytes(body);
            }

            RpcMessage message = new RpcMessage();
            message.setMessageType(messageType);
            message.setSerializeType(serializeType);
            message.setRequestId(requestId);

            if (messageType == MessageType.HEARTBEAT.getCode() || length == 0) {
                return message;
            }

            Serializer serializer = SerializerFactory.get(serializeType);
            Class<?> clazz = messageType == MessageType.REQUEST.getCode()
                    ? RpcRequest.class
                    : RpcResponse.class;
            message.setData(serializer.deserialize(body, clazz));
            return message;
        } finally {
            frame.release();
        }
    }
}
