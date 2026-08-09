package com.sparkrpc.protocol;

public final class ProtocolConstants {
    public static final short MAGIC = 0x5350; // "SP"
    public static final byte VERSION = 1;
    public static final int HEADER_LENGTH = 17; // magic(2)+ver(1)+type(1)+ser(1)+reqId(8)+len(4)
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    private ProtocolConstants() {
    }
}
