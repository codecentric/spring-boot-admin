# Spring Boot Admin by [codecentric](https://codecentric.de)

[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
![Build Status](https://github.com/codecentric/spring-boot-admin/actions/workflows/build-main.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/codecentric/spring-boot-admin/branch/master/graph/badge.svg?token=u5SWsZpj5S)](https://codecov.io/gh/codecentric/spring-boot-admin)
![Maven Central Version](https://img.shields.io/maven-central/v/de.codecentric/spring-boot-admin)

**Admin UI for Spring Boot applications**

Spring Boot Admin provides a web-based dashboard for monitoring and
managing [Spring Boot®](https://spring.io/projects/spring-boot) applications. It displays application health, metrics,
logs, thread information, and configuration through an intuitive web interface.

## Purpose & Goals

### Why Spring Boot Admin?

Spring Boot Actuator exposes rich runtime data, but consuming it through raw REST endpoints or JMX is cumbersome. Spring
Boot Admin bridges this gap with **operational visibility** out-of-the-box for any application that exposes actuator
endpoints—no custom dashboards or monitoring tools required.

### Design Philosophy

**Simplicity without sacrificing power.** Add a dependency and monitoring starts immediately. The interface stays
readable at a glance, while remaining production-ready and extensible for security, multi-application deployments, and
custom UIs.

### Who Benefits from Spring Boot Admin?

Development teams diagnose issues during development, operations teams monitor production, and DevOps/SRE teams fold it
into their observability stack. It scales from a single application to hundreds of microservices with minimal overhead.

## ✨ Features

- **Real-time Monitoring** - Application health, metrics, and performance dashboard
- **Log Viewer** - Stream and search application logs directly from the UI
- **Environment Inspector** - Browse application properties and environment variables
- **Thread Viewer** - Inspect thread dumps and thread information
- **HTTP Trace** - Monitor HTTP requests and responses
- **JMX Beans** - Manage and inspect JMX beans
- **Multi-Application Support** - Monitor multiple applications from a single server
- **Cross-Platform** - Monitor applications running on different Spring Boot versions
- **Fully Extensible** - Customize the UI and add custom views
- **Secure** - Built-in authentication and authorization support

## Compatibility

Spring Boot Admin versions track Spring Boot's major and minor versions as follows:

| Spring Boot | Spring Boot Admin |
|-------------|-------------------|
| 3.Y.Z       | 3.X.c             |
| 4.0.Y       | 4.0.c             |
| 4.1.Y       | 4.1.c             |

**Note:** You can monitor applications running on any Spring Boot version, independently of the admin server version.
For example, monitor a Spring Boot 2.7 application using the 2.7.y client with a 4.1.x admin server.

### Snapshot Builds

For the latest development version, add this repository:

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

## Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

| Resource                    | Link                                                                                                                                |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| Contribution Guide          | [CONTRIBUTING.md](CONTRIBUTING.md)                                                                                                  |
| Good first issues           | [label: ideal-for-contribution](https://github.com/codecentric/spring-boot-admin/issues?q=is%3Aopen+label%3Aideal-for-contribution) |
| Open Issues                 | [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues)                                                            |
| Spring Java Format Plugin   | [spring-io/spring-javaformat](https://github.com/spring-io/spring-javaformat)                                                       |
| Spring Framework Code Style | [Code Style Wiki](https://github.com/spring-projects/spring-framework/wiki/Code-Style)                                              |
| IntelliJ Formatter Plugin   | [spring-javaformat#intellij-idea](https://github.com/spring-io/spring-javaformat#intellij-idea)                                     |

## Getting Help

- **Documentation** — [Reference Guide](https://docs.spring-boot-admin.com/current)
- **Stack Overflow** — [Ask questions tagged
  `spring-boot-admin`](https://stackoverflow.com/questions/tagged/spring-boot-admin)
- **Chat** — [Gitter Community](https://gitter.im/codecentric/spring-boot-admin)
- **Issues** — [Report bugs on GitHub](https://github.com/codecentric/spring-boot-admin/issues)

## License

Spring Boot Admin is open source software released under
the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Spring, Spring Boot, and Spring Cloud are trademarks of [VMware, Inc.](https://www.vmware.com/)

---

Built by [codecentric](https://codecentric.de)
