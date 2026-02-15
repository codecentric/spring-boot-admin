---

sidebar_position: 40
sidebar_custom_props:
  icon: 'file-code'
---

# Consul Sample

The Consul sample demonstrates Spring Boot Admin Server integration with HashiCorp Consul for service discovery. This
sample shows how to leverage Consul's powerful service registry and health checking capabilities to automatically
discover and monitor Spring Boot applications.

## Overview

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-consul/`

**Features**:

- Automatic service discovery via Consul
- No Admin Client required on monitored applications
- Consul health check integration
- Metadata-based configuration
- Custom actuator endpoint paths
- Spring Security integration
- Servlet-based deployment

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Consul installed and running

## Installing Consul

### macOS

```bash
brew install consul
```

### Linux

```bash
wget https://releases.hashicorp.com/consul/1.17.0/consul_1.17.0_linux_amd64.zip
unzip consul_1.17.0_linux_amd64.zip
sudo mv consul /usr/local/bin/
```

### Windows

Download from: https://www.consul.io/downloads

### Docker

```bash
docker run -d -p 8500:8500 -p 8600:8600/udp --name=consul consul agent -server -ui -bootstrap-expect=1 -client=0.0.0.0
```

### Verify Installation

```bash
consul version
```

## Running the Sample

### Start Consul

```bash
# Development mode (single node)
consul agent -dev
```

Verify Consul is running: `http://localhost:8500/ui`

### Start Admin Server

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-consul
mvn spring-boot:run
```

Access Admin UI at: `http://localhost:8080`

### With Different Consul Host

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=\
  --spring.cloud.consul.host=consul-server
```

### Insecure Mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=insecure
```

## Project Structure

### Dependencies

```xml
<dependencies>
    <!-- Admin Server -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
    </dependency>

    <!-- Consul Discovery -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>

    <!-- Web (Servlet) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

### Main Application Class

```java title="SpringBootAdminConsulApplication.java"
@SpringBootApplication
@EnableDiscoveryClient  // Enable Consul discovery
@EnableAdminServer      // Enable Admin Server
public class SpringBootAdminConsulApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringBootAdminConsulApplication.class, args);
    }
}
```

## Configuration

### Admin Server Configuration

```yaml title="application.yml"
spring:
  application:
    name: consul-example

  cloud:
    config:
      enabled: false  # Disable config client
    consul:
      host: localhost
      port: 8500
      discovery:
        metadata:
          # IMPORTANT: Use dashes, not dots in metadata keys!
          management-context-path: /foo
          health-path: /ping
          user-name: user
          user-password: password

  profiles:
    active:
      - secure

  boot:
    admin:
      discovery:
        ignored-services: consul  # Don't monitor Consul itself

management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /foo  # Custom actuator base path
      path-mapping:
        health: /ping  # Custom health endpoint path
  endpoint:
    health:
      show-details: ALWAYS
```

:::warning Metadata Key Restriction
**CRITICAL**: Consul metadata keys **cannot contain dots**. Use dashes instead:

- ✅ `management-context-path`
- ❌ `management.context-path`

This is a Consul limitation, not a Spring Boot Admin limitation.
:::

### Client Application Configuration

For applications to be monitored:

```yaml
spring:
  application:
    name: my-service

  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        metadata:
          management-context-path: /actuator  # Use dashes!
          health-path: /actuator/health
          # For secured actuators
          user-name: ${actuator.username}
          user-password: ${actuator.password}

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## Security Configuration

### Insecure Profile

```java
@Profile("insecure")
@Configuration
public static class SecurityPermitAllConfig {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http.authorizeHttpRequests((authorizeRequests) ->
                authorizeRequests.anyRequest().permitAll())
            .csrf((csrf) -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    adminContextPath + "/instances",
                    adminContextPath + "/instances/*",
                    adminContextPath + "/actuator/**"
                ));
        return http.build();
    }
}
```

### Secure Profile (Default)

```java
@Profile("secure")
@Configuration
public static class SecuritySecureConfig {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        http.authorizeHttpRequests((authorizeRequests) ->
                authorizeRequests
                    .requestMatchers(adminContextPath + "/assets/**")
                        .permitAll()
                    .requestMatchers(adminContextPath + "/login")
                        .permitAll()
                    .anyRequest()
                        .authenticated())
            .formLogin((formLogin) -> formLogin
                .loginPage(adminContextPath + "/login")
                .successHandler(successHandler))
            .logout((logout) -> logout
                .logoutUrl(adminContextPath + "/logout"))
            .httpBasic(Customizer.withDefaults())
            .csrf((csrf) -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    adminContextPath + "/instances",
                    adminContextPath + "/instances/*",
                    adminContextPath + "/actuator/**"
                ));

        return http.build();
    }
}
```

