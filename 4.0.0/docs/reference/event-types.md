# Event Types Reference

Complete reference of all `InstanceEvent` types emitted by Spring Boot Admin Server during instance lifecycle.

## Event Base Class[​](#event-base-class "Direct link to Event Base Class")

All events extend `InstanceEvent`:

```
public abstract class InstanceEvent implements Serializable {
	private final InstanceId instance;     // Unique instance identifier
	private final long version;            // Event version (incremental)
	private final Instant timestamp;       // When event occurred
	private final String type;             // Event type constant
}
```

**Common Properties**:

* `instance`: Unique ID of the instance (e.g., `"abc123def456"`)
* `version`: Monotonically increasing version number
* `timestamp`: ISO-8601 timestamp when event was created
* `type`: String constant identifying the event type

## Event Lifecycle[​](#event-lifecycle "Direct link to Event Lifecycle")

Typical event sequence for an instance:

```
1. REGISTERED           → Instance first registers
2. ENDPOINTS_DETECTED   → Actuator endpoints discovered
3. STATUS_CHANGED       → Health status updated to UP
4. INFO_CHANGED         → Info endpoint data loaded
5. STATUS_CHANGED       → Status changes during lifecycle
6. REGISTRATION_UPDATED → Registration info changes (optional)
7. DEREGISTERED         → Instance unregisters
```

## Event Types[​](#event-types "Direct link to Event Types")

### 1. REGISTERED[​](#1-registered "Direct link to 1. REGISTERED")

**Class**: `InstanceRegisteredEvent`

**Type Constant**: `"REGISTERED"`

**When Emitted**: Instance first registers with Admin Server

**Payload**:

```
public class InstanceRegisteredEvent extends InstanceEvent {
	Registration registration;  // Complete registration info
}
```

**Registration Contents**:

* `name`: Application name
* `managementUrl`: Actuator base URL
* `healthUrl`: Health endpoint URL
* `serviceUrl`: Application base URL
* `source`: Registration source (e.g., "http-api", "discovery")
* `metadata`: Custom metadata map

**Example**:

```
{
  "instance": "abc123def456",
  "version": 0,
  "timestamp": "2026-02-07T10:00:00Z",
  "type": "REGISTERED",
  "registration": {
    "name": "my-service",
    "managementUrl": "http://localhost:8080/actuator",
    "healthUrl": "http://localhost:8080/actuator/health",
    "serviceUrl": "http://localhost:8080",
    "source": "http-api",
    "metadata": {
      "startup": "2026-02-07T09:59:55Z"
    }
  }
}
```

**Use Cases**:

* Send welcome notifications
* Initialize instance-specific monitoring
* Log new instance registrations
* Trigger discovery of endpoints

**Example Listener**:

```

@Component
public class RegistrationListener {

	@EventListener
	public void onInstanceRegistered(InstanceRegisteredEvent event) {
		log.info("New instance registered: {} at {}",
				event.getRegistration().getName(),
				event.getRegistration().getServiceUrl());
	}
}
```

***

### 2. REGISTRATION\_UPDATED[​](#2-registration_updated "Direct link to 2. REGISTRATION_UPDATED")

**Class**: `InstanceRegistrationUpdatedEvent`

**Type Constant**: `"REGISTRATION_UPDATED"`

**When Emitted**: Instance updates its registration (URL, metadata, etc.)

**Payload**:

```
public class InstanceRegistrationUpdatedEvent extends InstanceEvent {
	Registration registration;  // Updated registration info
}
```

**Common Triggers**:

* Instance IP address changes
* Management port changes
* Metadata updates
* Health URL changes

**Example**:

```
{
  "instance": "abc123def456",
  "version": 5,
  "timestamp": "2026-02-07T11:00:00Z",
  "type": "REGISTRATION_UPDATED",
  "registration": {
    "name": "my-service",
    "managementUrl": "http://192.168.1.100:8080/actuator",
    "healthUrl": "http://192.168.1.100:8080/actuator/health",
    "serviceUrl": "http://192.168.1.100:8080",
    "metadata": {
      "version": "2.0.0"
    }
  }
}
```

**Use Cases**:

* Detect instance migrations
* Update monitoring endpoints
* Track configuration changes
* Trigger re-discovery of endpoints

***

### 3. DEREGISTERED[​](#3-deregistered "Direct link to 3. DEREGISTERED")

**Class**: `InstanceDeregisteredEvent`

**Type Constant**: `"DEREGISTERED"`

**When Emitted**: Instance unregisters (shutdown, explicit deregistration)

**Payload**:

```
public class InstanceDeregisteredEvent extends InstanceEvent {
	// No additional fields - just base InstanceEvent fields
}
```

**Example**:

```
{
  "instance": "abc123def456",
  "version": 10,
  "timestamp": "2026-02-07T12:00:00Z",
  "type": "DEREGISTERED"
}
```

**Use Cases**:

* Send shutdown notifications
* Clean up instance-specific resources
* Log instance lifecycle
* Trigger alerts for unexpected shutdowns

