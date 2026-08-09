package com.sparkrpc.common;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private String requestId;
    private Object result;
    private String error;

    public static RpcResponse success(String requestId, Object result) {
        RpcResponse response = new RpcResponse();
        response.requestId = requestId;
        response.result = result;
        return response;
    }

    public static RpcResponse fail(String requestId, String error) {
        RpcResponse response = new RpcResponse();
        response.requestId = requestId;
        response.error = error;
        return response;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
