package com.sparkrpc.codec;

import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.ProtocolConstants;
import com.sparkrpc.protocol.RpcMessage;
import com.sparkrpc.serialize.Serializer;
import com.sparkrpc.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) {
        out.writeShort(ProtocolConstants.MAGIC);
        out.writeByte(ProtocolConstants.VERSION);
        out.writeByte(msg.getMessageType());
        out.writeByte(msg.getSerializeType());
        out.writeLong(msg.getRequestId());

        byte[] body = new byte[0];
        if (msg.getMessageType() != MessageType.HEARTBEAT.getCode() && msg.getData() != null) {
            Serializer serializer = SerializerFactory.get(msg.getSerializeType());
            body = serializer.serialize(msg.getData());
        }
        out.writeInt(body.length);
        if (body.length > 0) {
            out.writeBytes(body);
        }
    }
}
