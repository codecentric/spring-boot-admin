# Service Discovery Integration

Spring Boot Admin integrates seamlessly with Spring Cloud Discovery services, allowing automatic registration without the Spring Boot Admin Client library.

## Overview[​](#overview "Direct link to Overview")

When using service discovery, the Admin Server discovers applications automatically through the discovery client. This eliminates the need for:

* Spring Boot Admin Client dependency
* Explicit Admin Server URL configuration
* Manual registration code

## Supported Discovery Services[​](#supported-discovery-services "Direct link to Supported Discovery Services")

* **Eureka** (Netflix)
* **Consul** (HashiCorp)
* **Zookeeper** (Apache)
* **Kubernetes** (via Spring Cloud Kubernetes)

## Eureka Integration[​](#eureka-integration "Direct link to Eureka Integration")

### Server Setup[​](#server-setup "Direct link to Server Setup")

Add Eureka Client to your Admin Server:

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Enable discovery in your Admin Server:

SpringBootAdminApplication.java

```
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@EnableDiscoveryClient
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminApplication {
    static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```

Configure Eureka connection:

application.yml

```
spring:
  application:
    name: spring-boot-admin-server

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    registryFetchIntervalSeconds: 5
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

### Client Setup[​](#client-setup "Direct link to Client Setup")

Add Eureka Client to your application (no Admin Client needed):

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Enable discovery:

```
@EnableDiscoveryClient
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Configure Eureka and expose endpoints:

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
      startup: ${random.int}  # Triggers info update on restart
      user.name: ${spring.security.user.name}  # For secured actuators
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

### Eureka Metadata[​](#eureka-metadata "Direct link to Eureka Metadata")

Add custom metadata through Eureka:

application.yml

```
eureka:
  instance:
    metadata-map:
      startup: ${random.int}
      tags.environment: production
      tags.region: us-east-1
      team: platform
      version: ${spring.application.version}
```

## Consul Integration[​](#consul-integration "Direct link to Consul Integration")