## How It Works

### Service Discovery Flow

1. **Application Registration**:
    - Application registers with Consul on startup
    - Sends service metadata including actuator paths
    - Consul assigns service ID and health checks

2. **Health Checking**:
    - Consul performs HTTP health checks
    - Marks services as passing/failing
    - Admin Server queries only healthy services

3. **Admin Discovery**:
    - Admin Server queries Consul for registered services
    - Reads metadata to locate actuator endpoints
    - Begins monitoring discovered services

4. **Deregistration**:
    - Application deregisters on shutdown
    - Consul removes from registry
    - Admin Server stops monitoring

### Metadata Mapping

Admin Server reads specific metadata keys from Consul:

```yaml
spring:
  cloud:
    consul:
      discovery:
        metadata:
          # Required for endpoint detection
          management-context-path: /actuator  # Dashes only!
          management-port: 8081  # If different

          # Optional - for secured actuators
          user-name: admin
          user-password: ${ACTUATOR_PASSWORD}

          # Custom metadata
          environment: production
          version: 1.0.0
          team: platform
```

**Key Mappings**:

- `management-context-path` → Where to find actuator endpoints
- `management-port` → Management port if different from service port
- `health-path` → Custom health endpoint path
- `user-name` / `user-password` → Actuator credentials

## Custom Actuator Paths

This sample demonstrates custom actuator paths:

```yaml
management:
  endpoints:
    web:
      base-path: /foo          # Actuator at /foo instead of /actuator
      path-mapping:
        health: /ping          # Health at /foo/ping instead of /foo/health
```

Admin Server discovers these via metadata:

```yaml
spring:
  cloud:
    consul:
      discovery:
        metadata:
          management-context-path: /foo
          health-path: /ping
```

## Testing the Sample

### Verify Consul Registration

1. Access Consul UI: `http://localhost:8500/ui`
2. Navigate to "Services"
3. Should see:
    - `consul-example` (Admin Server)
    - Other registered services

### Check Service Health

In Consul UI, services should show:

- Status: Passing (green)
- Health check URL displayed
- Metadata visible

### Verify Admin Discovery

1. Access Admin UI: `http://localhost:8080`
2. Should see services registered in Consul
3. Click service to view:
    - Health status
    - Metrics
    - Environment
    - Custom /ping endpoint

### Test Dynamic Discovery

Register a new service:

```bash
# Register via Consul API
curl -X PUT -d '{
  "Name": "test-service",
  "Address": "127.0.0.1",
  "Port": 8081,
  "Meta": {
    "management-context-path": "/actuator"
  },
  "Check": {
    "HTTP": "http://127.0.0.1:8081/actuator/health",
    "Interval": "10s"
  }
}' http://localhost:8500/v1/agent/service/register
```

Service appears in Admin UI within seconds.

## Consul Features

### Health Checks

Consul supports multiple health check types:

#### HTTP Health Check

```yaml
spring:
  cloud:
    consul:
      discovery:
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
```

#### TTL Health Check

```yaml
spring:
  cloud:
    consul:
      discovery:
        health-check-ttl: 30s
```

Application must send heartbeat to Consul every 30 seconds.

### Service Tags

Add tags for filtering:

```yaml
spring:
  cloud:
    consul:
      discovery:
        tags:
          - production
          - backend
          - v1.0.0
```

### Service Filtering

Filter services monitored by Admin:

```yaml
spring:
  boot:
    admin:
      discovery:
        ignored-services:
          - consul        # Don't monitor Consul
          - config-server # Don't monitor Config Server
        services:         # Only monitor these (if specified)
          - my-service
          - another-service
```

## Advanced Configuration

### Consul ACL

Secure Consul with ACL tokens:

```yaml
spring:
  cloud:
    consul:
      token: ${CONSUL_TOKEN}
      discovery:
        acl-token: ${CONSUL_ACL_TOKEN}
```

### Consul TLS

Connect to Consul over TLS:

