---
sidebar_position: 1
title: Getting Started
---
# What is Spring Boot Admin?

Spring Boot Admin is a monitoring tool that aims to visualize information provided by Spring Boot Actuators in a nice and accessible way. It consists of two major parts:

- A **server** that provides a user interface to display and interact with Spring Boot Actuators.
- A **client** that is used to register at the server and allow to access actuator endpoints.

## Setting up a server

Since Spring Boot Admin relies on Spring Boot, you have to set up a Spring Boot application first. We recommend doing this by using [https://start.spring.io](https://start.spring.io/#!dependencies=codecentric-spring-boot-admin-server,web) . Spring Boot Admin Server is capable of running as servlet or webflux application, you need to decide on this and add the according Spring Boot Starter. In this example we’re using the servlet web starter.

1.  Add Spring Boot Admin Server and Spring Boot Web to your dependencies:

2.  Pull in the Spring Boot Admin Server configuration via adding `@EnableAdminServer` to your Application class:

    ``` java
    @SpringBootApplication @EnableAdminServer public class SpringBootAdminApplication { public static void main(String[] args) { SpringApplication.run(SpringBootAdminApplication.class, args); } }
    ```

Now run `mvn spring-boot:run` to start up the server. Aou should now be able to browse to http://localhost:8080 and see the Spring Boot Admin Server UI. As you have not set up any service to monitor, there will be no content, sadly. Let’s change this in the next section.

## Register Client Applications

To register an application at the Spring Boot Admin Server, you can either include the SBA Client or use [Spring Cloud Discovery](http://projects.spring.io/spring-cloud/spring-cloud.html) (e.g. Eureka, Consul, …). There is also a [static configuration](#server/discovery#spring-cloud-discovery-static-config) on the SBA Server side, but let’s keep things simple first, by using Spring Boot Admin Client!

1.  Add spring-boot-admin-starter-client to your dependencies:

    <div class="formalpara-title">

    **pom.xml**

    </div>

    ``` xml
    <dependency> <groupId>de.codecentric</groupId> <artifactId>spring-boot-admin-starter-client</artifactId> </dependency> <dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId> </dependency>
    ```

2.  Enable the SBA Client by configuring the URL of the Spring Boot Admin Server:

    <div class="formalpara-title">

    **application.properties**

    </div>

    ``` properties
    server.port=8081 # spring.boot.admin.client.url=http://localhost:8080 # management.endpoints.web.exposure.include=* #
    ```

  - Server port of the application. Must be different from the Spring Boot Admin Server port.

  - The URL of the Spring Boot Admin Server to register at.

  - Spring Boot does not exposed all management endpoints by default, we want all of them. For production you should carefully choose which endpoints to expose.

Now run `mvn spring-boot:run` to start up the client. After a few seconds, Spring Boot Admin should show the client in the UI as "UP". If this is not the case, look at the logs of the client carefully, it should tell you what went wrong:
