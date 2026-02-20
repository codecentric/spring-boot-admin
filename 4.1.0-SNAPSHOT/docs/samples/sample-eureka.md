# Eureka Sample

The Eureka sample demonstrates Spring Boot Admin Server integration with Netflix Eureka service discovery. This sample shows how to automatically discover and monitor Spring Boot applications registered with Eureka without using the Admin Client.

## Overview[​](#overview "Direct link to Overview")

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-eureka/`

**Features**:

* Automatic service discovery via Eureka
* No Admin Client required on monitored applications
* Dynamic instance registration and deregistration
* Metadata-based configuration
* Docker Compose setup with multiple services
* Spring Security integration
* Health check integration with Eureka

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17 or higher
* Maven 3.6+
* Docker and Docker Compose (for full stack)
* Eureka Server running (or use Docker Compose)

## Architecture[​](#architecture "Direct link to Architecture")

<!-- -->

**Key Points**:

1. Applications register with Eureka
2. Admin Server discovers applications from Eureka
3. No direct registration needed

## Running the Sample[​](#running-the-sample "Direct link to Running the Sample")

### Option 1: With External Eureka Server[​](#option-1-with-external-eureka-server "Direct link to Option 1: With External Eureka Server")

#### Start Eureka Server[​](#start-eureka-server "Direct link to Start Eureka Server")

```
# Using Docker
docker run -d -p 8761:8761 springcloud/eureka

# Or using Spring Cloud Eureka server JAR
java -jar eureka-server.jar
```

Verify Eureka is running: `http://localhost:8761`

#### Start Admin Server[​](#start-admin-server "Direct link to Start Admin Server")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
mvn spring-boot:run
```

Access Admin UI at: `http://localhost:8080`

### Option 2: Using Docker Compose (Recommended)[​](#option-2-using-docker-compose-recommended "Direct link to Option 2: Using Docker Compose (Recommended)")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
docker-compose up
```

This starts:

* Eureka Server (port 8761)
* Admin Server (port 8080)
* Spring Cloud Config Server (port 8888)
* Sample microservices (customers, stores)
* Supporting infrastructure (MongoDB, RabbitMQ)

**Access Points**:

* Admin UI: `http://localhost:8080`
* Eureka UI: `http://localhost:8761`
* Config Server: `http://localhost:8888`
* Sample App UI: `http://localhost:80`

### Change Eureka URL[​](#change-eureka-url "Direct link to Change Eureka URL")

```
mvn spring-boot:run -Dspring-boot.run.arguments=\
  --eureka.client.serviceUrl.defaultZone=http://other-eureka:8761/eureka/
```

Or set environment variable:

```
export EUREKA_SERVICE_URL=http://other-eureka:8761
mvn spring-boot:run
```

## Project Structure[​](#project-structure "Direct link to Project Structure")

### Dependencies[​](#dependencies "Direct link to Dependencies")

```
<dependencies>
    <!-- Admin Server -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
    </dependency>

    <!-- Eureka Discovery Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Web (Servlet excluded, uses WebFlux) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-tomcat</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

**Note**: Tomcat is excluded, so this sample runs on Netty (reactive stack).

### Main Application Class[​](#main-application-class "Direct link to Main Application Class")

SpringBootAdminEurekaApplication.java

```
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient  // Enable Eureka discovery
@EnableAdminServer      // Enable Admin Server
public class SpringBootAdminEurekaApplication {

    private final AdminServerProperties adminServer;

    public SpringBootAdminEurekaApplication(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminEurekaApplication.class, args);
    }
}
```

**Key Annotations**:

* `@EnableDiscoveryClient`: Enables Eureka client functionality
* `@EnableAdminServer`: Enables Admin Server
* Both work together to discover and monitor services

## Configuration[​](#configuration "Direct link to Configuration")

### Admin Server Configuration[​](#admin-server-configuration "Direct link to Admin Server Configuration")

application.yml

```
spring:
  application:
    name: spring-boot-admin-sample-eureka
  profiles:
    active:
      - secure

