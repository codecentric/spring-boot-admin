# Client Registration

To monitor your applications with Spring Boot Admin, they need to register with the Admin Server. There are three main approaches to achieve this:

1. **Spring Boot Admin Client** - Direct registration
2. **Spring Cloud Discovery** - Automatic registration via service discovery
3. **Static Configuration** - Manual configuration on the server side

## Using Spring Boot Admin Client[​](#using-spring-boot-admin-client "Direct link to Using Spring Boot Admin Client")

The Spring Boot Admin Client library enables applications to register themselves directly with the Admin Server.

### Step 1: Add Dependencies[​](#step-1-add-dependencies "Direct link to Step 1: Add Dependencies")

Add the Spring Boot Admin Client starter to your application:

pom.xml

```
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>4.0.0</version>
</dependency>
```

For Gradle:

build.gradle

```
implementation 'de.codecentric:spring-boot-admin-starter-client:4.0.0'
```

### Step 2: Configure the Admin Server URL[​](#step-2-configure-the-admin-server-url "Direct link to Step 2: Configure the Admin Server URL")

Add the Admin Server URL to your `application.properties` or `application.yml`:

application.yml

```
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080  # URL of your Admin Server

management:
  endpoints:
    web:
      exposure:
        include: "*"  # Expose all actuator endpoints
  endpoint:
    health:
      show-details: ALWAYS
  info:
    env:
      enabled: true  # Enable the info endpoint
```

application.properties

```
spring.boot.admin.client.url=http://localhost:8080
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=ALWAYS
management.info.env.enabled=true
```

### Step 3: Start Your Application[​](#step-3-start-your-application "Direct link to Step 3: Start Your Application")

When your application starts, it will automatically register with the Admin Server. You'll see your application appear in the Admin Server's web interface.

### Client Configuration Options[​](#client-configuration-options "Direct link to Client Configuration Options")

#### Custom Instance Metadata[​](#custom-instance-metadata "Direct link to Custom Instance Metadata")

Add custom metadata to your application registration:

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            tags:
              environment: production
              region: us-east-1
            team: platform
```

#### Custom Service URL[​](#custom-service-url "Direct link to Custom Service URL")

Override the service URL that the Admin Server uses to connect:

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          service-url: https://my-app.example.com
          service-host-type: IP  # or CANONICAL
```

#### Registration Interval[​](#registration-interval "Direct link to Registration Interval")

Configure how often the client registers with the server:

application.yml

```
spring:
  boot:
    admin:
      client:
        period: 10000  # milliseconds (default: 10000)
        auto-registration: true  # Enable/disable auto-registration
```

## Using Spring Cloud Discovery[​](#using-spring-cloud-discovery "Direct link to Using Spring Cloud Discovery")

If you're using Spring Cloud Discovery (Eureka, Consul, Zookeeper), you don't need the Spring Boot Admin Client. The Admin Server can discover applications automatically.

### Eureka Example[​](#eureka-example "Direct link to Eureka Example")

#### Step 1: Add Eureka Client Dependency[​](#step-1-add-eureka-client-dependency "Direct link to Step 1: Add Eureka Client Dependency")

Add to your application:

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### Step 2: Configure Eureka[​](#step-2-configure-eureka "Direct link to Step 2: Configure Eureka")

application.yml

```
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}  # Trigger info update after restart

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

#### Step 3: Enable Discovery on Admin Server[​](#step-3-enable-discovery-on-admin-server "Direct link to Step 3: Enable Discovery on Admin Server")

Add Eureka client to your Admin Server:

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Enable discovery in the Admin Server:

SpringBootAdminApplication.java

```
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminApplication {
    static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```

### Consul Example[​](#consul-example "Direct link to Consul Example")

application.yml

```
spring:
  cloud:
    consul:
      discovery:
        metadata:
          user-name: ${spring.security.user.name}
          user-password: ${spring.security.user.password}
```

warning

Consul does not allow dots (".") in metadata keys. Use dashes instead (e.g., `user-name` instead of `user.name`).

### Zookeeper Example[​](#zookeeper-example "Direct link to Zookeeper Example")

For Zookeeper integration, see the [spring-boot-admin-sample-zookeeper](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/) example.

## Static Configuration[​](#static-configuration "Direct link to Static Configuration")

You can configure applications statically on the Admin Server using Spring Cloud's `SimpleDiscoveryClient`.

application.yml (Admin Server)

```
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            my-application:
              - uri: http://localhost:8081
                metadata:
                  management.context-path: /actuator
```

This approach is useful for:

* Legacy applications that can't be modified
* Applications running in environments without service discovery
* Static infrastructure setups

## Securing Client Registration[​](#securing-client-registration "Direct link to Securing Client Registration")

When your Admin Server is secured, clients need credentials to register:

application.yml (Client)

```
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080
        username: admin
        password: secret
```

For more details, see [Security](/4.0.0/docs/server/security.md).

## Exposing Actuator Endpoints[​](#exposing-actuator-endpoints "Direct link to Exposing Actuator Endpoints")

Spring Boot Admin requires access to actuator endpoints. Ensure they are properly exposed:

application.yml

```
management:
  endpoints:
    web:
      exposure:
        include: "*"  # Expose all endpoints
      # Or be more specific:
      # include: health,info,metrics,env,loggers
```

warning

In production, carefully consider which endpoints to expose and implement proper security measures.

## Verifying Registration[​](#verifying-registration "Direct link to Verifying Registration")

After configuring your client:

1. Start your Admin Server
2. Start your client application
3. Navigate to the Admin Server UI (`http://localhost:8080`)
4. Your application should appear in the applications list

Check the logs for registration confirmation:

```
INFO: Application registered itself as <instance-id>
```

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Application Not Appearing[​](#application-not-appearing "Direct link to Application Not Appearing")

* Verify the Admin Server URL is correct
* Check network connectivity between client and server
* Ensure actuator endpoints are exposed
* Review client logs for registration errors
* Verify security credentials if server is secured

### Registration Keeps Failing[​](#registration-keeps-failing "Direct link to Registration Keeps Failing")

* Check if the Admin Server is running
* Verify firewall rules allow communication
* Ensure the management port is accessible
* Check for proxy or network configuration issues

## Next Steps[​](#next-steps "Direct link to Next Steps")

* [Client Features](/4.0.0/docs/client/client-features.md) - Learn about version display, JMX, logs, and tags
* [Client Configuration](/4.0.0/docs/client/configuration.md) - Explore advanced client configuration
* [Service Discovery](/4.0.0/docs/client/service-discovery.md) - Deep dive into Spring Cloud integration

## Example Projects[​](#example-projects "Direct link to Example Projects")

* [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/) - Direct client registration with security
* [spring-boot-admin-sample-eureka](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-eureka/) - Eureka discovery example
* [spring-boot-admin-sample-consul](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-consul/) - Consul discovery example