### Server Setup[​](#server-setup-1 "Direct link to Server Setup")

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
```

```
@EnableDiscoveryClient
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminApplication {
    static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```

application.yml

```
spring:
  application:
    name: spring-boot-admin-server
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        prefer-ip-address: true
        health-check-interval: 10s
```

### Client Setup[​](#client-setup-1 "Direct link to Client Setup")

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
```

application.yml

```
spring:
  application:
    name: my-application
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        metadata:
          user-name: ${spring.security.user.name}  # Note: dashes not dots!
          user-password: ${spring.security.user.password}
          environment: production
          management-context-path: ${management.server.base-path:/actuator}

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

warning

Consul does not allow dots (`.`) in metadata keys. Use dashes (`-`) or underscores (`_`) instead:

* ✅ `user-name` or `user_name`
* ❌ `user.name`

## Zookeeper Integration[​](#zookeeper-integration "Direct link to Zookeeper Integration")

### Server Setup[​](#server-setup-2 "Direct link to Server Setup")

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
```

application.yml

```
spring:
  application:
    name: spring-boot-admin-server
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        enabled: true
```

### Client Setup[​](#client-setup-2 "Direct link to Client Setup")

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
```

application.yml

```
spring:
  application:
    name: my-application
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        metadata:
          user.name: ${spring.security.user.name}
          user.password: ${spring.security.user.password}
          management.context-path: /actuator
```

## Filtering Discovered Services[​](#filtering-discovered-services "Direct link to Filtering Discovered Services")

By default, the Admin Server monitors all discovered services. You can filter services using the `InstanceFilter` interface.

### Configuration-Based Filtering[​](#configuration-based-filtering "Direct link to Configuration-Based Filtering")

application.yml

```
spring:
  boot:
    admin:
      discovery:
        ignored-services: consul,eureka,zookeeper  # Don't monitor discovery services
```

### Custom InstanceFilter[​](#custom-instancefilter "Direct link to Custom InstanceFilter")

```
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import org.springframework.stereotype.Component;

@Component
public class CustomInstanceFilter implements InstanceFilter {

    @Override
    public boolean test(Registration registration) {
        String name = registration.getName();

        // Ignore internal services
        if (name.startsWith("internal-")) {
            return false;
        }

        // Only monitor services with specific metadata
        String monitorable = registration.getMetadata().get("monitorable");
        if (!"true".equals(monitorable)) {
            return false;
        }

        return true;
    }
}
```

## Instance Preference Strategy[​](#instance-preference-strategy "Direct link to Instance Preference Strategy")

When multiple instances of the same service exist, configure which instance URL to use:

application.yml

```
spring:
  boot:
    admin:
      discovery:
        instance-prefer-ip: true  # Use IP instead of hostname
```

## Management Context Path[​](#management-context-path "Direct link to Management Context Path")

If your management endpoints are on a different port or path:

application.yml (Client)

```
management:
  server:
    port: 9090  # Management on different port
    base-path: /management

eureka:
  instance:
    metadata-map:
      management.port: 9090
      management.context-path: /management
```

## Health Check Configuration[​](#health-check-configuration "Direct link to Health Check Configuration")

Configure health check paths for discovery:

application.yml

```
eureka:
  instance:
    health-check-url-path: /actuator/health
    health-check-url: http://my-app.example.com/actuator/health
    status-page-url-path: /actuator/info
    home-page-url: /
```

## Service URL vs Management URL[​](#service-url-vs-management-url "Direct link to Service URL vs Management URL")

Discovery services may return different URLs for the service and management endpoints:

application.yml

```
eureka:
  instance:
    metadata-map:
      management.context-path: /actuator  # Management endpoint path
      service-url: https://my-app.example.com  # Public service URL
      management-url: http://internal-app:8080/actuator  # Internal mgmt URL
```

## Securing Discovered Services[​](#securing-discovered-services "Direct link to Securing Discovered Services")

Pass credentials through metadata:

application.yml

```
eureka:
  instance:
    metadata-map:
      user.name: admin
      user.password: ${ACTUATOR_PASSWORD}
```

Or configure on the Admin Server:

application.yml (Admin Server)

```
spring:
  boot:
    admin:
      instance-auth:
        enabled: true
        default-user-name: admin
        default-password: ${DEFAULT_PASSWORD}
        service-map:
          my-application:
            user-name: app-admin
            user-password: ${APP_PASSWORD}
```

## Advantages of Service Discovery[​](#advantages-of-service-discovery "Direct link to Advantages of Service Discovery")

1. **No Client Library Required**: Applications don't need Spring Boot Admin Client
2. **Automatic Discovery**: New instances automatically appear
3. **Centralized Configuration**: Manage discovery in one place
4. **Load Balancing**: Discovery services handle load balancing
5. **Health Checks**: Built-in health check integration
6. **Service Metadata**: Rich metadata support

## Disadvantages[​](#disadvantages "Direct link to Disadvantages")

1. **Additional Infrastructure**: Requires running discovery service
2. **Network Complexity**: Additional network hop
3. **Discovery Lag**: Slight delay in detecting new instances
4. **Metadata Limitations**: Some discovery services have metadata restrictions

## Mixed Mode[​](#mixed-mode "Direct link to Mixed Mode")

You can use both service discovery and direct client registration simultaneously:

application.yml

```
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080  # Direct registration
        auto-registration: true

eureka:
  client:
    enabled: true  # Also register with Eureka
```

This provides redundancy if one registration method fails.

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Application Not Discovered[​](#application-not-discovered "Direct link to Application Not Discovered")

1. **Check Discovery Registration**:

   ```
   # For Eureka
   curl http://localhost:8761/eureka/apps
   ```

2. **Verify Admin Server Discovery Client**: Ensure `@EnableDiscoveryClient` is present

3. **Check Network Connectivity**: Admin Server must reach discovery service

4. **Review Metadata**: Ensure management URLs are correct

### Incorrect Management URL[​](#incorrect-management-url "Direct link to Incorrect Management URL")

Set explicit management metadata:

```
eureka:
  instance:
    metadata-map:
      management.port: ${management.server.port}
      management.context-path: ${management.server.base-path}
```

### Health Check Failures[​](#health-check-failures "Direct link to Health Check Failures")

Ensure health endpoint is accessible:

```
management:
  endpoint:
    health:
      show-details: ALWAYS
  endpoints:
    web:
      exposure:
        include: health,info
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Use Metadata for Configuration**: Leverage metadata for flexible configuration
2. **Set Appropriate Intervals**: Balance between freshness and load
3. **Implement Filters**: Don't monitor unnecessary services
4. **Secure Metadata Transmission**: Use secure discovery service connections
5. **Monitor Discovery Health**: Ensure discovery service is healthy
6. **Document Metadata Schema**: Maintain clear metadata conventions
7. **Test Failover**: Verify behavior when discovery service is down

## See Also[​](#see-also "Direct link to See Also")

* [Registration](/4.0.1/docs/client/registration.md) - Direct client registration
* [Metadata](/4.0.1/docs/client/metadata.md) - Working with metadata
* [Eureka Sample](/4.0.1/docs/samples/sample-eureka.md) - Complete Eureka example
* [Consul Sample](/4.0.1/docs/samples/sample-consul.md) - Complete Consul example
* [Security](/4.0.1/docs/server/security.md) - Securing discovered services
