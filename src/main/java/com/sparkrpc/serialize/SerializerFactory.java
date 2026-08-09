package com.sparkrpc.serialize;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class SerializerFactory {
    private static final Map<Byte, Serializer> CODE_MAP = new ConcurrentHashMap<>();
    private static final Serializer DEFAULT = new JsonSerializer();

    static {
        CODE_MAP.put(DEFAULT.code(), DEFAULT);
        ServiceLoader.load(Serializer.class).forEach(s -> CODE_MAP.put(s.code(), s));
    }

    private SerializerFactory() {
    }

    public static Serializer getDefault() {
        return DEFAULT;
    }

    public static Serializer get(byte code) {
        Serializer serializer = CODE_MAP.get(code);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer code: " + code);
        }
        return serializer;
    }
}
