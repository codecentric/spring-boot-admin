import DocCardList from '@theme/DocCardList';

# Spring Boot Admin Server

The Spring Boot Admin Server acts as the core component for managing and monitoring multiple Spring Boot applications.
It collects health, metrics, and runtime information from registered applications and displays them on a user-friendly
web interface.

To set up the Spring Boot Admin Server, you'll need to create a Spring Boot application and add the Spring Boot Admin
Server Starter dependency. The server can operate as a Servlet or Reactive (WebFlux) application, depending on your
project setup.

**Key Features:**

* **Application Registration**: Clients (Spring Boot applications) register with the Admin Server via HTTP or service
  discovery mechanisms like Eureka or Consul.
* **Health Monitoring**: Provides an overview of the health status of registered applications via Spring Boot Actuator
  endpoints.
* **Metrics and Logs**: Displays key performance metrics and logs in real-time.
* **Management Actions**: Allows interaction with client applications for tasks like restarting, updating
  configurations, or triggering garbage collection.

The Admin Server itself is stateless, meaning it relies on its registered applications to periodically poll their
status. Once configured, the Admin Server dashboard provides a central view for managing all of your Spring Boot
services.

 <DocCardList />
