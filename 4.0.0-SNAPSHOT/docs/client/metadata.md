# Metadata and Tags

Metadata allows you to attach custom information to your application registration, which can be used for filtering, grouping, and providing additional context in the Spring Boot Admin UI.

## MetadataContributor[​](#metadatacontributor "Direct link to MetadataContributor")

The `MetadataContributor` interface enables you to programmatically add metadata to your application registration:

```
@FunctionalInterface
public interface MetadataContributor {
    Map<String, String> getMetadata();
}
```

## Built-in Metadata Contributors[​](#built-in-metadata-contributors "Direct link to Built-in Metadata Contributors")

### StartupDateMetadataContributor[​](#startupdatemetadatacontributor "Direct link to StartupDateMetadataContributor")

Automatically adds the application startup timestamp:

```
public class StartupDateMetadataContributor implements MetadataContributor {

    private final OffsetDateTime timestamp = OffsetDateTime.now();

    @Override
    public Map<String, String> getMetadata() {
        return singletonMap("startup",
                          timestamp.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
```

This metadata is automatically included and helps the Admin Server detect application restarts.

### CloudFoundryMetadataContributor[​](#cloudfoundrymetadatacontributor "Direct link to CloudFoundryMetadataContributor")

For Cloud Foundry deployments, adds CF-specific metadata:

```
public class CloudFoundryMetadataContributor implements MetadataContributor {

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("applicationId", vcapApplication.getApplicationId());
        metadata.put("instanceId", vcapApplication.getInstanceId());
        // Additional CF metadata
        return metadata;
    }
}
```

Automatically activated when running on Cloud Foundry.

### CompositeMetadataContributor[​](#compositemetadatacontributor "Direct link to CompositeMetadataContributor")

Combines multiple metadata contributors:

```
public class CompositeMetadataContributor implements MetadataContributor {

    private final List<MetadataContributor> delegates;

    public CompositeMetadataContributor(List<MetadataContributor> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        delegates.forEach(delegate -> metadata.putAll(delegate.getMetadata()));
        return metadata;
    }
}
```

Spring Boot Admin automatically creates a composite contributor from all `MetadataContributor` beans.

## Adding Metadata via Configuration[​](#adding-metadata-via-configuration "Direct link to Adding Metadata via Configuration")

### Static Metadata[​](#static-metadata "Direct link to Static Metadata")

Add static metadata through properties:

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            team: platform-team
            environment: production
            region: us-east-1
            version: 1.0.0
            support-email: platform@example.com
```

### Dynamic Metadata from Environment[​](#dynamic-metadata-from-environment "Direct link to Dynamic Metadata from Environment")

Use property placeholders to inject environment-specific values:

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            environment: ${APP_ENV:development}
            version: ${APP_VERSION:unknown}
            hostname: ${HOSTNAME:localhost}
            pod-name: ${POD_NAME:}
            namespace: ${NAMESPACE:default}
```

## Custom MetadataContributor[​](#custom-metadatacontributor "Direct link to Custom MetadataContributor")

Create custom metadata contributors for dynamic or computed metadata:

```
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomMetadataContributor implements MetadataContributor {

    private final Environment environment;
    private final BuildProperties buildProperties;

    public CustomMetadataContributor(Environment environment,
                                    @Autowired(required = false) BuildProperties buildProperties) {
        this.environment = environment;
        this.buildProperties = buildProperties;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();

        // Add build information
        if (buildProperties != null) {
            metadata.put("build.version", buildProperties.getVersion());
            metadata.put("build.time", buildProperties.getTime().toString());
            metadata.put("build.artifact", buildProperties.getArtifact());
        }

        // Add environment information
        metadata.put("spring.profiles", String.join(",",
                                                    environment.getActiveProfiles()));

        // Add JVM information
        metadata.put("java.version", System.getProperty("java.version"));
        metadata.put("java.vendor", System.getProperty("java.vendor"));

        // Add custom business metadata
        metadata.put("feature-flags",
                    environment.getProperty("app.feature-flags", ""));

        return metadata;
    }
}
```

### Kubernetes Metadata[​](#kubernetes-metadata "Direct link to Kubernetes Metadata")

```
@Component
@ConditionalOnProperty(name = "kubernetes.enabled", havingValue = "true")
public class KubernetesMetadataContributor implements MetadataContributor {

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();

        // Read from environment variables set by Kubernetes
        metadata.put("k8s.pod", System.getenv("HOSTNAME"));
        metadata.put("k8s.namespace", System.getenv("POD_NAMESPACE"));
        metadata.put("k8s.node", System.getenv("NODE_NAME"));
        metadata.put("k8s.service-account",
                    System.getenv("SERVICE_ACCOUNT"));

        // Add labels as metadata
        String labels = System.getenv("POD_LABELS");
        if (labels != null) {
            metadata.put("k8s.labels", labels);
        }

        return metadata;
    }
}
```

## Tags[​](#tags "Direct link to Tags")

Tags are a special type of metadata used for visual markers in the Admin UI. They appear as colored badges in the application list and instance views.

### Configuring Tags[​](#configuring-tags "Direct link to Configuring Tags")

#### Via Metadata[​](#via-metadata "Direct link to Via Metadata")

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
              region: us-west-2
              tier: backend
