package com.sparkrpc.client;

import com.sparkrpc.common.RpcRequest;
import com.sparkrpc.common.RpcResponse;

import java.lang.reflect.Proxy;

public final class RpcProxy {
    private RpcProxy() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass, RpcClient client) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    if (Object.class.equals(method.getDeclaringClass())) {
                        return switch (method.getName()) {
                            case "toString" -> "SparkRPCProxy(" + interfaceClass.getName() + ")";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> throw new UnsupportedOperationException(method.getName());
                        };
                    }
                    RpcRequest request = new RpcRequest();
                    request.setInterfaceName(interfaceClass.getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypeNames(RpcRequest.toTypeNames(method.getParameterTypes()));
                    request.setParameters(args == null ? new Object[0] : args);

                    RpcResponse response = client.send(request);
                    if (response.getError() != null) {
                        throw new RuntimeException(response.getError());
                    }
                    Object result = response.getResult();
                    Class<?> returnType = method.getReturnType();
                    if (result != null && returnType.isPrimitive()) {
                        return coercePrimitive(result, returnType);
                    }
                    return result;
                }
        );
    }

    private static Object coercePrimitive(Object value, Class<?> primitiveType) {
        if (value instanceof Number number) {
            if (primitiveType == int.class) {
                return number.intValue();
            }
            if (primitiveType == long.class) {
                return number.longValue();
            }
            if (primitiveType == double.class) {
                return number.doubleValue();
            }
            if (primitiveType == float.class) {
                return number.floatValue();
            }
            if (primitiveType == short.class) {
                return number.shortValue();
            }
            if (primitiveType == byte.class) {
                return number.byteValue();
            }
        }
        return value;
    }
}
