# Zookeeper Sample

Spring Boot Admin Server integration with Apache Zookeeper for service discovery. This sample shows how to use Zookeeper as a service registry to automatically discover and monitor Spring Boot applications.

## Overview[​](#overview "Direct link to Overview")

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/`

**Features**:

* Service discovery via Apache Zookeeper
* No Admin Client required on monitored apps
* Metadata-based configuration
* Custom actuator paths (/foo, /ping)
* Profile-based security

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17+, Maven 3.6+
* Apache Zookeeper installed

## Running[​](#running "Direct link to Running")

### Start Zookeeper[​](#start-zookeeper "Direct link to Start Zookeeper")

```
# Docker
docker run -d -p 2181:2181 zookeeper:3.8

# Or download from https://zookeeper.apache.org/
```

### Start Admin Server[​](#start-admin-server "Direct link to Start Admin Server")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-zookeeper
mvn spring-boot:run
```

Access at: `http://localhost:8080`

## Configuration[​](#configuration "Direct link to Configuration")

```
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

## Key Differences[​](#key-differences "Direct link to Key Differences")

### vs. Consul[​](#vs-consul "Direct link to vs. Consul")

* **Metadata keys**: Dots allowed in Zookeeper
* **Simplicity**: Fewer features, simpler setup
* **Use case**: Hadoop/Big Data ecosystems

### vs. Eureka[​](#vs-eureka "Direct link to vs. Eureka")

* **Maturity**: Zookeeper is older, more established
* **Ecosystem**: Hadoop/Kafka integration
* **Complexity**: More configuration required

## See Also[​](#see-also "Direct link to See Also")

* [Zookeeper Integration](/4.0.0/docs/integration/zookeeper.md)
* [Eureka Sample](/4.0.0/docs/samples/sample-eureka.md)
* [Consul Sample](/4.0.0/docs/samples/sample-consul.md)
