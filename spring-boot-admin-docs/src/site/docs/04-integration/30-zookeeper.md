---
sidebar_position: 30
sidebar_custom_props:
  icon: 'cloud'
---

# Zookeeper Integration

Apache Zookeeper is a centralized coordination service that can be used for service discovery with Spring Cloud
Zookeeper. This guide shows how to integrate Spring Boot Admin with Zookeeper.

## Overview

With Zookeeper integration:

- Applications register with Zookeeper
- Spring Boot Admin Server discovers applications via Zookeeper
- Automatic ephemeral node management
- No Spring Boot Admin Client library required

## Setting Up Zookeeper

### Install Zookeeper

```bash
# macOS
brew install zookeeper

# Linux
wget https://downloads.apache.org/zookeeper/zookeeper-3.8.3/apache-zookeeper-3.8.3-bin.tar.gz
tar -xzf apache-zookeeper-3.8.3-bin.tar.gz
cd apache-zookeeper-3.8.3-bin

# Docker
docker run -d --name zookeeper -p 2181:2181 zookeeper:latest
```

### Start Zookeeper

```bash
# Direct
zkServer start

# Docker
docker start zookeeper
```

Verify Zookeeper is running:

```bash
echo ruok | nc localhost 2181
# Should respond with: imok
```

## Configuring Spring Boot Admin Server

### Add Dependencies

```xml title="pom.xml"
<dependencies>
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
    </dependency>
</dependencies>
```

### Enable Discovery

```java title="SpringBootAdminZookeeperApplication.java"
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminZookeeperApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminZookeeperApplication.class, args);
    }
}
```

### Configure Zookeeper Client

```yaml title="application.yml"
spring:
  application:
    name: spring-boot-admin-server
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        enabled: true
        register: true
        root: /services

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

## Configuring Client Applications

### Add Dependencies

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
```

### Enable Discovery

```java
@EnableDiscoveryClient
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Configure Application

```yaml title="application.yml"
spring:
  application:
    name: my-application
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        enabled: true
        register: true
        metadata:
          management.context-path: /actuator
          user.name: ${spring.security.user.name}
          user.password: ${spring.security.user.password}

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

## Metadata Configuration

### Adding Custom Metadata

```yaml title="application.yml"
spring:
  cloud:
    zookeeper:
      discovery:
        metadata:
          management.context-path: /actuator
          user.name: admin
          user.password: secret
          tags.environment: production
          tags.region: us-east-1
          team: platform
          version: 1.0.0
```

### Management Port

```yaml title="application.yml"
server:
  port: 8080

management:
  server:
    port: 9090
  endpoints:
    web:
      base-path: /actuator

spring:
  cloud:
    zookeeper:
      discovery:
        metadata:
          management.port: 9090
          management.context-path: /actuator
```

## Instance Configuration

### Instance ID

```yaml
spring:
  cloud:
    zookeeper:
      discovery:
        instance-id: ${spring.application.name}:${random.value}
```

### Prefer IP Address

```yaml
spring:
  cloud:
    zookeeper:
      discovery:
        preferIpAddress: true
```

### Custom Service Name

```yaml
spring:
  cloud:
    zookeeper:
      discovery:
        serviceName: custom-service-name
```

## Connection Configuration

### Connection Timeout

```yaml
spring:
  cloud:
    zookeeper:
      connect-string: localhost:2181
      max-retries: 10
      max-sleep-ms: 500
      connection-timeout: 15000
      session-timeout: 60000
```

### Multiple Zookeeper Servers

```yaml
spring:
  cloud:
    zookeeper:
      connect-string: zk1:2181,zk2:2181,zk3:2181
```

### Zookeeper Path

```yaml
spring:
  cloud:
    zookeeper:
      discovery:
        root: /services
        uriSpec: '{scheme}://{address}:{port}'
```

## Docker Compose Example

```yaml title="docker-compose.yml"
version: '3'

services:
  zookeeper:
    image: zookeeper:3.8
    ports:
      - "2181:2181"
    environment:
      - ZOO_MY_ID=1

  spring-boot-admin:
    build: ./admin-server
    ports:
      - "8080:8080"
    environment:
      - SPRING_CLOUD_ZOOKEEPER_CONNECT_STRING=zookeeper:2181
    depends_on:
      - zookeeper

  my-application:
    build: ./my-app
    ports:
      - "8081:8081"
    environment:
      - SPRING_CLOUD_ZOOKEEPER_CONNECT_STRING=zookeeper:2181
    depends_on:
      - zookeeper
```

## Troubleshooting

### Connection Failures

Check Zookeeper is running:

```bash
echo ruok | nc localhost 2181
```

Verify connection:

```bash
zkCli.sh -server localhost:2181
ls /services
```

### Service Not Appearing

List registered services:

```bash
zkCli.sh -server localhost:2181
ls /services
get /services/my-application
```

### Session Timeout

Increase session timeout:

```yaml
spring:
  cloud:
    zookeeper:
      session-timeout: 120000  # 2 minutes
```

## Best Practices

1. **Configure Retries**:
   ```yaml
   spring:
     cloud:
       zookeeper:
         max-retries: 10
         max-sleep-ms: 500
   ```

2. **Use Ensemble**:
   ```yaml
   spring:
     cloud:
       zookeeper:
         connect-string: zk1:2181,zk2:2181,zk3:2181
   ```

3. **Set Appropriate Timeouts**:
   ```yaml
   spring:
     cloud:
       zookeeper:
         connection-timeout: 15000
         session-timeout: 60000
   ```

4. **Use Instance IDs**:
   ```yaml
   spring:
     cloud:
       zookeeper:
         discovery:
           instance-id: ${spring.application.name}:${random.value}
   ```

5. **Monitor Zookeeper Health**:
   ```bash
   echo mntr | nc localhost 2181
   ```

## Complete Example

See
the [spring-boot-admin-sample-zookeeper](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/)
project for a complete working example.

## See Also

- [Service Discovery](../03-client/40-service-discovery.md) - Service discovery overview
- [Zookeeper Sample](../09-samples/50-sample-zookeeper.md) - Detailed sample walkthrough
- [Metadata](../03-client/30-metadata.md) - Working with metadata