```

#### Via Info Endpoint[​](#via-info-endpoint "Direct link to Via Info Endpoint")

application.yml

```
info:
  tags:
    environment: production
    region: us-west-2
    tier: backend
```

### Tag Display[​](#tag-display "Direct link to Tag Display")

Tags appear as colored badges:

* In the applications list view
* In the instance details header
* Can be used for filtering and grouping

### Dynamic Tags[​](#dynamic-tags "Direct link to Dynamic Tags")

Create tags dynamically based on runtime conditions:

```
@Component
public class DynamicTagMetadataContributor implements MetadataContributor {

    private final Environment environment;

    public DynamicTagMetadataContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();

        // Environment tag
        String env = environment.getProperty("spring.profiles.active", "default");
        metadata.put("tags.environment", env);

        // Deployment type
        if (isKubernetes()) {
            metadata.put("tags.platform", "kubernetes");
        } else if (isCloudFoundry()) {
            metadata.put("tags.platform", "cloud-foundry");
        } else {
            metadata.put("tags.platform", "standalone");
        }

        // Health-based tag
        metadata.put("tags.monitoring", "enabled");

        return metadata;
    }

    private boolean isKubernetes() {
        return System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }

    private boolean isCloudFoundry() {
        return System.getenv("VCAP_APPLICATION") != null;
    }
}
```

## Metadata Use Cases[​](#metadata-use-cases "Direct link to Metadata Use Cases")

### 1. Security Credentials[​](#1-security-credentials "Direct link to 1. Security Credentials")

Pass credentials for actuator endpoint access:

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            user.name: ${spring.security.user.name}
            user.password: ${spring.security.user.password}
```

warning

Credentials in metadata are masked in the Admin UI but transmitted over the network. Always use HTTPS when transmitting sensitive data.

### 2. Service Discovery Integration[​](#2-service-discovery-integration "Direct link to 2. Service Discovery Integration")

#### Eureka[​](#eureka "Direct link to Eureka")

application.yml

```
eureka:
  instance:
    metadata-map:
      startup: ${random.int}  # Trigger update on restart
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}
      tags.environment: ${APP_ENV}
```

#### Consul[​](#consul "Direct link to Consul")

application.yml

```
spring:
  cloud:
    consul:
      discovery:
        metadata:
          user-name: ${spring.security.user.name}  # Note: use dashes, not dots
          user-password: ${spring.security.user.password}
          environment: production
```

warning

Consul does not allow dots (`.`) in metadata keys. Use dashes (`-`) instead.

### 3. Grouping Applications[​](#3-grouping-applications "Direct link to 3. Grouping Applications")

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            group: Legacy Squad
            squad: backend-team
            cost-center: CC-1234
```

The Admin UI can use the `group` metadata for visual grouping.

### 4. Custom URLs and Paths[​](#4-custom-urls-and-paths "Direct link to 4. Custom URLs and Paths")

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            management.context-path: /actuator
            service-url: https://my-app.example.com
            service-path: /api
```

### 5. Visibility Control[​](#5-visibility-control "Direct link to 5. Visibility Control")

application.yml

```
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: true  # Hide service URL in UI
```

## Accessing Metadata in Admin Server[​](#accessing-metadata-in-admin-server "Direct link to Accessing Metadata in Admin Server")

Metadata is available through the instance object:

```
@Component
public class MetadataProcessor {

    public void processInstance(Instance instance) {
        Map<String, String> metadata = instance.getRegistration().getMetadata();

        String environment = metadata.get("tags.environment");
        String team = metadata.get("team");
        String version = metadata.get("version");

        // Process metadata
        log.info("Instance {} - Environment: {}, Team: {}, Version: {}",
                instance.getRegistration().getName(),
                environment, team, version);
    }
}
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Use Meaningful Keys**: Use descriptive, hierarchical keys (e.g., `tags.environment`, `k8s.namespace`)
2. **Avoid Sensitive Data**: Don't include secrets unless necessary; use secure transmission
3. **Keep It Lightweight**: Don't overload metadata with large values
4. **Use Tags for Visuals**: Leverage tags for important visual indicators
5. **Document Metadata**: Maintain documentation of your metadata schema
6. **Use Environment Variables**: Make metadata configurable per environment
7. **Consistent Naming**: Use consistent naming conventions across services
8. **Leverage Existing Info**: Use `/actuator/info` for build and git information

## Metadata Security[​](#metadata-security "Direct link to Metadata Security")

### Masked Metadata[​](#masked-metadata "Direct link to Masked Metadata")

The Admin Server masks certain metadata keys by default:

* `password`
* `secret`
* `key`
* `token`
* `credentials`

These values are hidden in the UI but still transmitted to the server.

### Secure Transmission[​](#secure-transmission "Direct link to Secure Transmission")

Always use HTTPS when transmitting sensitive metadata:

```
spring:
  boot:
    admin:
      client:
        url: https://admin-server.example.com  # Use HTTPS
```

## See Also[​](#see-also "Direct link to See Also")

* [Client Features](/4.0.0-SNAPSHOT/docs/client/client-features.md) - Other client capabilities
* [Registration](/4.0.0-SNAPSHOT/docs/client/registration.md) - Application registration process
* [Service Discovery](/4.0.0-SNAPSHOT/docs/client/service-discovery.md) - Metadata in service discovery
* [Security](/4.0.0-SNAPSHOT/docs/server/security.md) - Securing metadata transmission
