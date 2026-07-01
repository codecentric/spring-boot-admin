---
sidebar_position: 10
sidebar_custom_props:
  icon: 'server'
---

# Server Setup

Setting up a Spring Boot Admin Server is straightforward and requires only a few steps. The server acts as the central
monitoring hub for all your Spring Boot applications.

## Creating the Admin Server

### Step 1: Create a Spring Boot Project

Use [Spring Initializr](https://start.spring.io) to create a new Spring Boot project, or add the dependencies to an
existing project.

### Step 2: Add Maven Dependencies

Add the Spring Boot Admin Server starter and a web starter to your `pom.xml`:

```xml title="pom.xml"

<dependencies>
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>@VERSION@</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
</dependencies>
```

For Gradle:

```groovy title="build.gradle"
dependencies {
    implementation 'de.codecentric:spring-boot-admin-starter-server:@VERSION@'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
}
```

:::tip
You can choose either Servlet (WebMVC) or Reactive (WebFlux) as your web stack. For reactive applications, use
`spring-boot-starter-webflux` instead.
:::

### Step 3: Enable Admin Server

Annotate your main application class with `@EnableAdminServer`:

```java title="SpringBootAdminApplication.java"
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}
}
```

The `@EnableAdminServer` annotation enables Spring Boot Admin Server by loading all required configuration through
Spring's auto-discovery feature.

### Step 4: Configure Application Properties

Create or update your `application.yml` or `application.properties`:

```yaml title="application.yml"
spring:
  application:
    name: spring-boot-admin-server

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

### Step 5: Run the Server

Start your application and navigate to `http://localhost:8080` to access the Spring Boot Admin UI.

## Server Configuration Options

### Custom Context Path

If you want to run the Admin Server under a different context path:

```yaml title="application.yml"
spring:
  boot:
    admin:
      context-path: /admin  # UI will be available at http://localhost:8080/admin
```

### Customizing the Server Port

```yaml title="application.yml"
server:
  port: 9090  # Run on a different port
```

## Servlet vs. Reactive

Spring Boot Admin Server can run on either a Servlet or Reactive stack:

### Servlet (Default)

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

Best for traditional servlet-based applications and when you need features like Jolokia (JMX support).

### Reactive (WebFlux)

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

Best for fully reactive applications and high-concurrency scenarios.

## Deployment Options

### Standalone JAR

Build and run as a standalone application:

```bash
mvn clean package
java -jar target/spring-boot-admin-server.jar
```

### WAR Deployment

For deployment to an external servlet container, see
the [spring-boot-admin-sample-war](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-war/)
example.

### Docker

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre
COPY target/spring-boot-admin-server.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:

```bash
docker build -t spring-boot-admin-server .
docker run -p 8080:8080 spring-boot-admin-server
```

## Next Steps

Now that your server is running, you need to register your applications:

- [Client Registration](./20-client-registration.md) - Learn how to register applications with the server
- [Server Configuration](../02-server/01-server.mdx) - Explore advanced server configuration options
- [Security](../02-server/02-security.md) - Secure your Admin Server

## Example Projects

- [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/) -
  Complete servlet-based example with security
- [spring-boot-admin-sample-reactive](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-reactive/) -
  Reactive (WebFlux) example