**Example Listener**:

```

@EventListener
public void onInstanceDeregistered(InstanceDeregisteredEvent event) {
	Instant timestamp = event.getTimestamp();
	long version = event.getVersion();

	log.info("Instance {} deregistered after {} events",
			event.getInstance(), version);

	// Cleanup resources
	cleanupResourcesFor(event.getInstance());
}
```

***

### 4. STATUS\_CHANGED[​](#4-status_changed "Direct link to 4. STATUS_CHANGED")

**Class**: `InstanceStatusChangedEvent`

**Type Constant**: `"STATUS_CHANGED"`

**When Emitted**: Instance health status changes

**Payload**:

```
public class InstanceStatusChangedEvent extends InstanceEvent {
	StatusInfo statusInfo;  // Current status information
}
```

**StatusInfo Contents**:

* `status`: Current status (`UP`, `DOWN`, `OUT_OF_SERVICE`, `UNKNOWN`, `OFFLINE`)
* `details`: Map of health details from actuator

**Status Values**:

* `UP`: Application is healthy
* `DOWN`: Application is unhealthy
* `OUT_OF_SERVICE`: Temporarily unavailable
* `UNKNOWN`: Status cannot be determined
* `OFFLINE`: Instance not responding
* `RESTRICTED`: Custom status

**Example**:

```
{
  "instance": "abc123def456",
  "version": 3,
  "timestamp": "2026-02-07T10:05:00Z",
  "type": "STATUS_CHANGED",
  "statusInfo": {
    "status": "UP",
    "details": {
      "diskSpace": {
        "status": "UP",
        "total": 500000000000,
        "free": 250000000000
      },
      "db": {
        "status": "UP",
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

**Use Cases**:

* Send alerts on status changes (UP → DOWN)
* Track uptime/downtime
* Trigger automated recovery
* Update dashboards

**Example Listener**:

```

@EventListener
public void onStatusChanged(InstanceStatusChangedEvent event) {
	StatusInfo statusInfo = event.getStatusInfo();
	String status = statusInfo.getStatus();

	if ("DOWN".equals(status)) {
		alertService.sendAlert(
				"Instance " + event.getInstance() + " is DOWN",
				statusInfo.getDetails()
		);
	}
}
```

***

### 5. ENDPOINTS\_DETECTED[​](#5-endpoints_detected "Direct link to 5. ENDPOINTS_DETECTED")

**Class**: `InstanceEndpointsDetectedEvent`

**Type Constant**: `"ENDPOINTS_DETECTED"`

**When Emitted**: Actuator endpoints are discovered

**Payload**:

```
public class InstanceEndpointsDetectedEvent extends InstanceEvent {
	Endpoints endpoints;  // Discovered endpoints
}
```

**Endpoints Contents**: List of `Endpoint` objects, each containing:

* `id`: Endpoint ID (e.g., "health", "metrics", "env")
* `url`: Full endpoint URL

**Example**:

```
{
  "instance": "abc123def456",
  "version": 1,
  "timestamp": "2026-02-07T10:00:05Z",
  "type": "ENDPOINTS_DETECTED",
  "endpoints": [
    {
      "id": "health",
      "url": "http://localhost:8080/actuator/health"
    },
    {
      "id": "metrics",
      "url": "http://localhost:8080/actuator/metrics"
    },
    {
      "id": "env",
      "url": "http://localhost:8080/actuator/env"
    },
    {
      "id": "loggers",
      "url": "http://localhost:8080/actuator/loggers"
    }
  ]
}
```

**Use Cases**:

* Enable/disable UI views based on available endpoints
* Start monitoring specific endpoints
* Validate expected endpoints are present
* Trigger custom endpoint polling

**Example Listener**:

```