eureka:
  instance:
    leaseRenewalIntervalInSeconds: 10  # Heartbeat interval
    health-check-url-path: /actuator/health
    metadata-map:
      startup: ${random.int}  # Trigger refresh on restart
  client:
    registryFetchIntervalSeconds: 5  # Fetch registry every 5s
    serviceUrl:
      defaultZone: ${EUREKA_SERVICE_URL:http://localhost:8761}/eureka/

management:
  endpoints:
    web:
      exposure:
        include: "*"  # Expose all actuator endpoints
  endpoint:
    health:
      show-details: ALWAYS
```

**Configuration Details**:

1. **Lease Renewal**: 10 seconds (faster detection of down instances)
2. **Registry Fetch**: 5 seconds (quick discovery of new services)
3. **Health Check**: Registered with Eureka
4. **Startup Metadata**: Random value triggers endpoint update after restart

### Client Application Configuration[​](#client-application-configuration "Direct link to Client Application Configuration")

For applications to be monitored, they only need:

```
spring:
  application:
    name: my-service  # Service name in Eureka

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    metadata-map:
      management.context-path: /actuator  # Tell Admin where actuator is

management:
  endpoints:
    web:
      exposure:
        include: "*"  # Expose endpoints
```

**No Admin Client dependency needed!**

## Security Configuration[​](#security-configuration "Direct link to Security Configuration")

### Insecure Profile[​](#insecure-profile "Direct link to Insecure Profile")

```
@Bean
@Profile("insecure")
public SecurityWebFilterChain securityWebFilterChainPermitAll(
        ServerHttpSecurity http) {
    return http
        .authorizeExchange((authorizeExchange) ->
            authorizeExchange.anyExchange().permitAll())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .build();
}
```

### Secure Profile (Default)[​](#secure-profile-default "Direct link to Secure Profile (Default)")

```
@Bean
@Profile("secure")
public SecurityWebFilterChain securityWebFilterChainSecure(
        ServerHttpSecurity http) {
    return http
        .authorizeExchange((authorizeExchange) ->
            authorizeExchange
                .pathMatchers(adminServer.path("/assets/**"))
                    .permitAll()
                .pathMatchers("/actuator/health/**")
                    .permitAll()
                .pathMatchers(adminServer.path("/login"))
                    .permitAll()
                .anyExchange()
                    .authenticated())
        .formLogin((formLogin) -> formLogin
            .loginPage(adminServer.path("/login"))
            .authenticationSuccessHandler(loginSuccessHandler(...)))
        .logout((logout) -> logout
            .logoutUrl(adminServer.path("/logout"))
            .logoutSuccessHandler(logoutSuccessHandler(...)))
        .httpBasic(Customizer.withDefaults())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .build();
}
```

## Docker Compose Setup[​](#docker-compose-setup "Direct link to Docker Compose Setup")

### Complete Stack[​](#complete-stack "Direct link to Complete Stack")

The `docker-compose.yml` provides a complete microservices environment:

docker-compose.yml

```
version: '2'

services:
  # Eureka Server
  eureka:
    image: springcloud/eureka
    container_name: eureka
    ports:
      - "8761:8761"
    environment:
      - EUREKA_INSTANCE_PREFERIPADDRESS=true

  # Admin Server
  admin:
    build:
      context: .
      dockerfile: ./src/main/docker/Dockerfile
    depends_on:
      - eureka
    ports:
     - "8080:8080"
    environment:
      - EUREKA_SERVICE_URL=http://eureka:8761
      - EUREKA_INSTANCE_PREFER_IP_ADDRESS=true

  # Spring Cloud Config Server
  config:
    image: springcloud/configserver
    depends_on:
      - eureka
    ports:
      - "8888:8888"
    environment:
      - EUREKA_SERVICE_URL=http://eureka:8761

  # Sample Microservices
  customers:
    image: springcloud/customers
    depends_on:
      - config
      - rabbit
    environment:
      - CONFIG_SERVER_URI=http://config:8888
      - RABBITMQ_HOST=rabbit

  stores:
    image: springcloud/stores
    depends_on:
      - config
      - rabbit
      - mongodb
    environment:
      - CONFIG_SERVER_URI=http://config:8888
      - RABBITMQ_HOST=rabbit
      - MONGODB_HOST=mongodb

  # Infrastructure
  mongodb:
    image: tutum/mongodb
    ports:
      - "27017:27017"
    environment:
      - AUTH=no

  rabbit:
    image: "rabbitmq:4"
    ports:
     - "5672:5672"
```

### Start Stack[​](#start-stack "Direct link to Start Stack")

```
docker-compose up
```

### Stop Stack[​](#stop-stack "Direct link to Stop Stack")

```
docker-compose down
```

### View Logs[​](#view-logs "Direct link to View Logs")

```
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f admin
```

## How It Works[​](#how-it-works "Direct link to How It Works")

### Service Discovery Flow[​](#service-discovery-flow "Direct link to Service Discovery Flow")

1. **Application Startup**:

   * Application starts and registers with Eureka
   * Sends metadata including actuator path
   * Eureka assigns instance ID

2. **Admin Server Discovery**:

   * Admin Server fetches registry from Eureka every 5s
   * Discovers new services
   * Reads metadata to find actuator endpoints

3. **Health Monitoring**:

   * Admin Server polls actuator endpoints
   * Updates instance status
   * Triggers notifications on status changes

4. **Application Shutdown**:

   * Application deregisters from Eureka
   * Admin Server removes instance from monitoring

### Metadata Mapping[​](#metadata-mapping "Direct link to Metadata Mapping")

Admin Server reads specific metadata keys:

```
eureka:
  instance:
    metadata-map:
      # Required for proper endpoint detection
      management.context-path: /actuator
      management.port: 8081  # If different from service port

      # Optional - for authenticated actuators
      user.name: admin
      user.password: ${admin.password}

      # Optional - custom metadata
      startup: ${random.int}  # Triggers refresh
      environment: production
      version: ${project.version}
```

**Important Keys**:

* `management.context-path`: Where actuator endpoints are located
* `management.port`: If management port differs from application port
* `user.name` / `user.password`: Credentials for secured actuators
* `startup`: Random value forces Admin to refresh endpoints after restart

## Testing the Sample[​](#testing-the-sample "Direct link to Testing the Sample")

### Verify Eureka Registration[​](#verify-eureka-registration "Direct link to Verify Eureka Registration")

1. Access Eureka UI: `http://localhost:8761`

2. Check "Instances currently registered with Eureka"

3. Should see:

   <!-- -->

   * `SPRING-BOOT-ADMIN-SAMPLE-EUREKA`
   * Other registered services

### Verify Admin Server Discovery[​](#verify-admin-server-discovery "Direct link to Verify Admin Server Discovery")

1. Access Admin UI: `http://localhost:8080`

2. Should see all Eureka-registered services

3. Click on each service to view:

   <!-- -->

   * Health status
   * Metrics
   * Environment
   * Logs
   * JVM details

### Test Dynamic Discovery[​](#test-dynamic-discovery "Direct link to Test Dynamic Discovery")

#### Register New Service[​](#register-new-service "Direct link to Register New Service")

```
# Start another instance
SERVER_PORT=8081 mvn spring-boot:run
```

Within 5 seconds, it should appear in Admin UI.

#### Deregister Service[​](#deregister-service "Direct link to Deregister Service")

Stop the application (Ctrl+C). Within \~40 seconds (lease timeout), it should disappear from Admin UI.

### Test Health Status Changes[​](#test-health-status-changes "Direct link to Test Health Status Changes")

Stop a monitored service and watch status change from UP → DOWN in Admin UI.

## Advanced Configuration[​](#advanced-configuration "Direct link to Advanced Configuration")

### Custom Service Filtering[​](#custom-service-filtering "Direct link to Custom Service Filtering")

Filter which services to monitor:

```
spring:
  boot:
    admin:
      discovery:
        ignored-services:
          - eureka-server  # Don't monitor Eureka itself
          - config-server  # Don't monitor Config Server
```

### Service Grouping[​](#service-grouping "Direct link to Service Grouping")

Group services using metadata:

```
# Client application
eureka:
  instance:
    metadata-map:
      group: backend-services
      team: platform
```

### Secure Actuator Endpoints[​](#secure-actuator-endpoints "Direct link to Secure Actuator Endpoints")

If client actuators are secured:

```
# Client application
eureka:
  instance:
    metadata-map:
      user.name: actuator-admin
      user.password: ${actuator.password}
```

Admin Server automatically uses these credentials.

### Custom Health Check URL[​](#custom-health-check-url "Direct link to Custom Health Check URL")

```
eureka:
  instance:
    health-check-url-path: /custom/health
    metadata-map:
      management.context-path: /custom
```

## Comparison: Eureka vs. Direct Registration[​](#comparison-eureka-vs-direct-registration "Direct link to Comparison: Eureka vs. Direct Registration")

| Aspect                | Eureka Discovery          | Direct Registration          |
| --------------------- | ------------------------- | ---------------------------- |
| **Setup**             | Eureka Server required    | No additional infrastructure |
| **Client Dependency** | Only Eureka client        | Admin Client required        |
| **Discovery**         | Automatic                 | Manual configuration         |
| **Scalability**       | Excellent (100+ services) | Limited (static config)      |
| **Dynamic Updates**   | Automatic                 | Manual restart               |
| **Use Case**          | Microservices             | Monoliths, small deployments |
| **Complexity**        | Higher                    | Lower                        |

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Admin Server Not Discovering Services[​](#admin-server-not-discovering-services "Direct link to Admin Server Not Discovering Services")

**Check Eureka connectivity**:

```
# Test Eureka API
curl http://localhost:8761/eureka/apps

# Check Admin logs
docker-compose logs admin | grep -i eureka
```

**Common Issues**:

1. Eureka URL incorrect
2. Network connectivity issues
3. Services not exposing actuator endpoints

**Solution**:

```
# Verify configuration
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/  # Trailing slash!
```

### Services Show as DOWN[​](#services-show-as-down "Direct link to Services Show as DOWN")

**Check health endpoint**:

```
curl http://localhost:8080/actuator/health
```

**Verify metadata**:

```
eureka:
  instance:
    metadata-map:
      management.context-path: /actuator  # Must match actual path
```

### Slow Discovery[​](#slow-discovery "Direct link to Slow Discovery")

Services take too long to appear:

```
eureka:
  client:
    registryFetchIntervalSeconds: 5  # Reduce from default 30s
  instance:
    leaseRenewalIntervalInSeconds: 10  # Reduce from default 30s
```

### Docker Compose Issues[​](#docker-compose-issues "Direct link to Docker Compose Issues")

**Port conflicts**:

```
# Change ports in docker-compose.yml
ports:
  - "9090:8080"  # Map to different host port
```

**Container connectivity**:

```
# Check network
docker network inspect spring-boot-admin-sample-eureka_discovery

# Check container logs
docker-compose logs eureka
```

## Production Considerations[​](#production-considerations "Direct link to Production Considerations")

### High Availability[​](#high-availability "Direct link to High Availability")

Run multiple Eureka servers:

```
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
```

### Security[​](#security "Direct link to Security")

Secure Eureka communication:

```
eureka:
  client:
    serviceUrl:
      defaultZone: https://${eureka.user}:${eureka.password}@eureka:8761/eureka/
```

### Performance Tuning[​](#performance-tuning "Direct link to Performance Tuning")

Optimize for large deployments:

```
spring:
  boot:
    admin:
      monitor:
        period: 20000  # Increase polling interval (ms)
        connect-timeout: 5000
        read-timeout: 10000

eureka:
  client:
    registryFetchIntervalSeconds: 10  # Balance freshness vs. load
```

### Monitoring Eureka Itself[​](#monitoring-eureka-itself "Direct link to Monitoring Eureka Itself")

Register Admin Server with itself:

```
spring:
  boot:
    admin:
      discovery:
        ignored-services: []  # Don't ignore any services
```

## Key Takeaways[​](#key-takeaways "Direct link to Key Takeaways")

This sample demonstrates:

✅ **Service Discovery Integration**

* Automatic discovery via Eureka
* No Admin Client dependency needed

✅ **Dynamic Monitoring**

* Services auto-register/deregister
* Real-time discovery updates

✅ **Complete Setup**

* Complete microservices stack
* Docker Compose orchestration

✅ **Scalable Architecture**

* Handles many services efficiently
* Centralized monitoring

## Next Steps[​](#next-steps "Direct link to Next Steps")

* Explore [Consul Sample](/4.1.0-SNAPSHOT/docs/samples/sample-consul.md) for alternative service discovery
* Review [Zookeeper Sample](/4.1.0-SNAPSHOT/docs/samples/sample-zookeeper.md) for Apache Zookeeper
* Check [Integration Guide](/4.1.0-SNAPSHOT/docs/integration/eureka.md) for detailed Eureka setup
* See [Hazelcast Sample](/4.1.0-SNAPSHOT/docs/samples/sample-hazelcast.md) for clustering

## See Also[​](#see-also "Direct link to See Also")

* [Eureka Integration Guide](/4.1.0-SNAPSHOT/docs/integration/eureka.md)
* [Service Discovery](/4.1.0-SNAPSHOT/docs/client/service-discovery.md)
* [Server Configuration](/4.1.0-SNAPSHOT/docs/server/server.md)
* [Netflix Eureka Documentation](https://github.com/Netflix/eureka/wiki)
