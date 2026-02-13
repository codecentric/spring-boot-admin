# Reference Documentation

Comprehensive reference material for Spring Boot Admin including event types, REST API, and configuration properties.

## Contents[​](#contents "Direct link to Contents")

### [Event Types](/4.0.0-SNAPSHOT/docs/reference/event-types.md)[​](#event-types "Direct link to event-types")

Complete catalog of all `InstanceEvent` types emitted by Spring Boot Admin, including:

* Event lifecycle and ordering
* Event payloads and properties
* Common use cases for each event
* Example event listeners

### [REST API](/4.0.0-SNAPSHOT/docs/reference/rest-api.md)[​](#rest-api "Direct link to rest-api")

HTTP API reference for Spring Boot Admin Server (intended for internal use by the SPA, not for public consumption):

* Instance registration endpoints
* Application management endpoints
* Event streaming
* Instance operations (restart, shutdown, etc.)

**Note**: The REST API is primarily designed for use by the built-in Single Page Application (SPA) and should not be considered a stable public API. Use at your own risk for external integrations.

## Quick Links[​](#quick-links "Direct link to Quick Links")

### Event Types[​](#event-types-1 "Direct link to Event Types")

All events extend `InstanceEvent` base class:

| Event Type             | Description                   |
| ---------------------- | ----------------------------- |
| `REGISTERED`           | Instance first registered     |
| `REGISTRATION_UPDATED` | Instance registration changed |
| `DEREGISTERED`         | Instance unregistered         |
| `STATUS_CHANGED`       | Health status changed         |
| `ENDPOINTS_DETECTED`   | Actuator endpoints discovered |
| `INFO_CHANGED`         | Info endpoint data changed    |

### Common Properties[​](#common-properties "Direct link to Common Properties")

#### Server Context Path[​](#server-context-path "Direct link to Server Context Path")

```
spring:
  boot:
    admin:
      context-path: /admin  # Default: /
```

#### Client Registration[​](#client-registration "Direct link to Client Registration")

```
spring:
  boot:
    admin:
      client:
        url: http://localhost:8080
        instance:
          name: ${spring.application.name}
```

### Status Values[​](#status-values "Direct link to Status Values")

Health status values in order of precedence:

1. `DOWN` - Application not healthy
2. `OUT_OF_SERVICE` - Temporarily unavailable
3. `OFFLINE` - Instance not responding
4. `UNKNOWN` - Status cannot be determined
5. `UP` - Application healthy
6. `RESTRICTED` - Custom status (application-defined)

## API Versioning[​](#api-versioning "Direct link to API Versioning")

Spring Boot Admin does not version its REST API. The API is primarily intended for internal use by the SPA and is not guaranteed to be stable for external integrations.

**Base Path**: `{server.context-path}/instances` (default: `/instances`)

**Content Type**: `application/json` (HAL JSON for collection endpoints)

**Stability**: The REST API is designed for internal use by the built-in SPA and may change without notice. For stable integrations, consider using the event notification system instead.

## Property Prefixes[​](#property-prefixes "Direct link to Property Prefixes")

All Spring Boot Admin properties use these prefixes:

### Server Properties[​](#server-properties "Direct link to Server Properties")

* `spring.boot.admin.*` - Core server configuration
* `spring.boot.admin.ui.*` - UI customization
* `spring.boot.admin.discovery.*` - Service discovery
* `spring.boot.admin.monitor.*` - Monitoring settings
* `spring.boot.admin.notify.*` - Notification settings

### Client Properties[​](#client-properties "Direct link to Client Properties")

* `spring.boot.admin.client.*` - Client configuration
* `spring.boot.admin.client.instance.*` - Instance metadata

## Configuration Metadata[​](#configuration-metadata "Direct link to Configuration Metadata")

Spring Boot Admin provides complete configuration metadata for IDE autocomplete:

```
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-server</artifactId>
</dependency>
```

Metadata files:

* `spring-configuration-metadata.json` (server)
* `additional-spring-configuration-metadata.json` (client)

## See Also[​](#see-also "Direct link to See Also")

* [Server Configuration](/4.0.0-SNAPSHOT/docs/server/server.md)
* [Client Configuration](/4.0.0-SNAPSHOT/docs/client/registration.md)
* [Customization](/4.0.0-SNAPSHOT/docs/06-customization/)
* [Notifications](/4.0.0-SNAPSHOT/docs/02-server/notifications/)
