# Reactive Sample

The Reactive sample demonstrates a Spring Boot Admin Server deployment using Spring WebFlux, the reactive, non-blocking web framework. This sample showcases how to run Admin Server in a fully reactive environment with minimal dependencies.

## Overview[​](#overview "Direct link to Overview")

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-reactive/`

**Features**:

* Reactive stack using Spring WebFlux
* Non-blocking I/O operations
* Spring Security for WebFlux
* Self-monitoring using Admin Client
* Profile-based security configuration
* Minimal dependencies
* DevTools support for development

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17 or higher
* Maven 3.6+

## Running the Sample[​](#running-the-sample "Direct link to Running the Sample")

### Quick Start (Insecure Mode)[​](#quick-start-insecure-mode "Direct link to Quick Start (Insecure Mode)")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-reactive
mvn spring-boot:run
```

Access the application at: `http://localhost:8080`

The application runs with the `insecure` profile by default, allowing access without authentication.

### With Security Enabled[​](#with-security-enabled "Direct link to With Security Enabled")

```
mvn spring-boot:run -Dspring-boot.run.profiles=secure
```

**Login Credentials**: Configure in `application.yml` or use default Spring Security credentials

### Change Port[​](#change-port "Direct link to Change Port")

```
SERVER_PORT=9090 mvn spring-boot:run
```

## Key Differences from Servlet Sample[​](#key-differences-from-servlet-sample "Direct link to Key Differences from Servlet Sample")

### Dependencies[​](#dependencies "Direct link to Dependencies")

The reactive sample uses minimal dependencies:

```
<dependencies>
    <!-- Admin Server -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
    </dependency>

    <!-- Admin Client (for self-monitoring) -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-client</artifactId>
    </dependency>

    <!-- Security for WebFlux -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- DevTools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**Notice**: No explicit WebFlux dependency needed - it's pulled in transitively by `spring-boot-admin-starter-server` when no servlet container is present.

### Reactive Architecture[​](#reactive-architecture "Direct link to Reactive Architecture")

The reactive sample leverages:

* **Non-blocking I/O**: All HTTP requests are handled reactively
* **Backpressure**: Built-in flow control for data streams
* **Event Loop**: Efficient thread utilization with Netty
* **Reactive Types**: `Mono` and `Flux` for asynchronous operations

## Application Structure[​](#application-structure "Direct link to Application Structure")

### Main Application Class[​](#main-application-class "Direct link to Main Application Class")

SpringBootAdminReactiveApplication.java

```
@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminReactiveApplication {

    private final AdminServerProperties adminServer;

    public SpringBootAdminReactiveApplication(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    static void main(String[] args) {
        SpringApplication.run(SpringBootAdminReactiveApplication.class, args);
    }

    @Bean
    public Notifier notifier() {
        return (e) -> Mono.empty();  // No-op notifier
    }
}
```

**Key Points**:

* `@EnableAdminServer` enables Admin Server functionality
* AdminServerProperties injected for security configuration
* Simple no-op notifier returns `Mono.empty()`

## Security Configuration[​](#security-configuration "Direct link to Security Configuration")

### Insecure Profile (Default)[​](#insecure-profile-default "Direct link to Insecure Profile (Default)")

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

**Characteristics**:

* All endpoints accessible without authentication
* CSRF protection disabled
* Useful for development and testing

Development Only

The insecure profile should only be used for local development and testing. Always enable security when deploying.

### Secure Profile[​](#secure-profile "Direct link to Secure Profile")

```
@Bean
@Profile("secure")
public SecurityWebFilterChain securityWebFilterChainSecure(
        ServerHttpSecurity http) {
    return http
        .authorizeExchange((authorizeExchange) ->
            authorizeExchange
                .pathMatchers(adminServer.path("/assets/**"))
                    .permitAll()  // Static resources
                .pathMatchers("/actuator/health/**")
                    .permitAll()  // Health endpoint
                .pathMatchers(adminServer.path("/login"))
                    .permitAll()  // Login page
                .anyExchange()
                    .authenticated())  // Everything else requires auth
        .formLogin((formLogin) -> formLogin
            .loginPage(adminServer.path("/login"))
            .authenticationSuccessHandler(
                loginSuccessHandler(adminServer.path("/"))))
        .logout((logout) -> logout
            .logoutUrl(adminServer.path("/logout"))
            .logoutSuccessHandler(
                logoutSuccessHandler(adminServer.path("/login?logout"))))
        .httpBasic(Customizer.withDefaults())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)  // Simplified for demo
        .build();
}

private ServerAuthenticationSuccessHandler loginSuccessHandler(String uri) {
    RedirectServerAuthenticationSuccessHandler successHandler =
        new RedirectServerAuthenticationSuccessHandler();
    successHandler.setLocation(URI.create(uri));
    return successHandler;
}

