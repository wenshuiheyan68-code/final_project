# 校园失物招领管理系统

本项目是一个基于 C/S 架构的 Java 命令行系统。服务端维护失物和招领记录；客户端通过 TCP Socket 发布、查询、认领、删除和统计信息。

## 技术点

- 集合：`ConcurrentHashMap`、`ArrayList`、`LinkedHashMap` 管理业务记录与请求参数。
- 反射：`CommandDispatcher` 扫描 `@RemoteCommand` 注解，动态路由网络命令。
- 序列化：`Request`、`Response` 和 `LostItem` 经对象流传输；数据快照保存为 `.dat` 文件。
- 网络编程：基于 TCP Socket 的客户端/服务端通信。
- 多线程：固定线程池并发处理客户端；定时线程周期性保存快照。

项目刻意未依赖 Spring、SSH 等框架，也未要求额外的数据库驱动，克隆后即可运行。

## 运行方式

1. 在 IDEA 中先运行 `cn.edu.finalproject.lostfound.server.ServerMain`。
2. 再运行 `cn.edu.finalproject.lostfound.client.ClientMain`，按菜单操作。
3. 需要快速验证时，直接运行 `cn.edu.finalproject.lostfound.DemoMain`。

默认端口为 `9876`。服务端运行产生的数据在 `data/lost-found.dat`；演示数据在 `data/demo-lost-found.dat`。

## 主要包说明

| 包 | 作用 |
| --- | --- |
| `model` | 失物记录及枚举实体 |
| `protocol` | 客户端与服务端传输对象 |
| `repository` | 集合存储和序列化快照 |
| `service` | 核心业务规则 |
| `command` | 反射命令分发 |
| `server` | TCP 服务端与多线程处理 |
| `client` | TCP 客户端与 CMD 菜单 |
