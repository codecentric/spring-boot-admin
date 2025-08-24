import DocCardList from '@theme/DocCardList';

# Registering Clients

Spring Boot Admin is built on top of the mechanisms provided by Spring Cloud. This means that it can integrate seamlessly with any Spring Cloud–compliant service discovery tool. Common options include Eureka, Kubernetes, Nacos, and many others that implement the Spring Cloud Discovery interfaces.

When it comes to connecting services, Spring Boot Admin offers flexible configuration options. You can choose to
configure the services explicitly, so that the admin server is aware of them without relying on any discovery mechanism.
This approach is often useful in smaller environments or when the service landscape is relatively static.

Alternatively, you can leverage Spring Cloud’s service discovery features. In this mode, Spring Boot Admin automatically discovers and registers services that are available within the configured discovery system. This reduces manual configuration overhead and is particularly well-suited for dynamic, cloud-native environments where services may scale up and down frequently.

Importantly, Spring Boot Admin does not force you to choose one method exclusively. A hybrid setup is also possible, where some services are registered manually while others are discovered automatically through Spring Cloud. This allows you to tailor the setup to your specific infrastructure and operational needs, combining the stability of manual configuration with the flexibility of automated discovery.

**Key Features:**

* **Automatic Registration:** The client can self-register with the Admin Server by sending regular status updates.
* **Health and Metrics Exposure:** The client leverages Spring Boot Actuator to expose endpoints for monitoring health
  status, system metrics, application logs, and other runtime data.
* **Management Actions:** The Admin Server can interact with the client for actions such as restarting the application,
  clearing caches, or triggering log file downloads.
* **Secure Communication:** Spring Boot Admin supports configuring authentication and SSL to ensure secure communication
  between the client and the server.

 <DocCardList />
