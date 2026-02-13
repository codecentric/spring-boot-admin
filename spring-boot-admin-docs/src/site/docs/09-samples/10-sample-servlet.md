---

sidebar_position: 10
sidebar_custom_props:
  icon: 'file-code'
---

# Servlet Sample

The Servlet sample demonstrates a complete Spring Boot Admin Server deployment using traditional servlet-based Spring
MVC. This is the most feature-rich sample, showcasing security, custom UI extensions, notifications, and
self-monitoring.

## Overview

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-servlet/`

**Features**:

- Traditional servlet-based deployment (Spring MVC)
- Spring Security integration with form login
- Self-monitoring using Admin Client
- Mail notifications configured
- Custom UI extensions included
- Custom notifier implementation
- HTTP exchange tracking
- Audit event logging
- Session persistence with JDBC
- Custom actuator endpoint
- JMX support via Jolokia

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Optional: Mail server for notifications

## Running the Sample

### Quick Start

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run
```

Access the application at: `http://localhost:8080`

### With Security Enabled

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=secure
```

**Login Credentials**:

- Username: `user`
- Password: `password`

### Change Port

```bash
SERVER_PORT=9090 mvn spring-boot:run
```

## Project Structure

### Dependencies

Key dependencies from `pom.xml`:

```xml
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

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Web (Servlet) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Notifications -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Session Management -->
    <dependency>
        <groupId>org.springframework.session</groupId>
        <artifactId>spring-session-jdbc</artifactId>
    </dependency>

    <!-- Custom UI Extensions -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-sample-custom-ui</artifactId>
    </dependency>
</dependencies>
```

### Main Application Class

```java title="SpringBootAdminServletApplication.java"
@SpringBootApplication
@EnableAdminServer
@EnableCaching
public class SpringBootAdminServletApplication {

    static void main(String[] args) {
        SpringApplication app = new SpringApplication(
            SpringBootAdminServletApplication.class
        );
        app.setApplicationStartup(new BufferingApplicationStartup(1500));
        app.run(args);
    }

    @Bean
    public CustomNotifier customNotifier(InstanceRepository repository) {
        return new CustomNotifier(repository);
    }

    @Bean
    public HttpHeadersProvider customHttpHeadersProvider() {
        return (instance) -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CUSTOM", "My Custom Value");
            return httpHeaders;
        };
    }

    @Bean
    public InstanceExchangeFilterFunction auditLog() {
        return (instance, request, next) -> next.exchange(request)
            .doOnSubscribe((s) -> {
                if (HttpMethod.DELETE.equals(request.method())
                    || HttpMethod.POST.equals(request.method())) {
                    log.info("{} for {} on {}",
                        request.method(), instance.getId(), request.url());
                }
            });
    }
}
```

**Key Points**:

- `@EnableAdminServer` activates Admin Server functionality
- Custom HTTP headers added to all instance requests
- Audit logging for DELETE/POST operations
- Application startup tracking enabled

## Configuration

### Application Configuration

```yaml title="application.yml"
spring:
  application:
    name: spring-boot-admin-sample-servlet

  boot:
    admin:
      client:
        url: http://localhost:8080  # Self-registration
        instance:
          service-host-type: IP
          metadata:
            tags:
              environment: test

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
    shutdown:
      enabled: true
    restart:
      enabled: true

logging:
  file:
    name: "target/boot-admin-sample-servlet.log"
  level:
    de.codecentric: info
```

### Static Instance Configuration

The sample includes multiple static instances with creative names:

```yaml
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            "Captain Debugbeard":
              - uri: http://localhost:8080
                metadata:
                  management.context-path: /actuator
                  group: Pirates of the Caribbean Bean

            "Stack Overflow Sorcerer":
              - uri: http://localhost:8080
                metadata:
                  management.context-path: /actuator
                  group: Wizarding World
```

## Security Configuration

### Spring Security Setup

```java title="SecuritySecureConfig.java"
@Profile("secure")
@Configuration
public class SecuritySecureConfig {

