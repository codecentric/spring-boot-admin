# Spring Boot Admin（由 [codecentric](https://codecentric.de) 出品）

> 本文档是 [README.md](README.md) 的中文翻译，若两者有出入以英文原版为准。

[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
![Build Status](https://github.com/codecentric/spring-boot-admin/actions/workflows/build-main.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/codecentric/spring-boot-admin/branch/master/graph/badge.svg?token=u5SWsZpj5S)](https://codecov.io/gh/codecentric/spring-boot-admin)
![Maven Central Version](https://img.shields.io/maven-central/v/de.codecentric/spring-boot-admin)

**Spring Boot 应用的管理界面（Admin UI）**

Spring Boot Admin 提供了一个基于 Web 的仪表盘，用于监控和管理
[Spring Boot®](https://spring.io/projects/spring-boot) 应用。它通过直观的 Web 界面展示应用的健康状态、指标、
日志、线程信息以及配置。

## 目的与目标

### 为什么选择 Spring Boot Admin？

Spring Boot Actuator 暴露了丰富的运行时数据，但通过原始 REST 端点或 JMX 来使用这些数据十分繁琐。Spring
Boot Admin 弥补了这一空缺，为任何暴露了 actuator 端点的应用提供开箱即用的**运维可见性**——无需自建仪表盘或监控工具。

### 设计理念

**简单而不牺牲强大功能。** 添加一个依赖，监控即刻开始。界面保持一目了然的可读性，同时具备生产可用性，
并可在安全性、多应用部署和自定义 UI 方面进行扩展。

### 谁能从 Spring Boot Admin 中受益？

开发团队用它诊断开发过程中的问题，运维团队用它监控生产环境，DevOps/SRE 团队则可将其纳入自己的可观测性
技术栈。从单个应用到数百个微服务，它都能以极小的开销从容应对。

## ✨ 功能特性

- **实时监控** - 应用健康状态、指标与性能仪表盘
- **日志查看器** - 直接在 UI 中流式查看和搜索应用日志
- **环境检查器** - 浏览应用属性和环境变量
- **线程查看器** - 检查线程转储和线程信息
- **HTTP 追踪** - 监控 HTTP 请求与响应
- **JMX Bean** - 管理和检查 JMX bean
- **多应用支持** - 通过单个服务器监控多个应用
- **跨平台** - 监控运行在不同 Spring Boot 版本上的应用
- **完全可扩展** - 自定义 UI 并添加自定义视图
- **安全** - 内置认证与授权支持

## 兼容性

Spring Boot Admin 的版本与 Spring Boot 的主次版本对应关系如下：

| Spring Boot | Spring Boot Admin |
|-------------|-------------------|
| 3.Y.Z       | 3.X.c             |
| 4.0.Y       | 4.0.c             |
| 4.1.Y       | 4.1.c             |

**注意：** 你可以监控运行在任意 Spring Boot 版本上的应用，与管理服务器的版本无关。
例如，使用 2.7.y 客户端配合 4.1.x 管理服务器来监控一个 Spring Boot 2.7 应用。

### 快照构建

如需使用最新的开发版本，请添加以下仓库：

```xml

<repository>
    <id>sba-snapshot</id>
    <name>Spring Boot Admin Snapshots</name>
    <url>https://maven.pkg.github.com/codecentric/spring-boot-admin</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
    <releases>
        <enabled>false</enabled>
    </releases>
</repository>
```

## 参与贡献

我们欢迎各种贡献！请在提交 pull request 之前阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

| 资源                        | 链接                                                                                                                                |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| 贡献指南                    | [CONTRIBUTING.md](CONTRIBUTING.md)                                                                                                  |
| 适合新手的议题              | [label: ideal-for-contribution](https://github.com/codecentric/spring-boot-admin/issues?q=is%3Aopen+label%3Aideal-for-contribution) |
| 开放议题                    | [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues)                                                            |
| Spring Java Format 插件     | [spring-io/spring-javaformat](https://github.com/spring-io/spring-javaformat)                                                       |
| Spring Framework 代码风格   | [Code Style Wiki](https://github.com/spring-projects/spring-framework/wiki/Code-Style)                                              |
| IntelliJ 格式化插件         | [spring-javaformat#intellij-idea](https://github.com/spring-io/spring-javaformat#intellij-idea)                                     |

## 获取帮助

- **文档** — [参考指南](https://docs.spring-boot-admin.com/current)
- **Stack Overflow** — [提问时使用
  `spring-boot-admin` 标签](https://stackoverflow.com/questions/tagged/spring-boot-admin)
- **聊天** — [Gitter 社区](https://gitter.im/codecentric/spring-boot-admin)
- **议题** — [在 GitHub 上报告 bug](https://github.com/codecentric/spring-boot-admin/issues)

## 许可证

Spring Boot Admin 是基于
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 发布的开源软件。

Spring、Spring Boot 和 Spring Cloud 是 [VMware, Inc.](https://www.vmware.com/) 的商标。

---

由 [codecentric](https://codecentric.de) 构建