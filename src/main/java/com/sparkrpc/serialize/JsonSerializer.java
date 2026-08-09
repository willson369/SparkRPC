package com.sparkrpc.serialize;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer {
    public static final byte CODE = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public byte code() {
        return CODE;
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return MAPPER.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialize failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return MAPPER.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("JSON deserialize failed", e);
        }
    }
}
