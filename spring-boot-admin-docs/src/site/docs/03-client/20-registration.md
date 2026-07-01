---

sidebar_position: 20
sidebar_custom_props:
  icon: 'link'
---

# Application Registration

The Spring Boot Admin Client handles the registration of your application with the Admin Server through the
`ApplicationRegistrator` and `ApplicationFactory` interfaces.

## ApplicationRegistrator

The `ApplicationRegistrator` is responsible for managing the registration lifecycle:

```java
public interface ApplicationRegistrator {

    /**
     * Registers the client application at spring-boot-admin-server.
     * @return true if successful registration on at least one admin server
     */
    boolean register();

    /**
     * Tries to deregister currently registered application
     */
    void deregister();

    /**
     * @return the id of this client as given by the admin server.
     * Returns null if not registered yet.
     */
    String getRegisteredId();
}
```

### Default Implementation

The `DefaultApplicationRegistrator` automatically handles:

- Initial registration on application startup
- Periodic re-registration (heartbeat)
- Automatic deregistration on shutdown
- Retry logic for failed registrations

### Configuration

```yaml title="application.yml"
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080  # Admin Server URL
        period: 10000  # Registration interval in milliseconds
        auto-registration: true  # Enable auto-registration
        auto-deregistration: true  # Enable auto-deregistration on shutdown
```

### Registration Process

1. **Application Startup**: The `RegistrationApplicationListener` triggers registration when `WebServerInitializedEvent`
   is fired
2. **Create Application**: `ApplicationFactory` creates the registration payload
3. **HTTP POST**: Client sends POST request to `/instances` endpoint
4. **Receive ID**: Server responds with an instance ID
5. **Periodic Heartbeat**: Client re-registers at configured intervals
6. **Shutdown Hook**: Application deregisters on graceful shutdown

## ApplicationFactory

The `ApplicationFactory` is responsible for creating the `Application` object that contains all registration
information.

```java
public interface ApplicationFactory {
    Application createApplication();
}
```

### Default Implementation: DefaultApplicationFactory

The default factory gathers information from:

- `InstanceProperties` - Client configuration
- `ServerProperties` - Web server configuration
- `ManagementServerProperties` - Actuator configuration
- `PathMappedEndpoints` - Actuator endpoint mappings
- `MetadataContributor` - Custom metadata

```java
@Override
public Application createApplication() {
    return Application.create(getName())
        .healthUrl(getHealthUrl())
        .managementUrl(getManagementUrl())
        .serviceUrl(getServiceUrl())
        .metadata(getMetadata())
        .build();
}
```

### Application Properties

#### Name

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          name: ${spring.application.name}  # Application name
```

#### Service URL

The URL where your application can be accessed:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          service-url: https://my-app.example.com
          # or let it auto-detect:
          service-base-url: https://my-app.example.com
          service-path: /
```

Auto-detection uses:

1. Configured `service-url` (highest priority)
2. `service-base-url` + `service-path`
3. Auto-detected from server properties

#### Management URL

URL for actuator endpoints:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          management-url: https://my-app.example.com/actuator
          # or
          management-base-url: https://my-app.example.com
management:
  endpoints:
    web:
      base-path: /actuator
```

#### Health URL

Specific health endpoint URL:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          health-url: https://my-app.example.com/actuator/health
```

### Host Type

Control how the service host is determined:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          service-host-type: IP  # or CANONICAL
```

- **`IP`**: Use the IP address
- **`CANONICAL`**: Use the canonical hostname

### Custom ApplicationFactory

Create a custom factory for specialized registration logic:

```java
@Component
public class CustomApplicationFactory implements ApplicationFactory {

    private final InstanceProperties instance;
    private final Environment environment;

    public CustomApplicationFactory(InstanceProperties instance,
                                   Environment environment) {
        this.instance = instance;
        this.environment = environment;
    }

    @Override
    public Application createApplication() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", environment.getProperty("app.environment"));
        metadata.put("version", environment.getProperty("app.version"));
        metadata.put("region", environment.getProperty("cloud.region"));