```yaml
spring:
  cloud:
    consul:
      scheme: https
      tls:
        enabled: true
        key-store-path: classpath:consul-keystore.p12
        key-store-password: ${KEYSTORE_PASSWORD}
```

### Multiple Datacenters

Register in specific datacenter:

```yaml
spring:
  cloud:
    consul:
      discovery:
        datacenter: dc1
```

### Prefer IP Address

Use IP instead of hostname:

```yaml
spring:
  cloud:
    consul:
      discovery:
        prefer-ip-address: true
        ip-address: 192.168.1.100
```

## Comparison: Consul vs. Eureka

| Feature             | Consul                            | Eureka                        |
|---------------------|-----------------------------------|-------------------------------|
| **Health Checks**   | Built-in (HTTP, TCP, TTL, Script) | Via Spring Boot actuator only |
| **Key-Value Store** | Yes                               | No                            |
| **ACL**             | Yes                               | Basic                         |
| **Multi-DC**        | Native support                    | Requires setup                |
| **DNS Interface**   | Yes                               | No                            |
| **Metadata Keys**   | No dots allowed                   | Dots allowed                  |
| **Complexity**      | Higher                            | Lower                         |
| **Ecosystem**       | HashiCorp ecosystem               | Netflix stack                 |

## Troubleshooting

### Metadata Key Errors

**Symptom**: Admin Server can't find actuator endpoints

**Cause**: Used dots in metadata keys

**Solution**: Use dashes instead:

```yaml
# Wrong
metadata:
  management.context-path: /actuator

# Correct
metadata:
  management-context-path: /actuator
```

### Services Not Discovered

**Check Consul connectivity**:

```bash
# Test Consul API
curl http://localhost:8500/v1/catalog/services

# Check health
curl http://localhost:8500/v1/health/state/passing
```

**Verify Admin logs**:

```bash
tail -f logs/spring-boot-admin.log | grep -i consul
```

### Health Check Failures

Services show as "failing" in Consul:

1. Verify health endpoint is accessible:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Check health check interval:
   ```yaml
   spring:
     cloud:
       consul:
         discovery:
           health-check-interval: 30s  # Increase if needed
   ```

3. Review Consul logs:
   ```bash
   consul monitor
   ```

### Connection Timeouts

Increase timeout values:

```yaml
spring:
  cloud:
    consul:
      discovery:
        health-check-timeout: 10s  # Increase from default
```

## Production Considerations

### Consul Cluster

Run Consul in cluster mode (3 or 5 nodes):

```bash
# Server node 1
consul agent -server -bootstrap-expect=3 -data-dir=/consul/data \
  -bind=192.168.1.10

# Server node 2
consul agent -server -data-dir=/consul/data \
  -bind=192.168.1.11 -join=192.168.1.10

# Server node 3
consul agent -server -data-dir=/consul/data \
  -bind=192.168.1.12 -join=192.168.1.10
```

### Enable ACL

```yaml
spring:
  cloud:
    consul:
      token: ${CONSUL_MANAGEMENT_TOKEN}
      discovery:
        acl-token: ${CONSUL_SERVICE_TOKEN}
```

### Monitor Consul Health

Register Admin Server to monitor itself:

```yaml
spring:
  boot:
    admin:
      discovery:
        ignored-services: []  # Don't ignore any services
```

## Key Takeaways

This sample demonstrates:

✅ **Consul Integration**

- Service discovery via Consul
- Health check integration

✅ **Metadata Handling**

- Proper metadata key formatting (dashes not dots)
- Custom actuator paths

✅ **Production Features**

- ACL support
- TLS encryption
- Multi-datacenter awareness

✅ **Flexibility**

- Custom endpoint paths
- Secure and insecure modes

## Next Steps

- Explore [Eureka Sample](./30-sample-eureka.md) for Netflix Eureka
- Review [Zookeeper Sample](./50-sample-zookeeper.md) for Apache Zookeeper
- Check [Consul Integration Guide](../04-integration/20-consul.md) for detailed setup
- See [Hazelcast Sample](./60-sample-hazelcast.md) for clustering

## See Also

- [Consul Integration Guide](../04-integration/20-consul.md)
- [Service Discovery](../03-client/40-service-discovery.md)
- [Server Configuration](../02-server/01-server.mdx)
- [HashiCorp Consul Documentation](https://www.consul.io/docs)
