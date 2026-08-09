package com.sparkrpc;

import com.sparkrpc.client.RpcClient;
import com.sparkrpc.client.RpcProxy;
import com.sparkrpc.codec.RpcDecoder;
import com.sparkrpc.codec.RpcEncoder;
import com.sparkrpc.common.RpcRequest;
import com.sparkrpc.example.HelloService;
import com.sparkrpc.example.HelloServiceImpl;
import com.sparkrpc.protocol.MessageType;
import com.sparkrpc.protocol.ProtocolConstants;
import com.sparkrpc.protocol.RpcMessage;
import com.sparkrpc.serialize.JsonSerializer;
import com.sparkrpc.server.RpcServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SparkRpcIntegrationTest {
    private int port;
    private RpcServer server;

    @BeforeEach
    void setUp() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        server = new RpcServer(port).register(new HelloServiceImpl());
        server.startAsync();
        TimeUnit.MILLISECONDS.sleep(400);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    void syncProxyCallWorks() throws Exception {
        try (RpcClient client = new RpcClient("127.0.0.1", port, 3000)) {
            HelloService hello = RpcProxy.create(HelloService.class, client);
            assertEquals("Hello, World! from SparkRPC", hello.hello("World"));
            assertEquals(5, hello.add(2, 3));
        }
    }

    @Test
    void codecRoundTripAndMagicCheck() {
        RpcRequest request = new RpcRequest();
        request.setRequestId("1");
        request.setInterfaceName(HelloService.class.getName());
        request.setMethodName("hello");
        request.setParameterTypeNames(new String[]{String.class.getName()});
        request.setParameters(new Object[]{"Spark"});

        RpcMessage message = new RpcMessage();
        message.setMessageType(MessageType.REQUEST.getCode());
        message.setSerializeType(JsonSerializer.CODE);
        message.setRequestId(42L);
        message.setData(request);

        EmbeddedChannel encodeChannel = new EmbeddedChannel(new RpcEncoder());
        encodeChannel.writeOutbound(message);
        ByteBuf encoded = encodeChannel.readOutbound();
        assertNotNull(encoded);
        assertEquals(ProtocolConstants.MAGIC, encoded.getShort(0));

        EmbeddedChannel decodeChannel = new EmbeddedChannel(new RpcDecoder());
        decodeChannel.writeInbound(encoded.retain());
        RpcMessage decoded = decodeChannel.readInbound();
        assertNotNull(decoded);
        assertEquals(42L, decoded.getRequestId());
        assertEquals(MessageType.REQUEST.getCode(), decoded.getMessageType());
        RpcRequest body = (RpcRequest) decoded.getData();
        assertEquals("hello", body.getMethodName());
        encoded.release();

        ByteBuf bad = Unpooled.buffer();
        bad.writeShort((short) 0x0000);
        bad.writeByte(1);
        bad.writeByte(1);
        bad.writeByte(1);
        bad.writeLong(1L);
        bad.writeInt(0);
        EmbeddedChannel badChannel = new EmbeddedChannel(new RpcDecoder());
        assertThrows(Exception.class, () -> badChannel.writeInbound(bad));
    }
}
