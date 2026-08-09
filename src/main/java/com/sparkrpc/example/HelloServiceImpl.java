package com.sparkrpc.example;

import com.sparkrpc.annotation.RpcService;

@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello, " + name + "! from SparkRPC";
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
