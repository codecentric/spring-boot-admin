# REST API Reference

Complete HTTP API reference for Spring Boot Admin Server.

## Base URL[​](#base-url "Direct link to Base URL")

Default: `http://localhost:8080`

With custom context path:

```
spring:
  boot:
    admin:
      context-path: /admin
```

Base URL becomes: `http://localhost:8080/admin`

## Content Types[​](#content-types "Direct link to Content Types")

* **Request**: `application/json`
* **Response**: `application/json` or `application/hal+json`
* **Streaming**: `text/event-stream` (Server-Sent Events)

## Authentication[​](#authentication "Direct link to Authentication")

If Spring Security is enabled, all endpoints require authentication:

```
curl -u user:password http://localhost:8080/instances
```

Or use token-based authentication as configured in your security setup.

## Instances API[​](#instances-api "Direct link to Instances API")

### Register Instance[​](#register-instance "Direct link to Register Instance")

Register a new instance with the Admin Server.

**Endpoint**: `POST /instances`

**Request Body**:

```
{
  "name": "my-service",
  "managementUrl": "http://localhost:8081/actuator",
  "healthUrl": "http://localhost:8081/actuator/health",
  "serviceUrl": "http://localhost:8081",
  "metadata": {
    "startup": "2026-02-07T10:00:00Z",
    "tags": {
      "environment": "production"
    }
  }
}
```

**Response**: `201 Created`

```
{
  "id": "abc123def456"
}
```

**Headers**:

* `Location`: `/instances/abc123def456`

**Example**:

```
curl -X POST http://localhost:8080/instances \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-service",
    "managementUrl": "http://localhost:8081/actuator",
    "healthUrl": "http://localhost:8081/actuator/health",
    "serviceUrl": "http://localhost:8081"
  }'
```

***

### List All Instances[​](#list-all-instances "Direct link to List All Instances")

Get all registered instances.

**Endpoint**: `GET /instances`

**Response**: `200 OK`

```
[
  {
    "id": "abc123def456",
    "version": 5,
    "registration": {
      "name": "my-service",
      "managementUrl": "http://localhost:8081/actuator",
      "healthUrl": "http://localhost:8081/actuator/health",
      "serviceUrl": "http://localhost:8081",
      "source": "http-api",
      "metadata": {}
    },
    "registered": true,
    "statusInfo": {
      "status": "UP",
      "details": {}
    },
    "statusTimestamp": "2026-02-07T10:05:00Z",
    "info": {},
    "endpoints": [
      {
        "id": "health",
        "url": "http://localhost:8081/actuator/health"
      },
      {
        "id": "metrics",
        "url": "http://localhost:8081/actuator/metrics"
      }
    ],
    "buildVersion": "1.0.0",
    "tags": {
      "environment": "production"
    }
  }
]
```

**Example**:

```
curl http://localhost:8080/instances
```

***

### List Instances by Name[​](#list-instances-by-name "Direct link to List Instances by Name")

Get all instances with a specific name.

**Endpoint**: `GET /instances?name={name}`

**Parameters**:

* `name` (required): Application name

**Response**: `200 OK`

Same as List All Instances, but filtered.

**Example**:

```
curl http://localhost:8080/instances?name=my-service
```

***

### Get Single Instance[​](#get-single-instance "Direct link to Get Single Instance")

Get details of a specific instance.

**Endpoint**: `GET /instances/{id}`

**Parameters**:

* `id` (path): Instance ID

**Response**: `200 OK`

```
{
  "id": "abc123def456",
  "version": 5,
  "registration": {
    "name": "my-service",
    "managementUrl": "http://localhost:8081/actuator",
    "healthUrl": "http://localhost:8081/actuator/health",
    "serviceUrl": "http://localhost:8081"
  },
  "registered": true,
  "statusInfo": {
    "status": "UP"
  },
  "endpoints": [...]
}
```

**Example**:

```
curl http://localhost:8080/instances/abc123def456
```

***

### Deregister Instance[​](#deregister-instance "Direct link to Deregister Instance")

Remove an instance from the registry.

**Endpoint**: `DELETE /instances/{id}`

**Parameters**:

* `id` (path): Instance ID

**Response**: `204 No Content`

**Example**:

```
curl -X DELETE http://localhost:8080/instances/abc123def456
```

***

### Instance Event Stream[​](#instance-event-stream "Direct link to Instance Event Stream")

Subscribe to real-time instance events via Server-Sent Events.

**Endpoint**: `GET /instances/events`

**Response**: `200 OK` (streaming)

**Content-Type**: `text/event-stream`

**Event Format**:

```
data:{"instance":"abc123","version":0,"type":"REGISTERED","timestamp":"2026-02-07T10:00:00Z","registration":{...}}

data:{"instance":"abc123","version":1,"type":"ENDPOINTS_DETECTED","timestamp":"2026-02-07T10:00:05Z","endpoints":[...]}

data:{"instance":"abc123","version":2,"type":"STATUS_CHANGED","timestamp":"2026-02-07T10:00:10Z","statusInfo":{"status":"UP"}}
```