    private final AdminServerProperties adminServer;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.path("/"));

        http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests
            .requestMatchers(adminServer.path("/assets/**"))
                .permitAll()  // Allow static resources
            .requestMatchers(adminServer.path("/actuator/info"))
                .permitAll()
            .requestMatchers(adminServer.path("/actuator/health"))
                .permitAll()
            .requestMatchers(adminServer.path("/login"))
                .permitAll()
            .anyRequest()
                .authenticated())
            .formLogin((formLogin) -> formLogin
                .loginPage(adminServer.path("/login"))
                .successHandler(successHandler))
            .logout((logout) -> logout
                .logoutUrl(adminServer.path("/logout")))
            .httpBasic(Customizer.withDefaults());

        // CSRF Configuration
        http.csrf((csrf) -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers(
                adminServer.path("/instances"),      // Instance registration
                adminServer.path("/instances/*"),    // Instance deregistration
                adminServer.path("/actuator/**")     // Actuator endpoints
            ));

        http.rememberMe((rememberMe) -> rememberMe
            .key(UUID.randomUUID().toString())
            .tokenValiditySeconds(1209600));  // 14 days

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
            PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Security Features**:

1. Form-based login with custom page
2. HTTP Basic authentication support
3. CSRF protection with token repository
4. Remember-me functionality (14 days)
5. Static resources publicly accessible
6. Health/info endpoints publicly accessible

## Custom Notifier

The sample includes a custom notifier implementation:

```java title="CustomNotifier.java"
public class CustomNotifier extends AbstractEventNotifier {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CustomNotifier.class);

    public CustomNotifier(InstanceRepository repository) {
        super(repository);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent statusChangedEvent) {
                LOGGER.info("Instance {} ({}) is {}",
                    instance.getRegistration().getName(),
                    event.getInstance(),
                    statusChangedEvent.getStatusInfo().getStatus());
            } else {
                LOGGER.info("Instance {} ({}) {}",
                    instance.getRegistration().getName(),
                    event.getInstance(),
                    event.getType());
            }
        });
    }
}
```

### Notifier Configuration

```java title="NotifierConfig.java"
@Configuration
public class NotifierConfig {

    @Bean
    public FilteringNotifier filteringNotifier() {
        CompositeNotifier delegate = new CompositeNotifier(
            otherNotifiers.getIfAvailable(Collections::emptyList)
        );
        return new FilteringNotifier(delegate, repository);
    }

    @Primary
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RemindingNotifier remindingNotifier() {
        RemindingNotifier notifier = new RemindingNotifier(
            filteringNotifier(), repository
        );
        notifier.setReminderPeriod(Duration.ofMinutes(10));
        notifier.setCheckReminderInverval(Duration.ofSeconds(10));
        return notifier;
    }
}
```

**Notification Features**:

- Custom event logging
- Filtering notifier for selective notifications
- Reminding notifier (sends reminders every 10 minutes)
- Composable notifier chain

## UI Customizations

### External Views

The sample demonstrates various external view configurations:

```yaml
spring:
  boot:
    admin:
      ui:
        external-views:
          # Simple link
          - label: "🚀"
            url: "https://codecentric.de"
            order: 2000

          # Dropdown with links
          - label: Resources
            children:
              - label: "📖 Docs"
                url: https://codecentric.github.io/spring-boot-admin/
              - label: "📦 Maven"
                url: https://search.maven.org/...
              - label: "🐙 GitHub"
                url: https://github.com/codecentric/spring-boot-admin

          # Iframe view
          - label: "🎅 Is it christmas"
            url: https://isitchristmas.com
            iframe: true
```

### View Settings

```yaml
spring:
  boot:
    admin:
      ui:
        view-settings:
          - name: "journal"
            enabled: false
```

## Session Management

The sample uses JDBC-based session persistence:

```java
@Bean
public EmbeddedDatabase dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("org/springframework/session/jdbc/schema-hsqldb.sql")
        .build();
}
```

**Benefits**:

- Session persistence across restarts
- Support for clustered deployments
- Built-in session cleanup

## Testing the Sample

### Access the UI

1. Start the application
2. Navigate to `http://localhost:8080`
3. Login (if secure profile is active)
4. View the registered instances

### Test Self-Monitoring

