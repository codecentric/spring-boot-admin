---

sidebar_position: 50
sidebar_custom_props:
  icon: 'file-code'
---

# Zookeeper Sample

Spring Boot Admin Server integration with Apache Zookeeper for service discovery. This sample shows how to use Zookeeper
as a service registry to automatically discover and monitor Spring Boot applications.

## Overview

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/`

**Features**:

- Service discovery via Apache Zookeeper
- No Admin Client required on monitored apps
- Metadata-based configuration
- Custom actuator paths (/foo, /ping)
- Profile-based security

## Prerequisites

- Java 17+, Maven 3.6+
- Apache Zookeeper installed

## Running

### Start Zookeeper

```bash
# Docker
docker run -d -p 2181:2181 zookeeper:3.8

# Or download from https://zookeeper.apache.org/
```

### Start Admin Server

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-zookeeper
mvn spring-boot:run
```

Access at: `http://localhost:8080`

## Configuration

```yaml
spring:
  application:
    name: zookeeper-example
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        metadata:
          management.context-path: /foo  # Dots allowed (unlike Consul)
          health.path: /ping
          user.name: user
          user.password: password

management:
  endpoints:
    web:
      base-path: /foo
      path-mapping:
        health: /ping
```

## Key Differences

### vs. Consul

- **Metadata keys**: Dots allowed in Zookeeper
- **Simplicity**: Fewer features, simpler setup
- **Use case**: Hadoop/Big Data ecosystems

### vs. Eureka

- **Maturity**: Zookeeper is older, more established
- **Ecosystem**: Hadoop/Kafka integration
- **Complexity**: More configuration required

## See Also

- [Zookeeper Integration](../04-integration/30-zookeeper.md)
- [Eureka Sample](./30-sample-eureka.md)
- [Consul Sample](./40-sample-consul.md)