**Example**:

```
curl -N http://localhost:8080/instances/events
```

**JavaScript Client**:

```
const eventSource = new EventSource('http://localhost:8080/instances/events');

eventSource.onmessage = (event) => {
  const instanceEvent = JSON.parse(event.data);
  console.log('Event:', instanceEvent.type, 'for', instanceEvent.instance);
};
```

**Heartbeat**: Ping comments sent every 10 seconds to keep connection alive.

***

### Instance Event Stream (Single Instance)[​](#instance-event-stream-single-instance "Direct link to Instance Event Stream (Single Instance)")

Subscribe to events for a specific instance.

**Endpoint**: `GET /instances/{id}/events`

**Parameters**:

* `id` (path): Instance ID

**Response**: Same as `/instances/events`, but filtered to single instance.

**Example**:

```
curl -N http://localhost:8080/instances/abc123def456/events
```

***

## Applications API[​](#applications-api "Direct link to Applications API")

Applications represent logical groups of instances with the same name.

### List All Applications[​](#list-all-applications "Direct link to List All Applications")

Get all applications (grouped instances).

**Endpoint**: `GET /applications`

**Response**: `200 OK`

```
[
  {
    "name": "my-service",
    "buildVersion": "1.0.0",
    "status": "UP",
    "instances": [
      {
        "id": "abc123",
        "healthUrl": "http://instance1:8081/actuator/health",
        "statusInfo": {"status": "UP"}
      },
      {
        "id": "def456",
        "healthUrl": "http://instance2:8081/actuator/health",
        "statusInfo": {"status": "UP"}
      }
    ]
  }
]
```

**Example**:

```
curl http://localhost:8080/applications
```

***

### Get Single Application[​](#get-single-application "Direct link to Get Single Application")

Get details of a specific application.

**Endpoint**: `GET /applications/{name}`

**Parameters**:

* `name` (path): Application name

**Response**: `200 OK`

```
{
  "name": "my-service",
  "buildVersion": "1.0.0",
  "status": "UP",
  "instances": [...]
}
```

**Response**: `404 Not Found` if application doesn't exist.

**Example**:

```
curl http://localhost:8080/applications/my-service
```

***

### Application Event Stream[​](#application-event-stream "Direct link to Application Event Stream")

Subscribe to application-level events.

**Endpoint**: `GET /applications/events`

**Response**: `200 OK` (streaming)

**Content-Type**: `text/event-stream`

**Example**:

```
curl -N http://localhost:8080/applications/events
```

***

### Refresh Applications[​](#refresh-applications "Direct link to Refresh Applications")

Trigger manual refresh of all instances from service discovery.

**Endpoint**: `POST /applications`

**Response**: `200 OK`

**Example**:

```
curl -X POST http://localhost:8080/applications
```

**Use Case**: Force refresh when using service discovery (Eureka, Consul, etc.)

***

### Delete Application[​](#delete-application "Direct link to Delete Application")

Deregister all instances of an application.

**Endpoint**: `DELETE /applications/{name}`

**Parameters**:

* `name` (path): Application name

**Response**: `204 No Content`

**Example**:

```
curl -X DELETE http://localhost:8080/applications/my-service
```

***

## Instance Actuator Proxy[​](#instance-actuator-proxy "Direct link to Instance Actuator Proxy")

Admin Server proxies requests to instance actuator endpoints.

### General Pattern[​](#general-pattern "Direct link to General Pattern")

**Endpoint**: `GET /instances/{id}/actuator/{endpoint}`

**Parameters**:

* `id` (path): Instance ID
* `endpoint` (path): Actuator endpoint name

**Response**: Proxied response from instance

**Examples**:

```
# Get health
curl http://localhost:8080/instances/abc123/actuator/health

# Get metrics
curl http://localhost:8080/instances/abc123/actuator/metrics

# Get specific metric
curl http://localhost:8080/instances/abc123/actuator/metrics/jvm.memory.used

# Get environment
curl http://localhost:8080/instances/abc123/actuator/env

# Get loggers
curl http://localhost:8080/instances/abc123/actuator/loggers
```

### Common Endpoints[​](#common-endpoints "Direct link to Common Endpoints")

| Endpoint                   | Description            |
| -------------------------- | ---------------------- |
| `/actuator/health`         | Health status          |
| `/actuator/info`           | Build and app info     |
| `/actuator/metrics`        | Metrics list           |
| `/actuator/metrics/{name}` | Specific metric        |
| `/actuator/env`            | Environment properties |
| `/actuator/loggers`        | Logger configuration   |
| `/actuator/loggers/{name}` | Specific logger        |
| `/actuator/httptrace`      | HTTP trace             |
| `/actuator/threaddump`     | Thread dump            |
| `/actuator/heapdump`       | Heap dump (binary)     |
| `/actuator/jolokia`        | JMX via Jolokia        |

