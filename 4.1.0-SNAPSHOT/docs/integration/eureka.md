# Eureka Integration

Netflix Eureka is a service discovery and registration solution that integrates seamlessly with Spring Boot Admin. This guide shows how to set up automatic application discovery using Eureka.

## Overview[​](#overview "Direct link to Overview")

With Eureka integration:

* Applications register with Eureka server
* Spring Boot Admin Server discovers applications automatically
* No Spring Boot Admin Client library needed
* Applications appear/disappear based on Eureka registration status

## Architecture[​](#architecture "Direct link to Architecture")

<!-- -->

Applications register with Eureka, and the Admin Server discovers them through Eureka's registry.

## Setting Up Eureka Server[​](#setting-up-eureka-server "Direct link to Setting Up Eureka Server")

First, you need a Eureka Server running. Here's a minimal setup:

### Dependencies[​](#dependencies "Direct link to Dependencies")

pom.xml

```

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

### Configuration[​](#configuration "Direct link to Configuration")

EurekaServerApplication.java

```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
	static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
```

application.yml

```
server:
  port: 8761

eureka:
  client:
    registerWithEureka: false
    fetchRegistry: false
  server:
    enableSelfPreservation: false
```

## Configuring Spring Boot Admin Server[​](#configuring-spring-boot-admin-server "Direct link to Configuring Spring Boot Admin Server")

### Add Dependencies[​](#add-dependencies "Direct link to Add Dependencies")

Add Eureka client to your Admin Server:

pom.xml

```

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
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

### Enable Discovery[​](#enable-discovery "Direct link to Enable Discovery")

Enable both Admin Server and Eureka Discovery:

SpringBootAdminEurekaApplication.java

```
import de.codecentric.boot.admin.server.config.EnableAdminServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminEurekaApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminEurekaApplication.class, args);
	}
}
```

### Configure Eureka Client[​](#configure-eureka-client "Direct link to Configure Eureka Client")

application.yml

```
spring:
  application:
    name: spring-boot-admin-server

eureka:
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}  # Trigger info update on restart
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

## Configuring Client Applications[​](#configuring-client-applications "Direct link to Configuring Client Applications")

Applications only need Eureka Client - no Spring Boot Admin Client required!

### Add Dependencies[​](#add-dependencies-1 "Direct link to Add Dependencies")

pom.xml

```

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### Enable Discovery[​](#enable-discovery-1 "Direct link to Enable Discovery")

Application.java

```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Application {
	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

### Configure Application[​](#configure-application "Direct link to Configure Application")

application.yml

```
spring:
  application:
    name: my-application

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}  # Triggers info/endpoint update on restart

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

## Metadata Configuration[​](#metadata-configuration "Direct link to Metadata Configuration")

### Adding Custom Metadata[​](#adding-custom-metadata "Direct link to Adding Custom Metadata")

Pass custom metadata through Eureka registration:

application.yml

```
eureka:
  instance:
    metadata-map:
      startup: ${random.int}
      tags.environment: production
      tags.region: us-east-1
      team: platform
      version: 1.0.0
```

### Security Credentials[​](#security-credentials "Direct link to Security Credentials")

For secured actuator endpoints, pass credentials via metadata:

application.yml

```
eureka:
  instance:
    metadata-map:
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}
```

warning

Credentials in metadata are visible to anyone who can query Eureka. Use HTTPS and secure your Eureka server appropriately.

### Management Port Configuration[​](#management-port-configuration "Direct link to Management Port Configuration")

If management endpoints are on a different port:

application.yml

```
server:
  port: 8080

management:
  server:
    port: 9090
  endpoints:
    web:
      base-path: /actuator

eureka:
  instance:
    metadata-map:
      management.port: 9090
      management.context-path: /actuator
```

## Service URL Configuration[​](#service-url-configuration "Direct link to Service URL Configuration")

### Custom Service URL[​](#custom-service-url "Direct link to Custom Service URL")

Override the service URL Spring Boot Admin uses:

application.yml

```
eureka:
  instance:
    metadata-map:
      service-url: https://my-app.example.com
      management-url: http://internal-app:9090/actuator
```

### Prefer IP Address[​](#prefer-ip-address "Direct link to Prefer IP Address")

Use IP address instead of hostname:

application.yml

```
eureka:
  instance:
    preferIpAddress: true
```

On the Admin Server:

application.yml

```
spring:
  boot:
    admin:
      discovery:
        instancePreferIp: true
```

## Filtering Services[​](#filtering-services "Direct link to Filtering Services")

### Ignore Specific Services[​](#ignore-specific-services "Direct link to Ignore Specific Services")

Don't monitor certain services:

application.yml (Admin Server)

```
spring:
  boot:
    admin:
      discovery:
        ignored-services: eureka,config-server,gateway
```

### Custom Instance Filter[​](#custom-instance-filter "Direct link to Custom Instance Filter")

```
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstanceFilterConfig {

	@Bean
	public InstanceFilter customInstanceFilter() {
		return registration -> {
			String name = registration.getName();

			// Don't monitor infrastructure services
			if (name.startsWith("eureka") ||
					name.startsWith("config") ||
					name.startsWith("gateway")) {
				return false;
			}

			// Only monitor services with specific metadata
			String monitorable = registration.getMetadata().get("monitor");
			return "true".equals(monitorable);
		};
	}
}
```

