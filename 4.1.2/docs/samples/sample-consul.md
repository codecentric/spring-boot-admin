# Consul Sample

The Consul sample demonstrates Spring Boot Admin Server integration with HashiCorp Consul for service discovery. This sample shows how to leverage Consul's powerful service registry and health checking capabilities to automatically discover and monitor Spring Boot applications.

## Overview[​](#overview "Direct link to Overview")

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-consul/`

**Features**:

* Automatic service discovery via Consul
* No Admin Client required on monitored applications
* Consul health check integration
* Metadata-based configuration
* Custom actuator endpoint paths
* Spring Security integration
* Servlet-based deployment

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17 or higher
* Maven 3.6+
* Consul installed and running

## Installing Consul[​](#installing-consul "Direct link to Installing Consul")

### macOS[​](#macos "Direct link to macOS")

```
brew install consul
```

### Linux[​](#linux "Direct link to Linux")

```
wget https://releases.hashicorp.com/consul/1.17.0/consul_1.17.0_linux_amd64.zip

unzip consul_1.17.0_linux_amd64.zip

sudo mv consul /usr/local/bin/
```

### Windows[​](#windows "Direct link to Windows")

Download from: <https://www.consul.io/downloads>

### Docker[​](#docker "Direct link to Docker")

```
docker run -d -p 8500:8500 -p 8600:8600/udp --name=consul consul agent -server -ui -bootstrap-expect=1 -client=0.0.0.0
```

### Verify Installation[​](#verify-installation "Direct link to Verify Installation")

```
consul version
```

## Running the Sample[​](#running-the-sample "Direct link to Running the Sample")

### Start Consul[​](#start-consul "Direct link to Start Consul")

```
# Development mode (single node)

consul agent -dev
```

Verify Consul is running: `http://localhost:8500/ui`

### Start Admin Server[​](#start-admin-server "Direct link to Start Admin Server")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-consul

mvn spring-boot:run
```

Access Admin UI at: `http://localhost:8080`

### With Different Consul Host[​](#with-different-consul-host "Direct link to With Different Consul Host")

```
mvn spring-boot:run -Dspring-boot.run.arguments=\

  --spring.cloud.consul.host=consul-server
```

### Insecure Mode[​](#insecure-mode "Direct link to Insecure Mode")

```
mvn spring-boot:run -Dspring-boot.run.profiles=insecure
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

### Main Application Class[​](#main-application-class "Direct link to Main Application Class")

SpringBootAdminConsulApplication.java

```
@SpringBootApplication

@EnableDiscoveryClient  // Enable Consul discovery

@EnableAdminServer      // Enable Admin Server

public class SpringBootAdminConsulApplication {



    static void main(String[] args) {

        SpringApplication.run(SpringBootAdminConsulApplication.class, args);

    }

}
```

## Configuration[​](#configuration "Direct link to Configuration")

### Admin Server Configuration[​](#admin-server-configuration "Direct link to Admin Server Configuration")

application.yml

```
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

Metadata Key Restriction

**CRITICAL**: Consul metadata keys **cannot contain dots**. Use dashes instead:

* ✅ `management-context-path`
* ❌ `management.context-path`

This is a Consul limitation, not a Spring Boot Admin limitation.

### Client Application Configuration[​](#client-application-configuration "Direct link to Client Application Configuration")

For applications to be monitored:

```
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

## Security Configuration[​](#security-configuration "Direct link to Security Configuration")

### Insecure Profile[​](#insecure-profile "Direct link to Insecure Profile")

```
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

### Secure Profile (Default)[​](#secure-profile-default "Direct link to Secure Profile (Default)")

```
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

## How It Works[​](#how-it-works "Direct link to How It Works")

### Service Discovery Flow[​](#service-discovery-flow "Direct link to Service Discovery Flow")

1. **Application Registration**:

   * Application registers with Consul on startup
   * Sends service metadata including actuator paths
   * Consul assigns service ID and health checks

2. **Health Checking**:

   * Consul performs HTTP health checks
   * Marks services as passing/failing
   * Admin Server queries only healthy services

3. **Admin Discovery**:

   * Admin Server queries Consul for registered services
   * Reads metadata to locate actuator endpoints
   * Begins monitoring discovered services

4. **Deregistration**:

   * Application deregisters on shutdown
   * Consul removes from registry
   * Admin Server stops monitoring

### Metadata Mapping[​](#metadata-mapping "Direct link to Metadata Mapping")

Admin Server reads specific metadata keys from Consul:

```
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