private ServerLogoutSuccessHandler logoutSuccessHandler(String uri) {
    RedirectServerLogoutSuccessHandler successHandler =
        new RedirectServerLogoutSuccessHandler();
    successHandler.setLogoutSuccessUrl(URI.create(uri));
    return successHandler;
}
```

**Security Features**:

1. **Form Login**: Custom login page at Admin Server path
2. **HTTP Basic**: Support for basic authentication
3. **Public Endpoints**: Static resources, health, and login page
4. **Custom Redirects**: Success handlers for login/logout
5. **Path-based Authorization**: Uses `ServerHttpSecurity` for reactive security

Reactive Security

Notice the use of `SecurityWebFilterChain` and `ServerHttpSecurity` instead of servlet-based `SecurityFilterChain` and `HttpSecurity`.

## Configuration[​](#configuration "Direct link to Configuration")

### Application Configuration[​](#application-configuration "Direct link to Application Configuration")

application.yml

```
spring:
  application:
    name: spring-boot-admin-sample-reactive

  boot:
    admin:
      client:
        url: http://localhost:8080  # Self-registration

  profiles:
    active:
      - insecure  # Default profile

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS

logging:
  file:
    name: "target/boot-admin-sample-reactive.log"
```

**Configuration Highlights**:

* Self-monitoring via Admin Client
* All actuator endpoints exposed
* Health details always shown
* Insecure profile active by default

## Reactive Stack Benefits[​](#reactive-stack-benefits "Direct link to Reactive Stack Benefits")

### 1. Non-Blocking I/O[​](#1-non-blocking-io "Direct link to 1. Non-Blocking I/O")

All operations are non-blocking:

```
// Instance queries are reactive
Flux<Instance> instances = instanceRepository.findAll();

// Event streams are reactive
Flux<InstanceEvent> events = eventStore.findAll();

// HTTP calls are reactive
Mono<ClientResponse> response = webClient
    .get()
    .uri("/actuator/health")
    .exchange();
```

### 2. Efficient Resource Usage[​](#2-efficient-resource-usage "Direct link to 2. Efficient Resource Usage")

* **Thread Pool**: Small fixed thread pool (typically 2x CPU cores)
* **Memory**: Lower memory footprint
* **Scalability**: Handles more concurrent connections with fewer threads

### 3. Backpressure Support[​](#3-backpressure-support "Direct link to 3. Backpressure Support")

The reactive stack automatically handles backpressure:

```
// Slow consumers won't overwhelm fast producers
eventStore.findAll()
    .limitRate(100)  // Process 100 events at a time
    .subscribe(event -> processEvent(event));
```

### 4. Better for Microservices[​](#4-better-for-microservices "Direct link to 4. Better for Microservices")

* **Resilience**: Non-blocking calls prevent thread exhaustion
* **Latency**: Better tail latency under load
* **Throughput**: Higher throughput for I/O-bound operations

## Testing the Sample[​](#testing-the-sample "Direct link to Testing the Sample")

### Access the UI[​](#access-the-ui "Direct link to Access the UI")

1. Start the application
2. Navigate to `http://localhost:8080`
3. No login required (insecure mode)

### Test Self-Monitoring[​](#test-self-monitoring "Direct link to Test Self-Monitoring")

The application monitors itself:

* Application name: `spring-boot-admin-sample-reactive`
* Status: UP
* All actuator endpoints available
* Check logs: `target/boot-admin-sample-reactive.log`

### Test Reactive Behavior[​](#test-reactive-behavior "Direct link to Test Reactive Behavior")

Monitor thread usage:

```
# Check thread count (should be low)
jcmd <pid> Thread.print | grep "nioEventLoopGroup" | wc -l
```

Expected: \~4-8 threads vs. hundreds in servlet mode under load

### Performance Testing[​](#performance-testing "Direct link to Performance Testing")

Compare reactive vs. servlet performance:

```
# Reactive sample
ab -n 10000 -c 100 http://localhost:8080/actuator/health

# Servlet sample
ab -n 10000 -c 100 http://localhost:8081/actuator/health
```

Reactive should handle higher concurrency with fewer resources.

## Build and Deploy[​](#build-and-deploy "Direct link to Build and Deploy")

### Build JAR[​](#build-jar "Direct link to Build JAR")

```
mvn clean package
```

Produces: `target/spring-boot-admin-sample-reactive.jar`

### Run JAR[​](#run-jar "Direct link to Run JAR")

```
java -jar target/spring-boot-admin-sample-reactive.jar
```

### With Security Profile[​](#with-security-profile "Direct link to With Security Profile")

```
java -jar target/spring-boot-admin-sample-reactive.jar \
  --spring.profiles.active=secure
```