## Health Check Configuration[​](#health-check-configuration "Direct link to Health Check Configuration")

### Custom Health Check Path[​](#custom-health-check-path "Direct link to Custom Health Check Path")

application.yml

```
eureka:
  instance:
    health-check-url-path: /actuator/health
    health-check-url: http://my-app.example.com/actuator/health
```

### Status Page URL[​](#status-page-url "Direct link to Status Page URL")

application.yml

```
eureka:
  instance:
    status-page-url-path: /actuator/info
    status-page-url: http://my-app.example.com/actuator/info
```

## Securing Eureka Discovery[​](#securing-eureka-discovery "Direct link to Securing Eureka Discovery")

### Basic Authentication[​](#basic-authentication "Direct link to Basic Authentication")

Secure Eureka server with basic auth:

application.yml (Admin Server)

```
eureka:
  client:
    serviceUrl:
      defaultZone: http://user:password@localhost:8761/eureka/
```

### Mutual TLS[​](#mutual-tls "Direct link to Mutual TLS")

Configure SSL for Eureka communication:

application.yml

```
eureka:
  client:
    serviceUrl:
      defaultZone: https://localhost:8761/eureka/
    tls:
      enabled: true
      key-store: classpath:keystore.p12
      key-store-password: changeit
      trust-store: classpath:truststore.jks
      trust-store-password: changeit
```

## Docker Compose Example[​](#docker-compose-example "Direct link to Docker Compose Example")

docker-compose.yml

```
version: '3'

services:
  eureka:
    image: springcloud/eureka
    ports:
      - "8761:8761"
    environment:
      - EUREKA_INSTANCE_HOSTNAME=eureka

  spring-boot-admin:
    build: ./admin-server
    ports:
      - "8080:8080"
    environment:
      - EUREKA_SERVICE_URL=http://eureka:8761
    depends_on:
      - eureka

  my-application:
    build: ./my-app
    ports:
      - "8081:8081"
    environment:
      - EUREKA_SERVICE_URL=http://eureka:8761
    depends_on:
      - eureka
      - spring-boot-admin
```

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Application Not Appearing[​](#application-not-appearing "Direct link to Application Not Appearing")

1. **Check Eureka Registration**:

   ```
   curl http://localhost:8761/eureka/apps
   ```

2. **Verify Admin Server sees Eureka apps**: Check Admin Server logs for discovery messages

3. **Confirm metadata is correct**:

   ```
   curl http://localhost:8761/eureka/apps/MY-APPLICATION | grep metadata
   ```

### Incorrect Management URL[​](#incorrect-management-url "Direct link to Incorrect Management URL")

Ensure management metadata is set:

```
eureka:
  instance:
    metadata-map:
      management.port: ${management.server.port}
      management.context-path: ${management.server.base-path}
```

### Health Check Failures[​](#health-check-failures "Direct link to Health Check Failures")

Verify health endpoint is accessible:

```
curl http://localhost:8081/actuator/health
```

Ensure Eureka health check path matches:

```
eureka:
  instance:
    health-check-url-path: /actuator/health
```

### Stale Instances[​](#stale-instances "Direct link to Stale Instances")

Eureka may keep instances in registry after shutdown. Configure self-preservation:

application.yml (Eureka Server)

```
eureka:
  server:
    enableSelfPreservation: false  # Disable for development
    evictionIntervalTimerInMs: 5000
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Set Appropriate Intervals**: Balance between freshness and load

   ```
   eureka:
     instance:
       leaseRenewalIntervalInSeconds: 10
     client:
       registryFetchIntervalSeconds: 5
   ```

2. **Use Startup Metadata**: Trigger updates on restart

   ```
   eureka:
     instance:
       metadata-map:
         startup: ${random.int}
   ```

3. **Expose Necessary Endpoints**: Only expose what's needed

   ```
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics
   ```

4. **Secure Metadata**: Use HTTPS for sensitive data

   ```
   eureka:
     client:
       serviceUrl:
         defaultZone: https://eureka:8761/eureka/
   ```

5. **Monitor Eureka Health**: Ensure Eureka is healthy

   ```
   management:
     health:
       eureka:
         enabled: true
   ```

6. **Use Instance Filters**: Don't monitor everything

   ```
   @Bean
   public InstanceFilter filter() {
       return registration -> !registration.getName().startsWith("internal-");
   }
   ```

7. **Configure Timeouts**: Prevent hanging requests

   ```
   eureka:
     client:
       eureka-server-connect-timeout-seconds: 5
       eureka-server-read-timeout-seconds: 8
   ```

## Complete Example[​](#complete-example "Direct link to Complete Example")

See the [spring-boot-admin-sample-eureka](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-eureka/) project for a complete working example.

## See Also[​](#see-also "Direct link to See Also")

* [Service Discovery](/4.1.0-SNAPSHOT/docs/client/service-discovery.md) - Service discovery overview
* [Eureka Sample](/4.1.0-SNAPSHOT/docs/samples/sample-eureka.md) - Detailed sample walkthrough
* [Security](/4.1.0-SNAPSHOT/docs/server/security.md) - Securing discovered services
* [Metadata](/4.1.0-SNAPSHOT/docs/client/metadata.md) - Working with metadata
