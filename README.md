# SparkRPC

Lightweight learning RPC based on **Java 17 + Netty 4**.

Inspired by [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) and similar Netty RPC projects.

## MVP Features

- Custom binary protocol (`Magic` + `LengthField`) for sticky / half packet handling
- Codec decoupled from business handler
- JDK dynamic proxy client + sync Future
- `@RpcService` style local service registration
- Pluggable serializer SPI (JSON shipped; Hessian later)
- Heartbeat + idle detection + connection reuse + timeout

## Quick Start

```bash
# requires JDK 17+
mvn test

# terminal 1
mvn -q exec:java -Dexec.mainClass=com.sparkrpc.example.ExampleServer

# terminal 2
mvn -q exec:java -Dexec.mainClass=com.sparkrpc.example.ExampleClient
```

## Showcase Site

- Live: https://website-sandy-pi-98.vercel.app
- Source: `website/`

```bash
cd website
vercel --prod
```

## Call Path

`Consumer → Proxy → Client Channel → Server Codec → Invoker → Provider`

## Roadmap

- [ ] Hessian serializer
- [ ] Async callback mode
- [ ] ZooKeeper registry
