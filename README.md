# Spring Boot Admin

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

Operating Spring Boot applications in production requires visibility into their runtime behavior. The Spring Boot
Actuator provides the data, but accessing it through raw REST endpoints or JMX can be cumbersome. Spring Boot Admin
bridges this gap by providing **operational visibility** without requiring custom dashboards or monitoring tools.
Developers can troubleshoot issues directly through a user-friendly interface instead of interpreting raw metrics, and
operations teams can monitor dozens or hundreds of microservices from a single location rather than managing each
application individually. The integration works out-of-the-box with any Spring Boot application that exposes actuator
endpoints, providing non-technical team members (operators, QA, support) with visibility into application state without
requiring deep technical knowledge.

### Design Philosophy

Spring Boot Admin is built on the principle of **simplicity without sacrificing power**. The project requires minimal
configuration—just add a dependency to your Spring Boot application and the monitoring starts working immediately. The
interface is designed so that operations teams can understand application state at a glance, without needing to
interpret raw metrics or understand implementation details. At the same time, Spring Boot Admin is production-ready,
handling real-world scenarios including security, multi-application deployments, and scalability concerns. The
architecture supports extensibility, allowing you to customize the UI and behavior for your organizational needs while
maintaining sensible defaults for teams who want to use it out-of-the-box. It also works across different Spring Boot
versions, architectures, and deployment strategies, and is designed to be lightweight—consuming minimal resources on
both the admin server and monitoring clients.

### Who Benefits from Spring Boot Admin?

Spring Boot Admin serves a broad range of users across different roles and organizational structures. **Development
teams** use it to quickly diagnose application issues during development and testing phases. **Operations teams**
leverage it to monitor production applications and respond to issues faster, while **DevOps engineers** integrate it
into their microservices management workflows in containerized or cloud environments. **Site reliability engineers (
SREs)** add Spring Boot Admin to their observability stack for Spring Boot-specific insights that complement broader
monitoring systems. Whether you're running a single application or managing hundreds of microservices across an
enterprise, Spring Boot Admin scales from small teams to large organizations with minimal operational overhead.

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

## Quick Start

### Server

1. Create a new Spring Boot application or use an existing one
2. Add the dependency:

```xml

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
    <version>${latest-version}</version>
</dependency>
```

3. Enable Spring Boot Admin in your main class:

```java

@SpringBootApplication
@EnableAdminServer
public class AdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
```

4. Start the application and open `http://localhost:8080`

### Client

Add the client dependency to any Spring Boot application you want to monitor:

```xml

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>${latest-version}</version>
</dependency>
```

Configure the connection:

```yaml
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080
```

For more detailed information, see the [reference documentation](https://docs.spring-boot-admin.com/current). For
service-discovery setups (Eureka, Consul, Zookeeper), see
the [Integration Guide](https://docs.spring-boot-admin.com/current/docs/integration).

## Compatibility

Spring Boot Admin versions track Spring Boot's major and minor versions as follows:

| Spring Boot | Spring Boot Admin |
|-------------|-------------------|
| 3.Y.Z       | 3.X.c             |
| 4.0.Y       | 4.0.c             |
| 4.1.Y       | 4.1.c             |

**Note:** You can monitor applications running on any Spring Boot version, independently of the admin server version.
For example, monitor a Spring Boot 2.7 application using the 2.7.y client with a 4.1.x admin server.

## For Users

| Resource                  | Link                                                                                                                                     |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| Reference Documentation   | [docs.spring-boot-admin.com/current](https://docs.spring-boot-admin.com/current)                                                         |
| Getting Started Guide     | [Getting Started](https://docs.spring-boot-admin.com/current/docs/getting-started)                                                       |
| Upgrade Guide (3.x → 4.x) | [Upgrading to SBA 4](https://docs.spring-boot-admin.com/current/docs/upgrading/spring-boot-admin-4)                                      |
| Releases & Changelog      | [GitHub Releases](https://github.com/codecentric/spring-boot-admin/releases)                                                             |
| Maven Central (stable)    | [de.codecentric:spring-boot-admin-starter-server](https://central.sonatype.com/artifact/de.codecentric/spring-boot-admin-starter-server) |
| Sample Applications       | [spring-boot-admin-samples/](spring-boot-admin-samples/)                                                                                 |
| Stack Overflow Q&A        | [tag: spring-boot-admin](https://stackoverflow.com/questions/tagged/spring-boot-admin)                                                   |
| Community Chat            | [Gitter](https://gitter.im/codecentric/spring-boot-admin)                                                                                |
| Bug Reports               | [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues)                                                                 |

## For Developers

| Resource               | Link                                                                                |
|------------------------|-------------------------------------------------------------------------------------|
| GitHub Repository      | [codecentric/spring-boot-admin](https://github.com/codecentric/spring-boot-admin)   |
| Security Documentation | [Security Guide](https://docs.spring-boot-admin.com/current/docs/security)          |
| Customization Guide    | [Customization Docs](https://docs.spring-boot-admin.com/current/docs/customization) |
| REST API Reference     | [REST API](https://docs.spring-boot-admin.com/current/docs/reference/rest-api)      |
| UI Development Guide   | [spring-boot-admin-server-ui/README.md](spring-boot-admin-server-ui/README.md)      |
| UI Component Explorer  | Storybook — run `npm run storybook` inside `spring-boot-admin-server-ui/`           |
| Snapshot Builds        | [GitHub Packages](https://maven.pkg.github.com/codecentric/spring-boot-admin)       |

### Building from Source

Requires Java 17+ and Maven 3.9+ (wrapper included).

```bash
git clone https://github.com/codecentric/spring-boot-admin.git
cd spring-boot-admin
./mvnw clean install
```

### Running Tests

```bash
./mvnw test                  # all tests
./mvnw test -pl spring-boot-admin-server-ui  # UI unit tests only (Vitest)
```

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