* `management-context-path` → Where to find actuator endpoints
* `management-port` → Management port if different from service port
* `health-path` → Custom health endpoint path
* `user-name` / `user-password` → Actuator credentials

## Custom Actuator Paths[​](#custom-actuator-paths "Direct link to Custom Actuator Paths")

This sample demonstrates custom actuator paths:

```
management:

  endpoints:

    web:

      base-path: /foo          # Actuator at /foo instead of /actuator

      path-mapping:

        health: /ping          # Health at /foo/ping instead of /foo/health
```

Admin Server discovers these via metadata:

```
spring:

  cloud:

    consul:

      discovery:

        metadata:

          management-context-path: /foo

          health-path: /ping
```

## Testing the Sample[​](#testing-the-sample "Direct link to Testing the Sample")

### Verify Consul Registration[​](#verify-consul-registration "Direct link to Verify Consul Registration")

1. Access Consul UI: `http://localhost:8500/ui`

2. Navigate to "Services"

3. Should see:

   <!-- -->

   * `consul-example` (Admin Server)
   * Other registered services

### Check Service Health[​](#check-service-health "Direct link to Check Service Health")

In Consul UI, services should show:

* Status: Passing (green)
* Health check URL displayed
* Metadata visible

### Verify Admin Discovery[​](#verify-admin-discovery "Direct link to Verify Admin Discovery")

1. Access Admin UI: `http://localhost:8080`

2. Should see services registered in Consul

3. Click service to view:

   <!-- -->

   * Health status
   * Metrics
   * Environment
   * Custom /ping endpoint

### Test Dynamic Discovery[​](#test-dynamic-discovery "Direct link to Test Dynamic Discovery")

Register a new service:

```
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

## Consul Features[​](#consul-features "Direct link to Consul Features")

### Health Checks[​](#health-checks "Direct link to Health Checks")

Consul supports multiple health check types:

#### HTTP Health Check[​](#http-health-check "Direct link to HTTP Health Check")

```
spring:

  cloud:

    consul:

      discovery:

        health-check-path: /actuator/health

        health-check-interval: 10s

        health-check-timeout: 5s
```

#### TTL Health Check[​](#ttl-health-check "Direct link to TTL Health Check")

```
spring:

  cloud:

    consul:

      discovery:

        health-check-ttl: 30s
```

Application must send heartbeat to Consul every 30 seconds.

### Service Tags[​](#service-tags "Direct link to Service Tags")

Add tags for filtering:

```
spring:

  cloud:

    consul:

      discovery:

        tags:

          - production

          - backend

          - v1.0.0
```

### Service Filtering[​](#service-filtering "Direct link to Service Filtering")

Filter services monitored by Admin:

```
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

## Advanced Configuration[​](#advanced-configuration "Direct link to Advanced Configuration")

### Consul ACL[​](#consul-acl "Direct link to Consul ACL")

Secure Consul with ACL tokens:

```
spring:

  cloud:

    consul:

      token: ${CONSUL_TOKEN}

      discovery:

        acl-token: ${CONSUL_ACL_TOKEN}
```

### Consul TLS[​](#consul-tls "Direct link to Consul TLS")

Connect to Consul over TLS:

```
spring:

  cloud:

    consul:

      scheme: https

      tls:

        enabled: true

        key-store-path: classpath:consul-keystore.p12

        key-store-password: ${KEYSTORE_PASSWORD}
```

### Multiple Datacenters[​](#multiple-datacenters "Direct link to Multiple Datacenters")

Register in specific datacenter:

```
spring:

  cloud:

    consul:

      discovery:

        datacenter: dc1
```

### Prefer IP Address[​](#prefer-ip-address "Direct link to Prefer IP Address")

Use IP instead of hostname:

```
spring:

  cloud:

    consul:

      discovery:

        prefer-ip-address: true

        ip-address: 192.168.1.100
```