        return Application.create(instance.getName())
            .healthUrl(buildHealthUrl())
            .managementUrl(buildManagementUrl())
            .serviceUrl(buildServiceUrl())
            .metadata(metadata)
            .build();
    }

    private String buildHealthUrl() {
        // Custom logic to build health URL
        return "https://my-app.com/health";
    }

    private String buildManagementUrl() {
        // Custom logic to build management URL
        return "https://my-app.com/management";
    }

    private String buildServiceUrl() {
        // Custom logic to build service URL
        return "https://my-app.com";
    }
}
```

## Specialized ApplicationFactories

### Servlet ApplicationFactory

For servlet-based applications:

```java
public class ServletApplicationFactory extends DefaultApplicationFactory {
    // Detects servlet port and context path automatically
}
```

### Reactive ApplicationFactory

For WebFlux applications:

```java
public class ReactiveApplicationFactory extends DefaultApplicationFactory {
    // Detects Netty port and context automatically
}
```

### Cloud Foundry ApplicationFactory

For Cloud Foundry deployments:

```java
public class CloudFoundryApplicationFactory implements ApplicationFactory {
    // Uses CF-specific environment variables:
    // - vcap.application.application_id
    // - vcap.application.instance_id
    // - vcap.application.uris
}
```

Automatically activated when Cloud Foundry is detected.

## Application Class

The `Application` class represents the registration payload:

```java
public class Application {
    private final String name;
    private final String managementUrl;
    private final String healthUrl;
    private final String serviceUrl;
    private final Map<String, String> metadata;

    // Builder pattern
    public static Builder create(String name) {
        return new Builder(name);
    }
}
```

### Building an Application

```java
Application app = Application.create("my-application")
    .healthUrl("http://localhost:8080/actuator/health")
    .managementUrl("http://localhost:8080/actuator")
    .serviceUrl("http://localhost:8080")
    .metadata("version", "1.0.0")
    .metadata("environment", "production")
    .build();
```

## Registration Lifecycle Events

Spring Boot Admin Client fires application events during registration:

```java
@Component
public class RegistrationEventListener {

    @EventListener
    public void onRegistration(InstanceRegisteredEvent event) {
        String instanceId = event.getRegistration().getInstanceId();
        log.info("Registered with instance ID: {}", instanceId);
    }

    @EventListener
    public void onDeregistration(InstanceDeregisteredEvent event) {
        log.info("Deregistered instance");
    }
}
```

## Custom Registrator

Implement custom registration logic:

```java
@Component
public class CustomApplicationRegistrator implements ApplicationRegistrator {

    private final ApplicationFactory applicationFactory;
    private final RestClient restClient;
    private final String adminUrl;
    private volatile String registeredId;

    @Override
    public boolean register() {
        Application application = applicationFactory.createApplication();

        try {
            Map<String, Object> response = restClient.post()
                .uri(adminUrl + "/instances")
                .body(application)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            this.registeredId = (String) response.get("id");
            log.info("Registered as: {}", registeredId);
            return true;
        } catch (Exception e) {
            log.error("Registration failed", e);
            return false;
        }
    }

    @Override
    public void deregister() {
        if (registeredId != null) {
            try {
                restClient.delete()
                    .uri(adminUrl + "/instances/" + registeredId)
                    .retrieve()
                    .toBodilessEntity();
                log.info("Deregistered successfully");
            } catch (Exception e) {
                log.error("Deregistration failed", e);
            }
        }
    }

    @Override
    public String getRegisteredId() {
        return registeredId;
    }
}
```

## Troubleshooting

### Registration Fails

Check:

- Admin Server URL is correct and accessible
- Network connectivity between client and server
- Firewall rules allow outbound connections
- Admin Server is running and healthy
- Credentials are correct if security is enabled

### Instance Not Appearing

Verify:

- Registration returned successfully (check logs)
- Application name is configured
- Health endpoint is accessible from Admin Server
- Actuator endpoints are exposed

### Repeated Re-registrations

This is normal behavior - the client re-registers periodically as a heartbeat. Adjust the period if needed:

```yaml
spring:
  boot:
    admin:
      client:
        period: 30000  # 30 seconds instead of default 10
```

## Best Practices

1. **Use environment-specific URLs** for service URL in different environments
2. **Configure appropriate metadata** to help identify instances
3. **Set reasonable registration periods** - too frequent causes unnecessary load
4. **Enable auto-deregistration** for clean shutdown
5. **Use service discovery** for dynamic environments instead of direct client registration
6. **Monitor registration logs** to ensure successful registration
7. **Configure health check paths** correctly for proper monitoring

## See Also

- [Metadata](./30-metadata.md) - Learn about custom metadata
- [Service Discovery](./40-service-discovery.md) - Alternative registration using Spring Cloud Discovery
- [Client Configuration](./80-configuration.md) - Complete configuration reference
- [Client Features](./10-client-features.md) - Additional client capabilities
