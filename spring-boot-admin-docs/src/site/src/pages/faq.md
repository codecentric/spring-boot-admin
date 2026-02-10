--- 
toc_min_heading_level: 2
toc_max_heading_level: 2
---
# FAQ

This FAQ covers common questions and troubleshooting scenarios encountered when using Spring Boot Admin.

---

## General Questions

### Can I include spring-boot-admin into my business application?

**tl;dr** You can, but you shouldn't.

You can set `spring.boot.admin.context-path` to alter the path where the UI and REST-API is served, but depending on the complexity of your application you might get in trouble. On the other hand in my opinion it makes no sense for an application to monitor itself. In case your application goes down your monitoring tool also does.

### Can I change or reload Spring Boot properties at runtime?

Yes, you can refresh the entire environment or set/update individual properties for both single instances as well as for the entire application.

Note, however, that the Spring Boot application needs to have [Spring Cloud Commons](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#endpoints) and `management.endpoint.env.post.enabled=true` in place.

Also check the details of `@RefreshScope` https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope.

### Which Spring Boot Admin version should I use?

Spring Boot Admin's version matches the major and minor versions of Spring Boot:

- **Spring Boot Admin 2.x** → **Spring Boot 2.x**
- **Spring Boot Admin 3.x** → **Spring Boot 3.x**
- **Spring Boot Admin 4.x** → **Spring Boot 4.x**

Always match the major and minor version numbers. For example, if you're using Spring Boot 3.2.x, use Spring Boot Admin 3.2.x.

---

## Client Registration Issues

### My client application is not registering with the Admin Server

> **Related Issues:** [#918](https://github.com/codecentric/spring-boot-admin/issues/918), [#2039](https://github.com/codecentric/spring-boot-admin/issues/2039), [#797](https://github.com/codecentric/spring-boot-admin/issues/797)
> **Stack Overflow:** [spring-boot-admin](https://stackoverflow.com/questions/tagged/spring-boot-admin+registration)

**Common causes:**

1. **Incorrect Admin Server URL**

Verify your client's `application.properties`:

```properties
spring.boot.admin.client.url=http://localhost:8080
```

Make sure the URL points to the running Admin Server.

2. **Missing dependency**

Ensure you have the client starter in your `pom.xml`:

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>${spring-boot-admin.version}</version>
</dependency>
```

3. **Network connectivity**

Test if the client can reach the admin server:

```bash
curl http://localhost:8080/actuator/health
```

### I get "401 Unauthorized" errors during registration

> **Related Issues:** [#803](https://github.com/codecentric/spring-boot-admin/issues/803), [#1190](https://github.com/codecentric/spring-boot-admin/issues/1190), [#470](https://github.com/codecentric/spring-boot-admin/issues/470)
> **Stack Overflow:** [spring-boot-admin+security](https://stackoverflow.com/questions/tagged/spring-boot-admin+spring-security)

This occurs when the Admin Server has security enabled but the client doesn't provide credentials.

**Solution:** Add credentials to your client configuration:

```properties
spring.boot.admin.client.username=admin
spring.boot.admin.client.password=secret
```

### Registration works but client shows as "OFFLINE" immediately

> **Related Issues:** [#319](https://github.com/codecentric/spring-boot-admin/issues/319), [#136](https://github.com/codecentric/spring-boot-admin/issues/136)
> **Stack Overflow:** [spring-boot-actuator](https://stackoverflow.com/questions/tagged/spring-boot-actuator+spring-boot-admin)

This typically happens when:

1. **Health endpoint is not accessible**

Ensure the health endpoint is exposed:

```properties
management.endpoints.web.exposure.include=health,info
```

2. **Client has security but Admin Server can't access it**

Provide credentials via metadata:

```properties
spring.boot.admin.client.instance.metadata.user.name=actuator-user
spring.boot.admin.client.instance.metadata.user.password=actuator-password
```

### Client registration works in local development but fails in Docker/Kubernetes

> **Related Issues:** [#1537](https://github.com/codecentric/spring-boot-admin/issues/1537), [#1665](https://github.com/codecentric/spring-boot-admin/issues/1665)
> **Stack Overflow:** [spring-boot+docker](https://stackoverflow.com/questions/tagged/spring-boot+docker), [spring-boot+kubernetes](https://stackoverflow.com/questions/tagged/spring-boot+kubernetes)

This is often due to hostname resolution issues.

**Solution:** Use IP addresses instead of hostnames:

```properties
spring.boot.admin.client.instance.service-host-type=IP
```

Or specify the service URL explicitly:

```properties
spring.boot.admin.client.instance.service-base-url=http://my-service:8080
```

---

## Actuator Endpoints

### Only "Health" and "Info" endpoints are visible in the UI

> **Related Issues:** [#1102](https://github.com/codecentric/spring-boot-admin/issues/1102)
> **Stack Overflow:** [spring-boot-actuator+endpoints](https://stackoverflow.com/questions/tagged/spring-boot-actuator+endpoints)

Starting with Spring Boot 2.x, most actuator endpoints are not exposed by default.

**Solution:** Expose all endpoints in your client's `application.properties`:

```properties
management.endpoints.web.exposure.include=*
```

For production, be more selective:

```properties
management.endpoints.web.exposure.include=health,info,metrics,env,loggers
```

### How do I verify endpoints are accessible?

Visit the actuator discovery endpoint directly on your client application:

```
http://localhost:8080/actuator
```

You should see a JSON response with links to all available endpoints.

### Endpoints work locally but not through Spring Boot Admin

Check if security is blocking the Admin Server from accessing client endpoints:

1. **Verify the Admin Server can access endpoints directly:**

```bash
curl -u user:password http://client-host:8080/actuator/metrics
```

2. **Configure instance authentication:**

```properties
# Client application
spring.boot.admin.client.instance.metadata.user.name=actuator
spring.boot.admin.client.instance.metadata.user.password=secret
```

---

## Service Discovery (Eureka, Consul, Kubernetes)

### Applications registered in Eureka don't appear in Spring Boot Admin

> **Related Issues:** [#1327](https://github.com/codecentric/spring-boot-admin/issues/1327), [#152](https://github.com/codecentric/spring-boot-admin/issues/152)
> **Stack Overflow:** [spring-cloud-eureka](https://stackoverflow.com/questions/tagged/spring-cloud-eureka+spring-boot-admin)

**Solution:** Enable registry fetching in your Admin Server:

```properties
eureka.client.fetch-registry=true
eureka.client.registry-fetch-interval-seconds=5
```

Also ensure your Admin Server has `@EnableDiscoveryClient`:

```java
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {
    static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
```

### Service discovery takes too long (1.5+ minutes)

> **Related Issues:** [#1327](https://github.com/codecentric/spring-boot-admin/issues/1327)

This is due to default registry fetch intervals.

**Solution:** Speed up discovery:

```properties
eureka.client.registry-fetch-interval-seconds=5
eureka.instance.lease-renewal-interval-in-seconds=10
```

### Services disappear from Admin Server when they go DOWN

> **Related Issues:** [#1472](https://github.com/codecentric/spring-boot-admin/issues/1472)
> **Stack Overflow:** [spring-cloud-discovery](https://stackoverflow.com/questions/tagged/spring-cloud+service-discovery)

This is a known issue with Eureka's `DiscoveryClient` implementation - it filters out non-UP services.

**Workaround:** Use client registration instead of service discovery for critical monitoring, or implement a custom `ServiceInstanceConverter`.

### Multiple instances of the same application only show one in Admin Server

> **Related Issues:** [#856](https://github.com/codecentric/spring-boot-admin/issues/856), [#552](https://github.com/codecentric/spring-boot-admin/issues/552)
> **Stack Overflow:** [spring-cloud+multiple-instances](https://stackoverflow.com/questions/tagged/spring-cloud)

This can happen with certain cloud platforms (PCF, Kubernetes) when instances share the same hostname.

**Solution:** Ensure each instance has a unique instance ID:

```properties
spring.boot.admin.client.instance.metadata.instanceId=${spring.application.name}:${random.value}
```

---

## Security & Authentication

### How do I secure the Admin Server UI?

Add Spring Security dependency and configure authentication:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/assets/**", "/login").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.loginPage("/login"))
            .logout(logout -> logout.logoutUrl("/logout"))
            .httpBasic(withDefaults());
        return http.build();
    }
}
```

### CORS errors when accessing client applications

> **Related Issues:** [#1362](https://github.com/codecentric/spring-boot-admin/issues/1362), [#1691](https://github.com/codecentric/spring-boot-admin/issues/1691)
> **Stack Overflow:** [spring-boot+cors](https://stackoverflow.com/questions/tagged/spring-boot+cors)

When client applications run on different domains, browsers make preflight requests that can fail.

**Solution:** Configure CORS on the Admin Server:

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
        }
    };
}
```

### CSRF protection is blocking client registration

By default, Spring Security's CSRF protection can block registration requests.

**Solution:** Exempt the registration endpoint:

```java
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("/instances", "/actuator/**")
);
```

---

## Notifications

### Mail notifications are not working

> **Related Issues:** [#507](https://github.com/codecentric/spring-boot-admin/issues/507)
> **Stack Overflow:** [spring-boot+email](https://stackoverflow.com/questions/tagged/spring-boot+email)

**Checklist:**

1. **Add mail dependency:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

2. **Configure mail properties:**

```properties
spring.boot.admin.notify.mail.enabled=true
spring.boot.admin.notify.mail.from=admin@example.com
spring.boot.admin.notify.mail.to=alerts@example.com

spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=user
spring.mail.password=secret
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

3. **Test mail configuration separately** to ensure SMTP settings are correct.

### Slack notifications are not sending

> **Related Issues:** [#202](https://github.com/codecentric/spring-boot-admin/issues/202), [#356](https://github.com/codecentric/spring-boot-admin/issues/356)
> **Stack Overflow:** [spring-boot+slack](https://stackoverflow.com/questions/tagged/spring-boot+slack)

**Solution:** Configure Slack webhook:

```properties
spring.boot.admin.notify.slack.enabled=true
spring.boot.admin.notify.slack.webhook-url=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
spring.boot.admin.notify.slack.channel=monitoring
```

Note: The channel name should not include the `#` prefix.

### I'm receiving too many notifications

> **Related Issues:** [#402](https://github.com/codecentric/spring-boot-admin/issues/402)

**Solution:** Filter notifications by status changes:

```properties
# Ignore specific status transitions
spring.boot.admin.notify.mail.ignore-changes=UNKNOWN:UP,UNKNOWN:OFFLINE
```

Or create a custom filtered notifier:

```java
@Bean
@Primary
public FilteringNotifier filteringNotifier(Notifier delegate, InstanceRepository repository) {
    return new FilteringNotifier(delegate, repository);
}
```

---

## Kubernetes & Cloud Deployments

### Health checks fail with 401 errors in Kubernetes

> **Related Issues:** [#1325](https://github.com/codecentric/spring-boot-admin/issues/1325)
> **Stack Overflow:** [kubernetes+spring-boot](https://stackoverflow.com/questions/tagged/kubernetes+spring-boot+health-check)

When health endpoints are secured in Kubernetes, the Admin Server cannot access them.

**Solution:** Either:

1. **Make health endpoint public** (for Kubernetes probes):

```properties
management.endpoint.health.show-details=when-authorized
management.endpoints.web.exposure.include=health,info
```

2. **Configure separate ports** for management endpoints:

```properties
management.server.port=8081
```

Then configure Kubernetes probes to use the management port.

### Spring Boot Admin creates wrong health URL in Kubernetes

> **Related Issues:** [#1522](https://github.com/codecentric/spring-boot-admin/issues/1522), [#437](https://github.com/codecentric/spring-boot-admin/issues/437)
> **Stack Overflow:** [kubernetes+spring-boot-admin](https://stackoverflow.com/questions/tagged/kubernetes+spring-boot)

This happens with multi-port services (e.g., HTTP + gRPC).

**Solution:** Explicitly configure the management base URL:

```properties
spring.boot.admin.client.instance.management-base-url=http://my-service:8081/actuator
```

### Liveness probe failures causing cascading restarts

**Important:** Never configure liveness probes to depend on external system health checks.

```yaml
# Bad - includes external dependencies
livenessProbe:
  httpGet:
    path: /actuator/health

# Good - only internal application health
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
```

Configure Spring Boot to separate liveness and readiness:

```properties
management.health.probes.enabled=true
management.endpoint.health.group.liveness.include=ping
management.endpoint.health.group.readiness.include=db,redis
```

---

## UI Customization

### How do I add custom views to the Admin UI?

> **Related Issues:** [#683](https://github.com/codecentric/spring-boot-admin/issues/683), [#867](https://github.com/codecentric/spring-boot-admin/issues/867)
> **Stack Overflow:** [spring-boot-admin+customization](https://stackoverflow.com/questions/tagged/spring-boot-admin)

Custom views must be implemented as Vue.js components and placed at:

```
/META-INF/spring-boot-admin-server-ui/extensions/{name}/
```

**Example registration:**

```javascript
SBA.use({
  install({viewRegistry}) {
    viewRegistry.addView({
      name: 'custom-view',
      path: '/custom',
      component: CustomComponent,
      label: 'Custom View',
      order: 1000,
    });
  }
});
```

For development, configure the extension location:

```properties
spring.boot.admin.ui.extension-resource-locations=file:///path/to/custom-ui/target/dist/
```

### Can I conditionally show custom views based on instance metadata?

> **Related Issues:** [#1385](https://github.com/codecentric/spring-boot-admin/issues/1385)

Yes, use the `isEnabled` function in view registration:

```javascript
viewRegistry.addView({
  name: 'custom-view',
  path: '/custom',
  component: CustomComponent,
  isEnabled: ({instance}) => instance.hasTag('custom-enabled')
});
```

---

## Performance & Troubleshooting

### Admin Server is slow or uses too much memory

**Common causes:**

1. **Too many instances being monitored**
2. **Aggressive monitoring intervals**
3. **Event store growing too large**

**Solutions:**

1. **Adjust monitoring intervals:**

```properties
spring.boot.admin.monitor.status-interval=30s
spring.boot.admin.monitor.info-interval=1m
```

2. **Use Hazelcast for clustered deployments:**

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-server-cloud</artifactId>
</dependency>
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
</dependency>
```

3. **Increase JVM memory:**

```bash
java -Xmx1g -Xms512m -jar admin-server.jar
```

### How do I enable DEBUG logging for troubleshooting?

Add to `application.properties`:

```properties
# General Admin Server logging
logging.level.de.codecentric.boot.admin=DEBUG

# Client registration logging
logging.level.de.codecentric.boot.admin.server.services.InstanceRegistry=DEBUG

# HTTP client logging
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

### Where can I get help?

1. **Check the changelog:** [GitHub Releases](https://github.com/codecentric/spring-boot-admin/releases)
2. **Search existing issues:** [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues)
3. **Ask the community:**
   - [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot-admin) - Questions tagged `spring-boot-admin`
   - [Stack Overflow Search](https://stackoverflow.com/search?q=spring-boot-admin) - Search all Spring Boot Admin discussions
4. **Report bugs:** [Create an issue](https://github.com/codecentric/spring-boot-admin/issues/new)

:::note Community Resources
**For questions and troubleshooting:** Use [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot-admin) with the `spring-boot-admin` tag. The FAQ entries above reference related Stack Overflow tags for each topic.

**For bug reports and feature requests:** Use [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues). The FAQ entries reference specific GitHub issues where bugs were reported and resolved.

For broader Spring ecosystem questions, also check:
- [Spring Boot on Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot)
- [Spring Security on Stack Overflow](https://stackoverflow.com/questions/tagged/spring-security) (for security-related questions)
- [Spring Cloud on Stack Overflow](https://stackoverflow.com/questions/tagged/spring-cloud) (for Eureka/Discovery questions)
:::