## Comparison: Consul vs. Eureka[​](#comparison-consul-vs-eureka "Direct link to Comparison: Consul vs. Eureka")

| Feature             | Consul                            | Eureka                        |
| ------------------- | --------------------------------- | ----------------------------- |
| **Health Checks**   | Built-in (HTTP, TCP, TTL, Script) | Via Spring Boot actuator only |
| **Key-Value Store** | Yes                               | No                            |
| **ACL**             | Yes                               | Basic                         |
| **Multi-DC**        | Native support                    | Requires setup                |
| **DNS Interface**   | Yes                               | No                            |
| **Metadata Keys**   | No dots allowed                   | Dots allowed                  |
| **Complexity**      | Higher                            | Lower                         |
| **Ecosystem**       | HashiCorp ecosystem               | Netflix stack                 |

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Metadata Key Errors[​](#metadata-key-errors "Direct link to Metadata Key Errors")

**Symptom**: Admin Server can't find actuator endpoints

**Cause**: Used dots in metadata keys

**Solution**: Use dashes instead:

```
# Wrong

metadata:

  management.context-path: /actuator



# Correct

metadata:

  management-context-path: /actuator
```

### Services Not Discovered[​](#services-not-discovered "Direct link to Services Not Discovered")

**Check Consul connectivity**:

```
# Test Consul API

curl http://localhost:8500/v1/catalog/services



# Check health

curl http://localhost:8500/v1/health/state/passing
```

**Verify Admin logs**:

```
tail -f logs/spring-boot-admin.log | grep -i consul
```

### Health Check Failures[​](#health-check-failures "Direct link to Health Check Failures")

Services show as "failing" in Consul:

1. Verify health endpoint is accessible:

   ```
   curl http://localhost:8080/actuator/health
   ```

2. Check health check interval:

   ```
   spring:

     cloud:

       consul:

         discovery:

           health-check-interval: 30s  # Increase if needed
   ```

3. Review Consul logs:

   ```
   consul monitor
   ```

### Connection Timeouts[​](#connection-timeouts "Direct link to Connection Timeouts")

Increase timeout values:

```
spring:

  cloud:

    consul:

      discovery:

        health-check-timeout: 10s  # Increase from default
```

## Production Considerations[​](#production-considerations "Direct link to Production Considerations")

### Consul Cluster[​](#consul-cluster "Direct link to Consul Cluster")

Run Consul in cluster mode (3 or 5 nodes):

```
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

### Enable ACL[​](#enable-acl "Direct link to Enable ACL")

```
spring:

  cloud:

    consul:

      token: ${CONSUL_MANAGEMENT_TOKEN}

      discovery:

        acl-token: ${CONSUL_SERVICE_TOKEN}
```

### Monitor Consul Health[​](#monitor-consul-health "Direct link to Monitor Consul Health")

Register Admin Server to monitor itself:

```
spring:

  boot:

    admin:

      discovery:

        ignored-services: []  # Don't ignore any services
```

## Key Takeaways[​](#key-takeaways "Direct link to Key Takeaways")

This sample demonstrates:

✅ **Consul Integration**

* Service discovery via Consul
* Health check integration

✅ **Metadata Handling**

* Proper metadata key formatting (dashes not dots)
* Custom actuator paths

✅ **Production Features**

* ACL support
* TLS encryption
* Multi-datacenter awareness

✅ **Flexibility**

* Custom endpoint paths
* Secure and insecure modes

## Next Steps[​](#next-steps "Direct link to Next Steps")

* Explore [Eureka Sample](/4.1.2/docs/samples/sample-eureka.md) for Netflix Eureka
* Review [Zookeeper Sample](/4.1.2/docs/samples/sample-zookeeper.md) for Apache Zookeeper
* Check [Consul Integration Guide](/4.1.2/docs/integration/consul.md) for detailed setup
* See [Hazelcast Sample](/4.1.2/docs/samples/sample-hazelcast.md) for clustering

## See Also[​](#see-also "Direct link to See Also")

* [Consul Integration Guide](/4.1.2/docs/integration/consul.md)
* [Service Discovery](/4.1.2/docs/client/service-discovery.md)
* [Server Configuration](/4.1.2/docs/server/server.md)
* [HashiCorp Consul Documentation](https://www.consul.io/docs)
