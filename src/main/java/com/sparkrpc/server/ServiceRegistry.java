package com.sparkrpc.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private final Map<String, Object> services = new ConcurrentHashMap<>();

    public void register(Object serviceImpl) {
        Class<?>[] interfaces = serviceImpl.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("Service must implement an interface: " + serviceImpl.getClass());
        }
        for (Class<?> iface : interfaces) {
            services.put(iface.getName(), serviceImpl);
        }
    }

    public Object get(String interfaceName) {
        return services.get(interfaceName);
    }
}
