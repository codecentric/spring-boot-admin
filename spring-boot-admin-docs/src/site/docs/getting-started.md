---
sidebar_position: 1
---
# Getting started

## Overview
Spring Boot Admin is a monitoring tool that aims to visualize information provided by Spring Boot Actuators in a nice and accessible way.
To achieve this, Spring Boot Admin consists of two main components:

* A server that provides a user interface to display and interact with Spring Boot Actuators.
* A client that is used to register at the server and allow to access actuator endpoints.

## Quick Start
Since Spring Boot Admin relies on Spring Boot, you have to set up a Spring Boot application first.
We recommend doing this by using [http://start.spring.io](http://start.spring.io).
Spring Boot Admin Server is capable of running as servlet or webflux application, you need to decide on this and add the according Spring Boot Starter.
In this example we're using the servlet web starter.

1. Add Spring Boot Admin Server starter as dependency:

__pom.xml__
```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
    <version>@VERSION@</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
2. Pull in the Spring Boot Admin Server configuration via adding `@EnableAdminServer` to your configuration:
```java
@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```

:::note
If you want to setup the Spring Boot Admin Server via war-deployment in a servlet-container, please have a look at the [spring-boot-admin-sample-war](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-war/).
:::

:::note
See also the [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/) project, which also adds security.
:::

## Registering Client Applications

To register your application at the SBA Server, you can either include the SBA Client or use [Spring Cloud Discovery](https://spring.io/projects/spring-cloud) (e.g. Eureka, Consul, …​). There is also a [simple option using a static configuration on the SBA Server side](server/server#spring-cloud-discovery-static-config).

### Spring Boot Admin Client

Each application that wants to register has to include the Spring Boot Admin Client. In order to secure the endpoints, also add the `spring-boot-starter-security`.

1. Add spring-boot-admin-starter-client to your dependencies:
pom.xml 
```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>@VERSION@</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
2. Enable the SBA Client by configuring the URL of the Spring Boot Admin Server:
application.properties
```properties
spring.boot.admin.client.url=http://localhost:8080  #(1)
management.endpoints.web.exposure.include=*  #(2)
management.info.env.enabled=true #(3)
```
   1. The URL of the Spring Boot Admin Server to register at.
   2. As with Spring Boot 2 most of the endpoints aren’t exposed via http by default, we expose all of them. For production you should carefully choose which endpoints to expose.
   3. Since Spring Boot 2.6, env info contributor is disabled by default. Hence, we have to enable it.
3. Make the actuator endpoints accessible:
```java
@Configuration
public static class SecurityPermitAllConfig {
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) {
        return http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests.anyRequest().permitAll())
            .csrf().disable().build(); //(1)
    }
}
```
   1. For the sake of brevity we’re disabling the security for now. Have a look at the [security section](security#%5Fsecurity) on how to deal with secured endpoints.

### Spring Cloud Discovery

If you already use Spring Cloud Discovery for your applications you don’t need the SBA Client. Just add a DiscoveryClient to Spring Boot Admin Server, the rest is done by our AutoConfiguration.

The following steps uses Eureka, but other Spring Cloud Discovery implementations are supported as well. There are examples using [Consul](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-consul/) and [Zookeeper](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/).

Also, have a look at the [Spring Cloud documentation](http://projects.spring.io/spring-cloud/spring-cloud.html).

1. Add spring-cloud-starter-eureka to your dependencies:
pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
2. Enable discovery by adding `@EnableDiscoveryClient` to your configuration:
```java
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableScheduling
@EnableAdminServer
public class SpringBootAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```
3. Tell the Eureka client where to find the service registry:
application.yml
```yml
spring:
  application:
    name: spring-boot-admin-sample-eureka
  profiles:
    active:
      - secure
eureka: #(1)
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}    # needed to trigger info and endpoint update after restart
  client:
    registryFetchIntervalSeconds: 5
    serviceUrl:
      defaultZone: ${EUREKA_SERVICE_URL:http://localhost:8761}/eureka/
management:
  endpoints:
    web:
      exposure:
        include: "*" #(2)
  endpoint:
    health:
      show-details: ALWAYS
```
   1. Configuration section for the Eureka client
   2. As with Spring Boot 2 most of the endpoints aren’t exposed via http by default, we expose all of them. For production, you should carefully choose which endpoints to expose.

See also [spring-boot-admin-sample-eureka](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-eureka/).

:::tip
You can include the Spring Boot Admin Server to your Eureka server. Setup everything as described above and set spring.boot.admin.context-path to something different to "/" so that the Spring Boot Admin Server UI won’t clash with Eureka’s one.
:::