### Modify Logger Level[​](#modify-logger-level "Direct link to Modify Logger Level")

**Endpoint**: `POST /instances/{id}/actuator/loggers/{name}`

**Request Body**:

```
{
  "configuredLevel": "DEBUG"
}
```

**Example**:

```
curl -X POST http://localhost:8080/instances/abc123/actuator/loggers/com.example \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'
```

***

## Instance Operations[​](#instance-operations "Direct link to Instance Operations")

### Restart Instance[​](#restart-instance "Direct link to Restart Instance")

Restart a Spring Boot application (requires `spring-boot-starter-actuator` with restart endpoint).

**Endpoint**: `POST /instances/{id}/actuator/restart`

**Response**: `200 OK`

**Example**:

```
curl -X POST http://localhost:8080/instances/abc123/actuator/restart
```

Dangerous Operation

This will restart the application. Ensure the restart endpoint is properly secured.

***

### Shutdown Instance[​](#shutdown-instance "Direct link to Shutdown Instance")

Gracefully shutdown a Spring Boot application.

**Endpoint**: `POST /instances/{id}/actuator/shutdown`

**Response**: `200 OK`

```
{
  "message": "Shutting down, bye..."
}
```

**Example**:

```
curl -X POST http://localhost:8080/instances/abc123/actuator/shutdown
```

Dangerous Operation

This will shut down the application. Ensure the shutdown endpoint is properly secured and consider it carefully in production.

***

## Error Responses[​](#error-responses "Direct link to Error Responses")

### 400 Bad Request[​](#400-bad-request "Direct link to 400 Bad Request")

Invalid request body or parameters.

```
{
  "error": "Bad Request",
  "message": "Invalid registration data",
  "status": 400
}
```

### 404 Not Found[​](#404-not-found "Direct link to 404 Not Found")

Instance or application not found.

```
{
  "error": "Not Found",
  "message": "Instance not found: abc123",
  "status": 404
}
```

### 500 Internal Server Error[​](#500-internal-server-error "Direct link to 500 Internal Server Error")

Server error.

```
{
  "error": "Internal Server Error",
  "message": "Failed to register instance",
  "status": 500
}
```

## CORS Support[​](#cors-support "Direct link to CORS Support")

Cross-Origin Resource Sharing (CORS) configuration:

```
spring:
  boot:
    admin:
      cors:
        allowed-origins: "http://localhost:3000"
        allowed-methods: "GET,POST,DELETE"
        allowed-headers: "*"
        exposed-headers: "Location"
        allow-credentials: true
        max-age: 3600
```

## Rate Limiting[​](#rate-limiting "Direct link to Rate Limiting")

No built-in rate limiting. Use reverse proxy (nginx, API gateway) for rate limiting if needed.

## Pagination[​](#pagination "Direct link to Pagination")

Instance and application lists are not paginated. For large deployments, consider filtering by name or using service discovery-based filtering.

## Caching[​](#caching "Direct link to Caching")

Responses are not cached by default. Add caching headers via reverse proxy if needed.

## WebSocket Support[​](#websocket-support "Direct link to WebSocket Support")

Not supported. Use Server-Sent Events (SSE) for real-time updates.

## API Clients[​](#api-clients "Direct link to API Clients")

### Java[​](#java "Direct link to Java")

```
RestTemplate restTemplate = new RestTemplate();

// Register instance
Registration registration = Registration.create("my-service")
		.managementUrl("http://localhost:8081/actuator")
		.healthUrl("http://localhost:8081/actuator/health")
		.serviceUrl("http://localhost:8081")
		.build();

ResponseEntity<Map> response = restTemplate.postForEntity(
		"http://localhost:8080/instances",
		registration,
		Map.class
);

String instanceId = (String) response.getBody().get("id");
```

### JavaScript/TypeScript[​](#javascripttypescript "Direct link to JavaScript/TypeScript")

```
// Register instance
const registration = {
  name: 'my-service',
  managementUrl: 'http://localhost:8081/actuator',
  healthUrl: 'http://localhost:8081/actuator/health',
  serviceUrl: 'http://localhost:8081'
};

const response = await fetch('http://localhost:8080/instances', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(registration)
});

const { id } = await response.json();
console.log('Instance ID:', id);
```

### Python[​](#python "Direct link to Python")

```
import requests

# Register instance
registration = {
    "name": "my-service",
    "managementUrl": "http://localhost:8081/actuator",
    "healthUrl": "http://localhost:8081/actuator/health",
    "serviceUrl": "http://localhost:8081"
}

response = requests.post(
    "http://localhost:8080/instances",
    json=registration
)

instance_id = response.json()["id"]
print(f"Instance ID: {instance_id}")
```

## See Also[​](#see-also "Direct link to See Also")

* [Event Types](/4.0.0/docs/reference/event-types.md)
* [Server Configuration](/4.0.0/docs/server/server.md)
* [Client Registration](/4.0.0/docs/client/registration.md)
* [Security](/4.0.0/docs/05-security/)
