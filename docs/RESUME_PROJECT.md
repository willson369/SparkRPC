# 项目经历（简历可用）

**SparkRPC**｜基于 Netty 的轻量学习向 RPC 框架  
**2025**｜独立开发  
**线上展示**：https://sparkrpc.vercel.app  
**仓库**：https://github.com/willson369/SparkRPC  

## 项目简介

从零实现一款面向学习与小规模内部通信的 RPC 框架：自定义二进制协议 + Netty 网络传输，打通「动态代理 → 编解码 → 本地调用 → 响应回写」完整调用链。配套科技极客风展示站并部署至 Vercel。

技术栈：**Java 17 / Netty 4 / Jackson(JSON) / SPI / JUnit 5 / Vercel 静态站**。借鉴 guide-rpc-framework 等开源方案，MVP 采用直连 + 同步 Future，预留 Hessian / ZooKeeper / 异步回调扩展点。

## 主要工作

1. **自定义协议帧，解决 TCP 粘包 / 半包**  
   设计 17B Header：`Magic(2) + Ver(1) + Type(1) + Ser(1) + RequestId(8) + Length(4)`。基于 `LengthFieldBasedFrameDecoder` 拆帧，并对魔数做校验，拒绝非法报文。

2. **编解码与业务 Handler 解耦**  
   `RpcEncoder` / `RpcDecoder` 独立于 `RpcServerHandler`，服务端按接口名反射路由到本地实现，捕获异常后封装 `RpcResponse` 写回；客户端以 RequestId 匹配 `CompletableFuture` 完成同步等待。

3. **JDK 动态代理封装远程调用**  
   `RpcProxy` 屏蔽网络细节，自动填充接口名、方法名、参数类型名；处理 JSON 反序列化后的数值类型与原始类型装箱差异，支持超时熔断（超时取消 pending Future）。

4. **连接保活与稳定性**  
   客户端写空闲发心跳，服务端读空闲关闭异常连接；长连接复用，避免每次调用重建 TCP。

5. **SPI 可插拔序列化**  
   `Serializer` 接口 + `ServiceLoader`，默认 JSON；为后续 Hessian / 其他序列化预留扩展位，注册中心适配层同样按可插拔方向预留。

6. **工程化交付与上线**  
   Maven 工程 + JUnit 5 集成测试（编解码往返 + 端到端同步调用）；示例 Client/Server 可一键跑通。静态展示站（协议图示 / 调用路径 / 技术栈）部署 Vercel，公网可访问。

## 可写进简历的量化 / 体感结果（建议精简选用）

| 点 | 数据或体感 |
|----|------------|
| 协议 | 自定义二进制帧 + Magic 校验，覆盖粘包 / 半包 |
| 调用 | 动态代理同步 Future，超时可控 |
| 验证 | 集成测试通过；示例输出 `Hello, Spark!` / `1+2=3` |
| 交付 | GitHub 开源仓库 + Vercel 展示站 |

## 一句话（可选放项目标题下）

独立实现 Netty 自定义协议 RPC：粘包半包处理、动态代理同步调用、心跳空闲保活与 SPI 序列化扩展，并完成展示站上线。

## 简历条目（精简版，可直接粘贴）

**SparkRPC｜Java / Netty 轻量 RPC 框架**｜独立开发｜2025  
从零设计自定义二进制协议与 Netty 编解码链路，解决 TCP 粘包 / 半包；客户端 JDK 动态代理 + 同步 Future（含超时）；服务端注解式本地注册与反射调用；心跳 / 空闲检测保障连接稳定；SPI 预留序列化与注册中心扩展。配套展示站上线 Vercel。  
GitHub：https://github.com/willson369/SparkRPC　｜　Demo：https://sparkrpc.vercel.app
