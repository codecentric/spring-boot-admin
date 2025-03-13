import DocCardList from '@theme/DocCardList';

# Spring Boot Admin Client

The Spring Boot Admin Client is a Spring Boot application that registers itself with the Spring Boot Admin Server to
enable monitoring and management. By including the Spring Boot Admin Client Starter dependency in your application, the
Spring Boot Admin Server can automatically access health, metrics, and other management endpoints, depending on which
Actuator endpoints are accessible.

**Key Features:**

* **Automatic Registration:** The client can self-register with the Admin Server by sending regular status updates.
* **Health and Metrics Exposure:** The client leverages Spring Boot Actuator to expose endpoints for monitoring health
  status, system metrics, application logs, and other runtime data.
* **Management Actions:** The Admin Server can interact with the client for actions such as restarting the application,
  clearing caches, or triggering log file downloads.
* **Secure Communication:** The client supports configuring authentication and SSL to ensure secure communication
  between the client and the Admin Server.

By adding the Spring Boot Admin Client to your applications, they become discoverable by the Admin Server, enabling
centralized monitoring, alerting, and management. This makes it easy to monitor the health of your entire system in
real-time.

 <DocCardList />
