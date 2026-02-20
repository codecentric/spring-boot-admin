# Server Setup

Setting up a Spring Boot Admin Server is straightforward and requires only a few steps. The server acts as the central monitoring hub for all your Spring Boot applications.

## Creating the Admin Server[​](#creating-the-admin-server "Direct link to Creating the Admin Server")

### Step 1: Create a Spring Boot Project[​](#step-1-create-a-spring-boot-project "Direct link to Step 1: Create a Spring Boot Project")

Use [Spring Initializr](https://start.spring.io) to create a new Spring Boot project, or add the dependencies to an existing project.

### Step 2: Add Maven Dependencies[​](#step-2-add-maven-dependencies "Direct link to Step 2: Add Maven Dependencies")

Add the Spring Boot Admin Server starter and a web starter to your `pom.xml`:

pom.xml

```

<dependencies>
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>4.1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
</dependencies>
```

For Gradle:

build.gradle

```
dependencies {
    implementation 'de.codecentric:spring-boot-admin-starter-server:4.1.0-SNAPSHOT'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
}
```

tip

You can choose either Servlet (WebMVC) or Reactive (WebFlux) as your web stack. For reactive applications, use `spring-boot-starter-webflux` instead.

### Step 3: Enable Admin Server[​](#step-3-enable-admin-server "Direct link to Step 3: Enable Admin Server")

Annotate your main application class with `@EnableAdminServer`:

SpringBootAdminApplication.java

```
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

The `@EnableAdminServer` annotation enables Spring Boot Admin Server by loading all required configuration through Spring's auto-discovery feature.

### Step 4: Configure Application Properties[​](#step-4-configure-application-properties "Direct link to Step 4: Configure Application Properties")

Create or update your `application.yml` or `application.properties`:

application.yml

```
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

### Step 5: Run the Server[​](#step-5-run-the-server "Direct link to Step 5: Run the Server")

Start your application and navigate to `http://localhost:8080` to access the Spring Boot Admin UI.

## Server Configuration Options[​](#server-configuration-options "Direct link to Server Configuration Options")

### Custom Context Path[​](#custom-context-path "Direct link to Custom Context Path")

If you want to run the Admin Server under a different context path:

application.yml

```
spring:
  boot:
    admin:
      context-path: /admin  # UI will be available at http://localhost:8080/admin
```

### Customizing the Server Port[​](#customizing-the-server-port "Direct link to Customizing the Server Port")

application.yml

```
server:
  port: 9090  # Run on a different port
```

## Servlet vs. Reactive[​](#servlet-vs-reactive "Direct link to Servlet vs. Reactive")

Spring Boot Admin Server can run on either a Servlet or Reactive stack:

### Servlet (Default)[​](#servlet-default "Direct link to Servlet (Default)")

```

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

Best for traditional servlet-based applications and when you need features like Jolokia (JMX support).

### Reactive (WebFlux)[​](#reactive-webflux "Direct link to Reactive (WebFlux)")

```

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

Best for fully reactive applications and high-concurrency scenarios.

## Deployment Options[​](#deployment-options "Direct link to Deployment Options")

### Standalone JAR[​](#standalone-jar "Direct link to Standalone JAR")

Build and run as a standalone application:

```
mvn clean package
java -jar target/spring-boot-admin-server.jar
```

### WAR Deployment[​](#war-deployment "Direct link to WAR Deployment")

For deployment to an external servlet container, see the [spring-boot-admin-sample-war](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-war/) example.

### Docker[​](#docker "Direct link to Docker")

Create a `Dockerfile`:

```
FROM eclipse-temurin:17-jre
COPY target/spring-boot-admin-server.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:

```
docker build -t spring-boot-admin-server .
docker run -p 8080:8080 spring-boot-admin-server
```

## Next Steps[​](#next-steps "Direct link to Next Steps")

Now that your server is running, you need to register your applications:

* [Client Registration](/4.1.0-SNAPSHOT/docs/getting-started/client-registration.md) - Learn how to register applications with the server
* [Server Configuration](/4.1.0-SNAPSHOT/docs/server/server.md) - Explore advanced server configuration options
* [Security](/4.1.0-SNAPSHOT/docs/server/security.md) - Secure your Admin Server

## Example Projects[​](#example-projects "Direct link to Example Projects")

* [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/) - Complete servlet-based example with security
* [spring-boot-admin-sample-reactive](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-reactive/) - Reactive (WebFlux) example
