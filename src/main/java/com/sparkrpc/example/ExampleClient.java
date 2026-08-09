package com.sparkrpc.example;

import com.sparkrpc.client.RpcClient;
import com.sparkrpc.client.RpcProxy;

public class ExampleClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9090;
        try (RpcClient client = new RpcClient(host, port)) {
            HelloService hello = RpcProxy.create(HelloService.class, client);
            System.out.println(hello.hello("Spark"));
            System.out.println("1+2=" + hello.add(1, 2));
        }
    }
}
