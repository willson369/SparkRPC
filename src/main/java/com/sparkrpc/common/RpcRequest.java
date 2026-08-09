package com.sparkrpc.common;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRequest implements Serializable {
    private String requestId;
    private String interfaceName;
    private String methodName;
    private String[] parameterTypeNames;
    private Object[] parameters;

    public Class<?>[] resolveParameterTypes() throws ClassNotFoundException {
        if (parameterTypeNames == null || parameterTypeNames.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[parameterTypeNames.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            types[i] = forName(parameterTypeNames[i]);
        }
        return types;
    }

    public static String[] toTypeNames(Class<?>[] types) {
        if (types == null || types.length == 0) {
            return new String[0];
        }
        return Arrays.stream(types).map(Class::getName).toArray(String[]::new);
    }

    private static Class<?> forName(String name) throws ClassNotFoundException {
        return switch (name) {
            case "int" -> int.class;
            case "long" -> long.class;
            case "double" -> double.class;
            case "float" -> float.class;
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "char" -> char.class;
            case "void" -> void.class;
            default -> Class.forName(name);
        };
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypeNames() {
        return parameterTypeNames;
    }

    public void setParameterTypeNames(String[] parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
