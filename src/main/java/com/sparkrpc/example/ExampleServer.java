package com.sparkrpc.example;

import com.sparkrpc.server.RpcServer;

public class ExampleServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9090;
        new RpcServer(port)
                .register(new HelloServiceImpl())
                .start();
    }
}
