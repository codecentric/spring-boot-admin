# Application Registration

The Spring Boot Admin Client handles the registration of your application with the Admin Server through the `ApplicationRegistrator` and `ApplicationFactory` interfaces.

## ApplicationRegistrator[​](#applicationregistrator "Direct link to ApplicationRegistrator")

The `ApplicationRegistrator` is responsible for managing the registration lifecycle:

```
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

### Default Implementation[​](#default-implementation "Direct link to Default Implementation")

The `DefaultApplicationRegistrator` automatically handles:

* Initial registration on application startup
* Periodic re-registration (heartbeat)
* Automatic deregistration on shutdown
* Retry logic for failed registrations

### Configuration[​](#configuration "Direct link to Configuration")

application.yml

```
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080  # Admin Server URL
        period: 10000  # Registration interval in milliseconds
        auto-registration: true  # Enable auto-registration
        auto-deregistration: true  # Enable auto-deregistration on shutdown
```

### Registration Process[​](#registration-process "Direct link to Registration Process")

1. **Application Startup**: The `RegistrationApplicationListener` triggers registration when `WebServerInitializedEvent` is fired
2. **Create Application**: `ApplicationFactory` creates the registration payload
3. **HTTP POST**: Client sends POST request to `/instances` endpoint
4. **Receive ID**: Server responds with an instance ID
5. **Periodic Heartbeat**: Client re-registers at configured intervals
6. **Shutdown Hook**: Application deregisters on graceful shutdown

## ApplicationFactory[​](#applicationfactory "Direct link to ApplicationFactory")

The `ApplicationFactory` is responsible for creating the `Application` object that contains all registration information.

```
public interface ApplicationFactory {
    Application createApplication();
}
```

### Default Implementation: DefaultApplicationFactory[​](#default-implementation-defaultapplicationfactory "Direct link to Default Implementation: DefaultApplicationFactory")

The default factory gathers information from:

* `InstanceProperties` - Client configuration
* `ServerProperties` - Web server configuration
* `ManagementServerProperties` - Actuator configuration
* `PathMappedEndpoints` - Actuator endpoint mappings
* `MetadataContributor` - Custom metadata

```
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

### Application Properties[​](#application-properties "Direct link to Application Properties")

#### Name[​](#name "Direct link to Name")

```
spring:
  boot:
    admin:
      client:
        instance:
          name: ${spring.application.name}  # Application name
```

#### Service URL[​](#service-url "Direct link to Service URL")

The URL where your application can be accessed:

```
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

#### Management URL[​](#management-url "Direct link to Management URL")

URL for actuator endpoints:

```
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

#### Health URL[​](#health-url "Direct link to Health URL")

Specific health endpoint URL:

```
spring:
  boot:
    admin:
      client:
        instance:
          health-url: https://my-app.example.com/actuator/health
```

### Host Type[​](#host-type "Direct link to Host Type")

Control how the service host is determined:

```
spring:
  boot:
    admin:
      client:
        instance:
          service-host-type: IP  # or CANONICAL
```

* **`IP`**: Use the IP address
* **`CANONICAL`**: Use the canonical hostname

### Custom ApplicationFactory[​](#custom-applicationfactory "Direct link to Custom ApplicationFactory")

Create a custom factory for specialized registration logic:

```
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

## Specialized ApplicationFactories[​](#specialized-applicationfactories "Direct link to Specialized ApplicationFactories")

### Servlet ApplicationFactory[​](#servlet-applicationfactory "Direct link to Servlet ApplicationFactory")

For servlet-based applications:

```
public class ServletApplicationFactory extends DefaultApplicationFactory {
    // Detects servlet port and context path automatically
}
```

### Reactive ApplicationFactory[​](#reactive-applicationfactory "Direct link to Reactive ApplicationFactory")

For WebFlux applications:

```
public class ReactiveApplicationFactory extends DefaultApplicationFactory {
    // Detects Netty port and context automatically
}
```

### Cloud Foundry ApplicationFactory[​](#cloud-foundry-applicationfactory "Direct link to Cloud Foundry ApplicationFactory")

For Cloud Foundry deployments:

```
public class CloudFoundryApplicationFactory implements ApplicationFactory {
    // Uses CF-specific environment variables:
    // - vcap.application.application_id
    // - vcap.application.instance_id
    // - vcap.application.uris
}
```

Automatically activated when Cloud Foundry is detected.

## Application Class[​](#application-class "Direct link to Application Class")

The `Application` class represents the registration payload:

```
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

### Building an Application[​](#building-an-application "Direct link to Building an Application")

```
Application app = Application.create("my-application")
    .healthUrl("http://localhost:8080/actuator/health")
    .managementUrl("http://localhost:8080/actuator")
    .serviceUrl("http://localhost:8080")
    .metadata("version", "1.0.0")
    .metadata("environment", "production")
    .build();
```

## Registration Lifecycle Events[​](#registration-lifecycle-events "Direct link to Registration Lifecycle Events")

Spring Boot Admin Client fires application events during registration:

```
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

## Custom Registrator[​](#custom-registrator "Direct link to Custom Registrator")

Implement custom registration logic:

```
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

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Registration Fails[​](#registration-fails "Direct link to Registration Fails")

Check:

* Admin Server URL is correct and accessible
* Network connectivity between client and server
* Firewall rules allow outbound connections
* Admin Server is running and healthy
* Credentials are correct if security is enabled

### Instance Not Appearing[​](#instance-not-appearing "Direct link to Instance Not Appearing")

Verify:

* Registration returned successfully (check logs)
* Application name is configured
* Health endpoint is accessible from Admin Server
* Actuator endpoints are exposed

### Repeated Re-registrations[​](#repeated-re-registrations "Direct link to Repeated Re-registrations")

This is normal behavior - the client re-registers periodically as a heartbeat. Adjust the period if needed:

```
spring:
  boot:
    admin:
      client:
        period: 30000  # 30 seconds instead of default 10
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Use environment-specific URLs** for service URL in different environments
2. **Configure appropriate metadata** to help identify instances
3. **Set reasonable registration periods** - too frequent causes unnecessary load
4. **Enable auto-deregistration** for clean shutdown
5. **Use service discovery** for dynamic environments instead of direct client registration
6. **Monitor registration logs** to ensure successful registration
7. **Configure health check paths** correctly for proper monitoring

## See Also[​](#see-also "Direct link to See Also")

* [Metadata](/4.1.0-SNAPSHOT/docs/client/metadata.md) - Learn about custom metadata
* [Service Discovery](/4.1.0-SNAPSHOT/docs/client/service-discovery.md) - Alternative registration using Spring Cloud Discovery
* [Client Configuration](/4.1.0-SNAPSHOT/docs/client/configuration.md) - Complete configuration reference
* [Client Features](/4.1.0-SNAPSHOT/docs/client/client-features.md) - Additional client capabilities
