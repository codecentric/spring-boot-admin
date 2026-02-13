---
sidebar_position: 40
sidebar_custom_props:
  icon: 'puzzle'
---

# Integration

Spring Boot Admin integrates seamlessly with various service discovery solutions and clustering technologies. This
section covers how to set up and configure these integrations.

## Service Discovery

Instead of using the Spring Boot Admin Client library, you can leverage Spring Cloud Discovery services to automatically
register applications:

- **[Eureka](./10-eureka.md)** - Netflix Eureka service discovery
- **[Consul](./20-consul.md)** - HashiCorp Consul service mesh
- **[Zookeeper](./30-zookeeper.md)** - Apache Zookeeper coordination service

### Benefits

- No client library dependency required
- Automatic discovery of new instances
- Built-in health checking
- Service metadata support
- Load balancing integration

## Clustering

For high-availability deployments, Spring Boot Admin supports clustering:

- **[Hazelcast](./40-hazelcast.md)** - Distributed event store and coordination

### Benefits

- Shared event store across cluster nodes
- No single point of failure
- Automatic synchronization
- Distributed notifications

## Choosing an Integration

### Use Service Discovery When:

- You already have a service discovery infrastructure
- Running in a microservices environment
- Need automatic service registration
- Want to leverage existing service mesh features

### Use Direct Client Registration When:

- Simple deployment with few applications
- No service discovery infrastructure
- Need full control over registration
- Running in traditional environments

### Use Clustering When:

- Require high availability
- Multiple Admin Server instances
- Need shared state across servers
- Running in production with SLAs

## Integration Patterns

### Pattern 1: Service Discovery Only

```
Applications → Service Discovery (Eureka/Consul) ← Admin Server
```

Applications register with service discovery, Admin Server discovers them automatically.

### Pattern 2: Direct Registration with Clustering

```
Applications → Admin Server 1 ←→ Hazelcast ←→ Admin Server 2 ← Applications
```

Applications use client library, Admin Servers share state via Hazelcast.

### Pattern 3: Service Discovery with Clustering

```
Applications → Service Discovery ← Admin Server 1 ←→ Hazelcast ←→ Admin Server 2
```

Combines automatic discovery with high availability.

## Quick Comparison

| Feature              | Eureka    | Consul         | Zookeeper    | Hazelcast  |
|----------------------|-----------|----------------|--------------|------------|
| Type                 | Discovery | Discovery + KV | Coordination | Clustering |
| Setup Complexity     | Medium    | Medium         | High         | Low        |
| Spring Cloud Support | Excellent | Excellent      | Good         | N/A        |
| Health Checks        | Built-in  | Built-in       | Custom       | N/A        |
| Metadata Support     | Yes       | Limited        | Yes          | N/A        |
| HA                   | Yes       | Yes            | Yes          | Yes        |
| Persistence          | In-memory | Persistent     | Persistent   | In-memory  |

## Getting Started

1. Choose your integration based on your infrastructure
2. Follow the specific guide for setup instructions
3. Configure your applications appropriately
4. Test the integration in development
5. Deploy to production with monitoring

## See Also

- [Service Discovery](../03-client/40-service-discovery.md) - Client-side discovery configuration
- [Clustering](../02-server/20-Clustering.mdx) - Admin Server clustering details
- [Samples](../09-samples/) - Working example projects