The application monitors itself via the Admin Client. You should see:

- Application name: `spring-boot-admin-sample-servlet`
- Status: UP
- All actuator endpoints available
- Custom metadata and tags

### Test Notifications

Monitor the logs for custom notification events:

```
INFO - Instance spring-boot-admin-sample-servlet (...) is UP
INFO - Instance spring-boot-admin-sample-servlet (...) ENDPOINTS_DETECTED
```

### Test Custom Headers

All requests to instances include the custom header `X-CUSTOM: My Custom Value`.

### Test External Views

Click the external view links in the navigation:

- Rocket emoji (🚀) - Opens codecentric.de
- Resources dropdown - Multiple documentation links
- "Is it christmas" - Iframe view

## Build and Deploy

### Build JAR

```bash
mvn clean package
```

Produces: `target/spring-boot-admin-sample-servlet.jar`

### Run JAR

```bash
java -jar target/spring-boot-admin-sample-servlet.jar
```

### With Profiles

```bash
java -jar target/spring-boot-admin-sample-servlet.jar \
  --spring.profiles.active=secure
```

### Deployment Considerations

When deploying, consider:

1. **External Database**: Replace HSQLDB with PostgreSQL/MySQL
2. **Mail Server**: Configure SMTP for real notifications
3. **Security**: Use external user store (LDAP/OAuth2)
4. **HTTPS**: Enable TLS/SSL
5. **Session Store**: Use Redis or external database

Example deployment configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/admin
    username: admin
    password: ${DB_PASSWORD}

  mail:
    host: smtp.company.com
    port: 587
    username: ${SMTP_USER}
    password: ${SMTP_PASSWORD}

server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
```

## Customization Ideas

### Add Custom Actuator Endpoint

Create a custom endpoint (example included):

```java
@Component
@Endpoint(id = "custom")
public class CustomEndpoint {

    @ReadOperation
    public Map<String, Object> customEndpoint() {
        return Map.of(
            "message", "Hello from custom endpoint",
            "timestamp", Instant.now()
        );
    }
}
```

### Add Database Notifier

Replace log-based notifier with database persistence:

```java
public class DatabaseNotifier extends AbstractEventNotifier {

    private final EventRepository eventRepository;

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            eventRepository.save(new EventEntity(event, instance));
        });
    }
}
```

### Add Custom Metadata

Enhance self-registration with custom metadata:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            version: ${project.version}
            region: us-east-1
            team: platform
            tags:
              environment: production
              cost-center: engineering
```

## Troubleshooting

### Port Already in Use

```bash
# Change port
SERVER_PORT=9090 mvn spring-boot:run
```

### Security Issues

If you cannot access the UI:

1. Check if `secure` profile is active
2. Verify credentials: `user` / `password`
3. Check CSRF token in browser console
4. Clear browser cookies

### Self-Registration Fails

1. Verify client URL matches server URL
2. Check actuator endpoints are exposed
3. Review logs for connection errors
4. Ensure security permits instance registration

### Mail Notifications Not Working

1. Configure valid SMTP server
2. Check firewall/network access
3. Verify credentials
4. Enable debug logging:

```yaml
logging:
  level:
    org.springframework.mail: DEBUG
```

## Key Takeaways

This sample demonstrates:

✅ **Complete Deployment Setup**

- Security, session management, notifications

✅ **Self-Monitoring Pattern**

- Admin Server monitoring itself via Admin Client

✅ **Extensibility**

- Custom notifiers, endpoints, headers, UI views

✅ **Best Practices**

- Profile-based configuration
- CSRF protection
- Proper security configuration

## Next Steps

- Explore [Reactive Sample](./20-sample-reactive.md) for WebFlux alternative
- Review [Security Documentation](../05-security/) for deployment hardening
- Check [Customization Guide](../06-customization/) for more extensions
- See [Notification Configuration](../02-server/notifications/) for notifier options

## See Also

- [Server Configuration](../02-server/01-server.mdx)
- [Client Registration](../03-client/20-registration.md)
- [UI Customization](../06-customization/ui/)
- [Spring Security Integration](../05-security/10-server-authentication.md)