@EventListener
public void onEndpointsDetected(InstanceEndpointsDetectedEvent event) {
	Endpoints endpoints = event.getEndpoints();

	boolean hasMetrics = endpoints.get("metrics").isPresent();
	boolean hasLoggers = endpoints.get("loggers").isPresent();

	if (hasMetrics && hasLoggers) {
		// Enable advanced monitoring
		advancedMonitoring.enable(event.getInstance());
	}
}
```

***

### 6. INFO\_CHANGED[​](#6-info_changed "Direct link to 6. INFO_CHANGED")

**Class**: `InstanceInfoChangedEvent`

**Type Constant**: `"INFO_CHANGED"`

**When Emitted**: Info endpoint data changes

**Payload**:

```
public class InstanceInfoChangedEvent extends InstanceEvent {
	Info info;  // Info endpoint data
}
```

**Info Contents**: Map of arbitrary info data from `/actuator/info`, commonly including:

* `build`: Build information (version, time, artifact)
* `git`: Git information (commit, branch, time)
* Custom application metadata

**Example**:

```
{
  "instance": "abc123def456",
  "version": 2,
  "timestamp": "2026-02-07T10:00:10Z",
  "type": "INFO_CHANGED",
  "info": {
    "build": {
      "version": "1.0.0",
      "artifact": "my-service",
      "name": "my-service",
      "time": "2026-02-07T09:00:00Z"
    },
    "git": {
      "branch": "main",
      "commit": {
        "id": "abc123",
        "time": "2026-02-06T15:30:00Z"
      }
    },
    "custom": {
      "team": "Platform",
      "environment": "production"
    }
  }
}
```

**Use Cases**:

* Track deployed versions
* Display build information in UI
* Verify correct version deployed
* Trigger version-specific logic

***

## Event Ordering[​](#event-ordering "Direct link to Event Ordering")

Events are ordered by `version` number, which is monotonically increasing per instance:

```
version 0: REGISTERED
version 1: ENDPOINTS_DETECTED
version 2: STATUS_CHANGED (to UP)
version 3: INFO_CHANGED
version 4: STATUS_CHANGED (to DOWN)
version 5: STATUS_CHANGED (to UP)
version 6: DEREGISTERED
```

**Important**: Version numbers are unique per instance and always increase.

## Event Persistence[​](#event-persistence "Direct link to Event Persistence")

Events are stored in the `InstanceEventStore`:

* **InMemoryEventStore**: Non-persistent, lost on restart
* **HazelcastEventStore**: Distributed, persisted across cluster

**Event Compaction**: Old events are compacted to prevent unlimited growth:

```
spring:
  boot:
    admin:
      event-store:
        max-log-size-per-aggregate: 100  # Keep last 100 events per instance
```

## Listening to Events[​](#listening-to-events "Direct link to Listening to Events")

### Spring Event Listener[​](#spring-event-listener "Direct link to Spring Event Listener")

```

@Component
public class MyEventListener {

	@EventListener
	public void onAnyInstanceEvent(InstanceEvent event) {
		log.info("Event: {} for instance {} at version {}",
				event.getType(), event.getInstance(), event.getVersion());
	}

	@EventListener
	public void onSpecificEvent(InstanceStatusChangedEvent event) {
		// Handle specific event type
	}
}
```

### Custom Notifier[​](#custom-notifier "Direct link to Custom Notifier")

```

@Component
public class CustomNotifier extends AbstractEventNotifier {

	public CustomNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono.fromRunnable(() -> {
			switch (event.getType()) {
				case "STATUS_CHANGED":
					handleStatusChange((InstanceStatusChangedEvent) event);
					break;
				case "REGISTERED":
					handleRegistration((InstanceRegisteredEvent) event);
					break;
				// Handle other events
			}
		});
	}
}
```

### Event Stream (SSE)[​](#event-stream-sse "Direct link to Event Stream (SSE)")

Subscribe to event stream via REST API:

```
curl -N http://localhost:8080/instances/events
```

Returns Server-Sent Events (SSE) stream:

```
data:{"instance":"abc123","version":0,"type":"REGISTERED",...}

data:{"instance":"abc123","version":1,"type":"ENDPOINTS_DETECTED",...}

data:{"instance":"abc123","version":2,"type":"STATUS_CHANGED",...}
```

## Event Filtering[​](#event-filtering "Direct link to Event Filtering")

Filter events by type using `FilteringNotifier`:

```

@Bean
public FilteringNotifier filteringNotifier(Notifier delegate,
										   InstanceRepository repository) {
	FilteringNotifier notifier = new FilteringNotifier(delegate, repository);
	notifier.setFilterExpression("!(type == 'INFO_CHANGED')");  // Exclude INFO_CHANGED
	return notifier;
}
```

**Filter Expression Language**: SpEL (Spring Expression Language)

**Available Variables**:

* `type`: Event type string
* `instance`: Instance ID
* `version`: Event version
* Event-specific fields (e.g., `statusInfo.status` for STATUS\_CHANGED)

**Examples**:

```
// Only DOWN events
"type == 'STATUS_CHANGED' && statusInfo.status == 'DOWN'"

// Exclude INFO_CHANGED and ENDPOINTS_DETECTED
		"!(type == 'INFO_CHANGED' || type == 'ENDPOINTS_DETECTED')"

// Only production instances (via metadata)
		"metadata['environment'] == 'production'"
```

## Event Reminders[​](#event-reminders "Direct link to Event Reminders")

Use `RemindingNotifier` to send periodic reminders:

```

@Bean
public RemindingNotifier remindingNotifier(Notifier delegate,
										   InstanceRepository repository) {
	RemindingNotifier notifier = new RemindingNotifier(delegate, repository);
	notifier.setReminderPeriod(Duration.ofMinutes(10));
	notifier.setCheckReminderInverval(Duration.ofSeconds(60));
	return notifier;
}
```

Sends reminders for instances still in non-UP status after configured period.

## See Also[​](#see-also "Direct link to See Also")

* [Custom Notifiers](/4.0.0/docs/server/notifications/custom-notifiers.md)
* [Instance Registry](/4.0.0/docs/server/instance-registry.md)
* [REST API](/4.0.0/docs/reference/rest-api.md)
