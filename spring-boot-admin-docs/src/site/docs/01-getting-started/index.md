---
sidebar_position: 2
sidebar_custom_props:
  icon: 'rocket'
---

# Getting Started

Spring Boot Admin follows a server-client architecture designed to provide centralized monitoring and management of
Spring Boot applications. This guide will help you quickly set up both the server and client components.

## Architecture Overview

Spring Boot Admin consists of two main components:

- **Server**: A centralized monitoring hub that provides a web-based UI and aggregates data from multiple applications
- **Client**: Applications that register themselves with the server and expose management endpoints

The server continuously polls the clients' Actuator endpoints to collect health status, metrics, and other management
information, making this data available through an intuitive dashboard.

## Quick Start

The fastest way to get started with Spring Boot Admin:

1. **Set up the Admin Server** - Create a Spring Boot application with `@EnableAdminServer`
2. **Register your applications** - Add the Admin Client to your applications or use Spring Cloud Discovery
3. **Access the dashboard** - Navigate to your server URL to view and manage your applications

## Prerequisites

- Java 17 or higher
- Spring Boot 3.0 or higher
- Maven or Gradle build tool

:::note
Spring Boot Admin 3.x requires Spring Boot 3.x. For Spring Boot 2.x applications, use Spring Boot Admin 2.x.
:::

## What's Next?

- [Server Setup](./10-server-setup.md) - Learn how to configure the Admin Server
- [Client Registration](./20-client-registration.md) - Discover different ways to register your applications

## Motivation

In modern microservices architecture, monitoring and managing distributed systems is complex and challenging. Spring
Boot Admin provides a powerful solution for visualizing, monitoring, and managing Spring Boot applications in real-time.

By offering a web interface that aggregates the health and metrics of all attached services, Spring Boot Admin
simplifies the process of ensuring system stability and performance. Whether you need insights into application health,
memory usage, or log output, Spring Boot Admin offers a centralized tool that streamlines operational management.

:::info
While Spring Boot Admin offers a user-friendly and centralized interface for monitoring Spring Boot applications, it is
not designed to replace sophisticated, full-scale monitoring and observability tools like Grafana, Datadog, or Instana.
These tools provide advanced capabilities such as real-time alerting, history data, complex metric analysis, distributed
tracing, and customizable dashboards across diverse environments.

Spring Boot Admin excels at providing a lightweight, application-centric view with essential health checks, metrics, and
management endpoints. For production-grade observability in larger, more complex systems, integrating Spring Boot Admin
alongside these advanced platforms ensures comprehensive system monitoring and deep insights.
:::
