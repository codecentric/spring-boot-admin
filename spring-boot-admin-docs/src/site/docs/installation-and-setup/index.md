---
sidebar_position: 1
---

# Installation and Setup

## Overview

Spring Boot Admin works by registering Spring Boot applications that expose Actuator endpoints. Each application's
health and metrics data is polled by Spring Boot Admin Server, which aggregates and displays this information in a web
dashboard. The registered applications can either self-register or be discovered using service discovery tools like
Eureka or Consul. Through the dashboard, users can monitor the health, memory usage, logs, and more for each
application, and even interact with them via management endpoints for tasks like restarting or updating configurations.

## Motivation

In modern microservices architecture, monitoring and managing distributed systems is complex and challenging. Spring
Boot Admin provides a powerful solution for visualizing, monitoring, and managing Spring Boot applications in real-time.
By offering a web interface that aggregates the health and metrics of all attached services, Spring Boot Admin
simplifies the process of ensuring system stability and performance. Whether you need insights into application health,
memory usage, or log output, Spring Boot Admin offers a centralized tool that streamlines operational management,
helping developers and DevOps teams maintain robust and efficient applications.

While Spring Boot Admin offers a user-friendly and centralized interface for monitoring Spring Boot applications, it is
not designed to replace sophisticated, full-scale monitoring and observability tools like Grafana, Datadog, or Instana.
These tools provide advanced capabilities such as real-time alerting, history data, complex metric analysis, distributed
tracing, and customizable dashboards across diverse environments.

Spring Boot Admin excels at providing a lightweight, application-centric view with essential health checks, metrics, and
management endpoints. For production-grade observability in larger, more complex systems, integrating Spring Boot Admin
alongside these advanced platforms ensures comprehensive system monitoring and deep insights.

## Quick Start

Since Spring Boot Admin is built on top of Spring Boot, you'll need to set up a Spring Boot application first. We
recommend using [http://start.spring.io](http://start.spring.io) for easy project setup. The Spring Boot Admin Server
can run as either in a Servlet or WebFlux application, so you'll need to choose one and add the corresponding Spring
Boot Starter. In this example, we'll use the Servlet Web Starter.

### Setting up the Spring Boot Admin Server

To set up Spring Boot Admin Server, you need to add the dependency `spring-boot-admin-starter-server` as well as
`spring-boot-starter-web` to your project (either in your `pom.xml` or `build.gradle(.kts)`).

```xml title="pom.xml"

<project>
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>@VERSION@</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</project>
```

After that, you need to annotate your main class with `@EnableAdminServer` to enable the Spring Boot Admin Server.
This will load all required configuration at runtime by leveraging Springs' autodiscovery feature.

```java title="SpringBootAdminApplication.java"

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}
}
```

After starting your application, you can now access the Spring Boot Admin Server web interface at
`http://localhost:8080`.

:::note
If you want to set up Spring Boot Admin Server via war-deployment in a servlet-container, please have a look at
the [spring-boot-admin-sample-war](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-war/).
:::

:::note
See also
the [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/)
project, which adds security.
:::

### Registering Applications

To register your application at the server, you can either include the Spring Boot Admin Client or
use [Spring Cloud Discovery](https://spring.io/projects/spring-cloud) (e.g. Eureka, Consul, …​). There is also
an [option to use static configuration on server side](../server#spring-cloud-discovery-static-config).

#### Using Spring Boot Admin Client

Each application that is not using Spring Cloud features but wants to register at the server has to include the Spring
Boot Admin Client as dependency.

```xml title="pom.xml"

<project>
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-client</artifactId>
        <version>@VERSION@</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</project>
```

After adding the dependency, you need to configure the URL of the Spring Boot Admin Server in your
`application.properties` or `application.yml` file as follows:

```properties title="application.properties"
spring.boot.admin.client.url=http://localhost:8080  #1
management.endpoints.web.exposure.include=*  #2
management.info.env.enabled=true #3
```

1. This property defines the URL of the Spring Boot Admin Server.
2. As with Spring Boot 2 most of the endpoints aren’t exposed via http by default, but we want to expose all of them.
   For production, you should carefully choose which endpoints to expose and keep security in mind. It is also possible
   to use a different port for the actuator endpoints by setting `management.port` property.
3. Since Spring Boot 2.6, the info actuator endpoint is disabled by default. In our case, we enable it to
   provide additional information to the Spring Boot Admin Server.

When you start your monitored application now, it will register itself at the Spring Boot Admin Server. You can see your
app in the web interface of Spring Boot Admin.

:::info
It is possible to add `spring-boot-admin-client` as well as `spring-boot-admin-server` to the same application. This
allows you to monitor the Spring Boot Admin Server itself. To get a more realistic setup, you should run the Spring Boot
Admin Server and clients in separate applications.
:::

#### Using Spring Cloud Discovery

If you already use Spring Cloud Discovery in your application architecture you don’t need to add Spring Boot Admin
Client. In this case you can leverage the Spring Cloud features by adding a DiscoveryClient to Spring Boot Admin Server.

The following steps uses Eureka, but other Spring Cloud Discovery implementations are supported as well. There are
examples
for [Consul](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-consul/)
and [Zookeeper](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/).

Since Spring Boot Admin Server is fully build on top of Spring Cloud features and uses its discovery mechanism, please
refer to the [Spring Cloud documentation](http://projects.spring.io/spring-cloud) for more information.

To start using Eureka, you need to add the following dependencies to your project:

```xml title="pom.xml"

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

After that, you have to enable discovery by adding `@EnableDiscoveryClient` to your configuration:

```java title="SpringBootAdminApplication.java"

@EnableDiscoveryClient
@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}
}
```

The next step is to configure the Eureka client in your `application.yml` file and define the URL of Eureka's service
registry.

```yml title="application.yml"
spring:
  application:
    name: spring-boot-admin-sample-eureka
  profiles:
    active:
      - secure
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}    # needed to trigger info and endpoint update after restart
  client:
    registryFetchIntervalSeconds: 5
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

:::info
There is also a [basic example](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-eureka/) in Spring Boot Admin's GitHub repository using Eureka.
:::

:::tip
You can include the Spring Boot Admin Server to your Eureka server as well. Setup everything as described above and set
`spring.boot.admin.context-path` to something different from `/` so that the Spring Boot Admin Server UI won’t clash
with
Eureka’s one.
:::

### Docker Images
Since Spring Boot Admin can be run in a vast variety of environments, we neither provide nor maintain any Docker images.
However, you can easily create your own Docker image by adding a Dockerfile to your project and add the configuration that fits your needs.
Even though we don't provide Docker images, we have a some example in our Docker Hub repository at [https://hub.docker.com/r/codecentric/spring-boot-admin](https://hub.docker.com/r/codecentric/spring-boot-admin).

:::info
We do not offer any support for these images. 
They are provided as-is and are not maintained by the Spring Boot Admin team.
Neither do we guarantee that they are up-to-date nor secure.
As stated in the preambles, we recommend to create your own Docker image.
:::
