# Spring Boot Admin Server

The Spring Boot Admin Server acts as the core component for managing and monitoring multiple Spring Boot applications. It collects health, metrics, and runtime information from registered applications and displays them on a user-friendly web interface.

To set up the Spring Boot Admin Server, you'll need to create a Spring Boot application and add the Spring Boot Admin Server Starter dependency. The server can operate as a Servlet or Reactive (WebFlux) application, depending on your project setup.

**Key Features:**

* **Application Registration**: Clients (Spring Boot applications) register with the Admin Server via HTTP or service discovery mechanisms like Eureka or Consul.
* **Health Monitoring**: Provides an overview of the health status of registered applications via Spring Boot Actuator endpoints.
* **Metrics and Logs**: Displays key performance metrics and logs in real-time.
* **Management Actions**: Allows interaction with client applications for tasks like restarting, updating configurations, or triggering garbage collection.

The Admin Server itself is stateless, meaning it relies on its registered applications to periodically poll their status. Once configured, the Admin Server dashboard provides a central view for managing all of your Spring Boot services.

## [<!-- -->Set up server](/4.0.1/docs/server/server.md)

[Running Behind a Front-end Proxy Server](/4.0.1/docs/server/server.md)

## [<!-- -->Foster Security](/4.0.1/docs/server/security.md)

[Since there are several approaches on solving authentication and authorization in distributed web applications Spring](/4.0.1/docs/server/security.md)

## [<!-- -->Server(-Sent) Events](/4.0.1/docs/server/Events.md)

[Spring Boot Admin uses event sourcing per default to track changes of registered applications.](/4.0.1/docs/server/Events.md)

## [<!-- -->Clustering](/4.0.1/docs/server/Clustering.md)

[Spring Boot Admin Server supports cluster replication via Hazelcast. It is automatically enabled when a HazelcastConfig\\- or HazelcastInstance\\-Bean is present.](/4.0.1/docs/server/Clustering.md)

## [<!-- -->Persistence and Event Store](/4.0.1/docs/server/persistence.md)

[Spring Boot Admin uses an event-sourced architecture to track the state of registered applications. All changes to](/4.0.1/docs/server/persistence.md)

## [<!-- -->Instance Registry](/4.0.1/docs/server/instance-registry.md)

[The Instance Registry is the core component responsible for managing registered applications in Spring Boot Admin. It](/4.0.1/docs/server/instance-registry.md)

## [<!-- -->Notifications](/4.0.1/docs/server/notifications/.md)

[Configure notifications to alert teams about instance status changes via email, Slack, Teams, and other channels.](/4.0.1/docs/server/notifications/.md)

## [<!-- -->Properties](/4.0.1/docs/server/server-properties.md)

[\<PropertyTable](/4.0.1/docs/server/server-properties.md)
