# Server(-Sent) Events

Spring Boot Admin uses event sourcing per default to track changes of registered applications. Every change (see relevant events below) of an application is stored as an event in memory. This allows to reconstruct the state of an application at any point in time, as far as the server is not restarted.

info

Server Events in Spring Boot Admin are not the same as Spring Boot's AuditEvent actuator events. Server Events track changes and lifecycle of registered instances for monitoring and UI purposes, while AuditEvents are used for auditing application actions and security events.

## Event Endpoints[​](#event-endpoints "Direct link to Event Endpoints")

Spring Boot Admin utilizes several endpoints for accessing instance events via the `InstancesController`:

### List All Events[​](#list-all-events "Direct link to List All Events")

* **GET `/instances/events`**
* Returns all instance events as a JSON array.
* Use this to retrieve the complete event history for all registered instances on UI startup.

### Stream All Events[​](#stream-all-events "Direct link to Stream All Events")

* **GET `/instances/events`** (with `Accept: text/event-stream`)
* Returns a continuous stream of instance events using [Server-Sent Events (SSE)](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events).
* Useful for real-time monitoring and UI updates.

### Stream Events for a Specific Instance[​](#stream-events-for-a-specific-instance "Direct link to Stream Events for a Specific Instance")

* **GET `/instances/{id}`** (with `Accept: text/event-stream`)
* Streams events for a single instance, identified by its ID.

## Available Events[​](#available-events "Direct link to Available Events")

### InstanceRegisteredEvent[​](#instanceregisteredevent "Direct link to InstanceRegisteredEvent")

Indicates that a new instance has been registered with Spring Boot Admin. This event is used to add the instance to SBA and starts monitoring its status and endpoints.

### InstanceRegistrationUpdatedEvent[​](#instanceregistrationupdatedevent "Direct link to InstanceRegistrationUpdatedEvent")

Signals that the registration details of an instance have changed (e.g., new management URL or changed registration source). This event is used to update how the instance is accessed and monitored.

### InstanceEndpointsDetectedEvent[​](#instanceendpointsdetectedevent "Direct link to InstanceEndpointsDetectedEvent")

Signals that the endpoints (such as actuator endpoints) of an instance have been detected or updated. This event is used to refresh the available operations and monitoring features for the instance in the UI.

### InstanceInfoChangedEvent[​](#instanceinfochangedevent "Direct link to InstanceInfoChangedEvent")

Occurs when the metadata or information (e.g., version, build info, tags) of an instance changes. This event is used to update the displayed details about the instance in the UI.

### InstanceStatusChangedEvent[​](#instancestatuschangedevent "Direct link to InstanceStatusChangedEvent")

Occurs when the status of an instance changes (e.g., from UP to DOWN, or vice versa). This event is used to update the health indicator and status badge in the UI, and may trigger notifications or alerts.

### InstanceDeregisteredEvent[​](#instancederegisteredevent "Direct link to InstanceDeregisteredEvent")

Indicates that an instance has been unregistered from Spring Boot Admin. This event is used to remove the instance from the UI and stop monitoring its status and endpoints.

## Viewing Events in the UI Journal[​](#viewing-events-in-the-ui-journal "Direct link to Viewing Events in the UI Journal")

All instance events described above can be viewed in the Journal section of the Spring Boot Admin UI. The Journal provides a chronological log of all relevant events for each registered instance, allowing users to track changes, status updates, and lifecycle actions directly from the web interface.

This feature helps administrators and operators to:

* Audit the history of instance registrations, status changes, and endpoint updates
* Troubleshoot issues by reviewing the sequence of events
* Gain insights into the operational state of monitored applications