### Production Configuration[​](#production-configuration "Direct link to Production Configuration")

Example production configuration:

```
spring:
  profiles:
    active:
      - secure  # Enable security

  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}

server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}

management:
  server:
    port: 8081  # Separate management port
```

## Comparison: Reactive vs. Servlet[​](#comparison-reactive-vs-servlet "Direct link to Comparison: Reactive vs. Servlet")

| Aspect           | Reactive Sample             | Servlet Sample                    |
| ---------------- | --------------------------- | --------------------------------- |
| **Web Stack**    | WebFlux (Netty)             | Spring MVC (Tomcat)               |
| **Thread Model** | Event loop (4-8 threads)    | Thread per request (200+ threads) |
| **I/O Model**    | Non-blocking                | Blocking                          |
| **Memory**       | Lower footprint             | Higher footprint                  |
| **Scalability**  | High (10K+ connections)     | Medium (100s of connections)      |
| **Complexity**   | Higher learning curve       | Traditional, simpler              |
| **Dependencies** | Minimal                     | More dependencies                 |
| **Use Case**     | High concurrency, I/O-bound | CPU-bound, traditional apps       |

## When to Use Reactive Sample[​](#when-to-use-reactive-sample "Direct link to When to Use Reactive Sample")

✅ **Use Reactive When**:

* Monitoring many instances (100+)
* High concurrency requirements
* Microservices architecture
* Cloud-native deployments
* Limited resources (memory/CPU)
* I/O-bound workloads

❌ **Use Servlet When**:

* Traditional monolithic applications
* Team unfamiliar with reactive programming
* Heavy CPU-bound processing
* Existing servlet-based infrastructure
* Simpler debugging requirements

## Common Issues[​](#common-issues "Direct link to Common Issues")

### ClassNotFoundException[​](#classnotfoundexception "Direct link to ClassNotFoundException")

If you see WebFlux-related errors:

```
java.lang.ClassNotFoundException: reactor.netty.http.server.HttpServer
```

**Solution**: Ensure no servlet dependencies are present:

```
<!-- Remove if present -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

### Port Conflict[​](#port-conflict "Direct link to Port Conflict")

If port 8080 is in use:

```
SERVER_PORT=9090 mvn spring-boot:run
```

### Security Configuration Not Applied[​](#security-configuration-not-applied "Direct link to Security Configuration Not Applied")

If security profile doesn't work:

```
# Verify active profiles
curl http://localhost:8080/actuator/env | jq '.activeProfiles'
```

Ensure profile is set correctly in `application.yml` or via command line.

## Customization Ideas[​](#customization-ideas "Direct link to Customization Ideas")

### Add Custom Reactive Notifier[​](#add-custom-reactive-notifier "Direct link to Add Custom Reactive Notifier")

```
@Bean
public Notifier customReactiveNotifier() {
    return (event) -> {
        return webClient
            .post()
            .uri("https://webhook.site/...")
            .bodyValue(event)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(e -> {
                log.error("Notification failed", e);
                return Mono.empty();
            });
    };
}
```

### Add WebClient Customization[​](#add-webclient-customization "Direct link to Add WebClient Customization")

```
@Bean
public InstanceWebClientCustomizer customTimeout() {
    return (builder) -> builder
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
        ));
}
```

### Add Reactive Health Indicator[​](#add-reactive-health-indicator "Direct link to Add Reactive Health Indicator")

```
@Component
public class CustomHealthIndicator implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return Mono.just(Health.up()
            .withDetail("custom", "Reactive health check")
            .build());
    }
}
```

## Key Takeaways[​](#key-takeaways "Direct link to Key Takeaways")

This sample demonstrates:

✅ **Reactive Architecture**

* Non-blocking I/O with WebFlux
* Efficient resource utilization

✅ **Security Options**

* Profile-based security configuration
* Reactive security filters

✅ **Minimal Dependencies**

* Lightweight deployment
* Faster startup time

✅ **Fully Configured**

* Self-monitoring capability
* Scalable architecture

## Next Steps[​](#next-steps "Direct link to Next Steps")

* Explore [Servlet Sample](/4.1.0-SNAPSHOT/docs/samples/sample-servlet.md) for traditional deployment
* Review [Eureka Sample](/4.1.0-SNAPSHOT/docs/samples/sample-eureka.md) for service discovery
* Check [Hazelcast Sample](/4.1.0-SNAPSHOT/docs/samples/sample-hazelcast.md) for clustering
* Read [Customization Guide](/4.1.0-SNAPSHOT/docs/06-customization/) for extensions

## See Also[​](#see-also "Direct link to See Also")

* [Server Configuration](/4.1.0-SNAPSHOT/docs/server/server.md)
* [Client Registration](/4.1.0-SNAPSHOT/docs/client/registration.md)
* [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
