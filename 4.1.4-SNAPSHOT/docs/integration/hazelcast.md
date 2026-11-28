# Hazelcast Clustering

Hazelcast provides distributed data structures for clustering multiple Spring Boot Admin Server instances. This enables high availability and shared state across servers.

## Overview[​](#overview "Direct link to Overview")

With Hazelcast clustering:

* Multiple Admin Server instances share event store
* No single point of failure
* Automatic synchronization across nodes
* Distributed notifications

## Architecture[​](#architecture "Direct link to Architecture")

<!-- -->

## Why Hazelcast?[​](#why-hazelcast "Direct link to Why Hazelcast?")

* **High Availability**: No single point of failure
* **Scalability**: Add more Admin Server instances
* **Shared State**: All servers see the same application state
* **Distributed Events**: Events propagated across cluster
* **Simple Setup**: Minimal configuration required

## Setting Up Hazelcast[​](#setting-up-hazelcast "Direct link to Setting Up Hazelcast")

### Add Dependencies[​](#add-dependencies "Direct link to Add Dependencies")

pom.xml

```
<dependencies>

    <dependency>

        <groupId>de.codecentric</groupId>

        <artifactId>spring-boot-admin-starter-server</artifactId>

    </dependency>

    <dependency>

        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-webflux</artifactId>

    </dependency>

    <dependency>

        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-hazelcast</artifactId>

    </dependency>

    <dependency>

        <groupId>com.hazelcast</groupId>

        <artifactId>hazelcast</artifactId>

    </dependency>

</dependencies>
```

`spring-boot-starter-hazelcast` is declared as an optional dependency of `spring-boot-admin-server`, so it is not pulled in transitively by `spring-boot-admin-starter-server`. Without it, Spring Boot does not auto-configure a `HazelcastInstance` and the Admin Server nodes do not form a cluster.

### Configure Hazelcast[​](#configure-hazelcast "Direct link to Configure Hazelcast")

HazelcastConfig.java

```
import com.hazelcast.config.Config;

import com.hazelcast.config.EvictionConfig;

import com.hazelcast.config.EvictionPolicy;

import com.hazelcast.config.InMemoryFormat;

import com.hazelcast.config.MapConfig;

import com.hazelcast.config.MergePolicyConfig;

import com.hazelcast.config.MaxSizePolicy;

import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;



@Configuration

public class HazelcastConfig {



    @Bean

    public Config hazelcastConfig() {

        MapConfig eventStoreMap = new MapConfig("spring-boot-admin-event-store")

            .setInMemoryFormat(InMemoryFormat.OBJECT)

            .setBackupCount(1)

            .setMergePolicyConfig(new MergePolicyConfig(

                PutIfAbsentMergePolicy.class.getName(), 100));



        MapConfig sentNotificationsMap = new MapConfig("spring-boot-admin-sent-notifications")

            .setInMemoryFormat(InMemoryFormat.OBJECT)

            .setBackupCount(1)

            .setEvictionConfig(new EvictionConfig()

                .setEvictionPolicy(EvictionPolicy.LRU)

                .setMaxSizePolicy(MaxSizePolicy.PER_NODE))

            .setMergePolicyConfig(new MergePolicyConfig(

                PutIfAbsentMergePolicy.class.getName(), 100));



        MapConfig notificationFiltersMap = new MapConfig("spring-boot-admin-notification-filters")

            .setInMemoryFormat(InMemoryFormat.OBJECT)

            .setBackupCount(1)

            .setMergePolicyConfig(new MergePolicyConfig(

                PutIfAbsentMergePolicy.class.getName(), 100));



        Config config = new Config();

        config.addMapConfig(eventStoreMap);

        config.addMapConfig(sentNotificationsMap);

        config.addMapConfig(notificationFiltersMap);

        config.setProperty("hazelcast.jmx", "true");



        // Development example: use multicast for cluster discovery.

        config.getNetworkConfig()

            .getJoin()

            .getMulticastConfig()

            .setEnabled(true);



        return config;

    }

}
```

The Hazelcast starter uses this `Config` bean to create the `HazelcastInstance`. Do not create a `HazelcastInstance` or an `InstanceEventStore` bean for the standard setup; Spring Boot Admin configures those automatically.

To load an external Hazelcast XML or YAML file instead, point Spring Boot at it with `spring.hazelcast.config`:

application.yml

```
spring:

  hazelcast:

    config: classpath:hazelcast.yaml
```

The referenced file must use Hazelcast's configuration format. Hazelcast settings placed directly under `hazelcast.*` in Spring Boot's `application.yml` are not bound as Spring Boot application properties.

### Basic Configuration[​](#basic-configuration "Direct link to Basic Configuration")

The `Config` bean above uses Hazelcast's default multicast discovery. For a different discovery mechanism, configure the Hazelcast `Config` bean or use an external Hazelcast configuration file via `spring.hazelcast.config`.

### Selective Clustering[​](#selective-clustering "Direct link to Selective Clustering")

The event store, sent-notifications map, and notification-filters map can be disabled independently. All three maps are enabled by default:

application.yml

```
spring:

  boot:

    admin:

      hazelcast:

        event-store-enabled: false

        sent-notifications-enabled: true

        notification-filters-enabled: false
```

When the event-store map is disabled, Spring Boot Admin uses its in-memory event store and still applies `spring.boot.admin.event-store.max-log-size-per-aggregate` and `spring.boot.admin.event-store.prune-info-updated-events`.

When the sent-notifications map is disabled, notifications are not deduplicated across the cluster, so every node sends its own notifications. When the notification-filters map is disabled, the `notificationFiltersMap` bean is a local map, and filters are not shared. All switches are evaluated at runtime and work with AOT.

These switches apply to Spring Boot Admin's auto-configured beans. If you declare your own `InstanceEventStore` bean, it replaces the auto-configured event store, so `event-store-enabled` no longer selects between the Hazelcast and in-memory stores. A `Config` bean alone does not disable these switches.

## Network Configuration[​](#network-configuration "Direct link to Network Configuration")

### Multicast (Development)[​](#multicast-development "Direct link to Multicast (Development)")

Automatic discovery using multicast:

```
config.getNetworkConfig()

    .getJoin()

    .getMulticastConfig()

    .setEnabled(true)

    .setMulticastGroup("224.2.2.3")

    .setMulticastPort(54327);
```

For a Hazelcast YAML configuration file, set the equivalent Hazelcast `network.join.multicast` options in that file and load it with `spring.hazelcast.config` as shown above.

### TCP/IP (Production)[​](#tcpip-production "Direct link to TCP/IP (Production)")

Explicit member list for production:

```
config.getNetworkConfig()

    .getJoin()

    .getMulticastConfig()

    .setEnabled(false);



config.getNetworkConfig()

    .getJoin()

    .getTcpIpConfig()

    .setEnabled(true)

    .addMember("192.168.1.100")

    .addMember("192.168.1.101")

    .addMember("192.168.1.102");
```

For a Hazelcast YAML configuration file, put the member list under Hazelcast's `network.join.tcp-ip` configuration and load the file with `spring.hazelcast.config` as shown above.

### Kubernetes[​](#kubernetes "Direct link to Kubernetes")

For Kubernetes deployments:

```
<dependency>

    <groupId>com.hazelcast</groupId>

    <artifactId>hazelcast-kubernetes</artifactId>

</dependency>
```

```
config.getNetworkConfig()

    .getJoin()

    .getMulticastConfig()

    .setEnabled(false);



config.getNetworkConfig()

    .getJoin()

    .getKubernetesConfig()

    .setEnabled(true)

    .setProperty("namespace", "default")

    .setProperty("service-name", "spring-boot-admin");
```

## Event Store Configuration[​](#event-store-configuration "Direct link to Event Store Configuration")

### Map Configuration[​](#map-configuration "Direct link to Map Configuration")

```
MapConfig mapConfig = new MapConfig("spring-boot-admin-event-store")

    .setBackupCount(1)  // Number of backup copies

    .setAsyncBackupCount(0)  // Async backups

    .setTimeToLiveSeconds(0)  // No expiration

    .setMaxIdleSeconds(0)  // No idle timeout

    .setMergePolicyConfig(new MergePolicyConfig(

        PutIfAbsentMergePolicy.class.getName(), 100));
```

### Event Store Size[​](#event-store-size "Direct link to Event Store Size")

Limit events per instance:

application.yml

```
spring:

  boot:

    admin:

      event-store:

        max-log-size-per-aggregate: 500
```

This setting applies to the auto-configured Hazelcast and in-memory event stores. A custom `InstanceEventStore` bean must apply its own retention limit.

## High Availability Setup[​](#high-availability-setup "Direct link to High Availability Setup")

### Load Balancer Configuration[​](#load-balancer-configuration "Direct link to Load Balancer Configuration")

```
upstream spring_boot_admin {

    least_conn;

    server admin1:8080;

    server admin2:8080;

    server admin3:8080;

}



server {

    listen 80;

    server_name admin.example.com;



    location / {

        proxy_pass http://spring_boot_admin;

        proxy_set_header Host $host;

        proxy_set_header X-Real-IP $remote_addr;

        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        proxy_set_header X-Forwarded-Proto $scheme;

    }

}
```

### Session Persistence[​](#session-persistence "Direct link to Session Persistence")

Use sticky sessions for the UI:

```
upstream spring_boot_admin {

    ip_hash;  # Sticky sessions

    server admin1:8080;

    server admin2:8080;

}
```

Or use Spring Session:

```
<dependency>

    <groupId>org.springframework.session</groupId>

    <artifactId>spring-session-hazelcast</artifactId>

</dependency>
```

```
@EnableHazelcastHttpSession

@Configuration

public class SessionConfig {

    // Hazelcast will be used for session storage

}
```

## Docker Compose Example[​](#docker-compose-example "Direct link to Docker Compose Example")

docker-compose.yml

```
version: '3'



services:

  admin1:

    build: ./admin-server

    ports:

      - "8080:8080"

    environment:

      - SERVER_PORT=8080



  admin2:

    build: ./admin-server

    ports:

      - "8081:8080"

    environment:

      - SERVER_PORT=8080



  admin3:

    build: ./admin-server

    ports:

      - "8082:8080"

    environment:

      - SERVER_PORT=8080



  nginx:

    image: nginx:alpine

    ports:

      - "80:80"

    volumes:

      - ./nginx.conf:/etc/nginx/nginx.conf

    depends_on:

      - admin1

      - admin2

      - admin3
```

This Compose snippet only starts the services; it does not configure Hazelcast discovery. `HAZELCAST_MEMBERS` is not read by Spring Boot Admin or Hazelcast by default. Configure discovery in the Hazelcast `Config` bean or an external Hazelcast configuration file, and make that configuration available to every container. Member addresses must be reachable on the Compose network.

## Monitoring Hazelcast[​](#monitoring-hazelcast "Direct link to Monitoring Hazelcast")

### Management Center[​](#management-center "Direct link to Management Center")

Use Hazelcast Management Center:

Configure and connect Management Center using Hazelcast's own settings for the version in use; the `hazelcast.management-center.*` keys are not Spring Boot properties.

### JMX Monitoring[​](#jmx-monitoring "Direct link to JMX Monitoring")

Enable JMX:

```
config.setProperty("hazelcast.jmx", "true");
```

### Health Checks[​](#health-checks "Direct link to Health Checks")

Check cluster health:

```
@Component

public class HazelcastHealthCheck {



    private final HazelcastInstance hazelcastInstance;



    public HazelcastHealthCheck(HazelcastInstance hazelcastInstance) {

        this.hazelcastInstance = hazelcastInstance;

    }



    public boolean isHealthy() {

        return hazelcastInstance.getCluster().getMembers().size() > 0;

    }



    public int getClusterSize() {

        return hazelcastInstance.getCluster().getMembers().size();

    }

}
```

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Split Brain[​](#split-brain "Direct link to Split Brain")

Configure merge policy:

```
mapConfig.setMergePolicyConfig(new MergePolicyConfig(

    PutIfAbsentMergePolicy.class.getName(), 100));
```

### Members Not Joining[​](#members-not-joining "Direct link to Members Not Joining")

1. **Check network connectivity**:

   ```
   telnet admin1 5701
   ```

2. **Verify multicast**:

   ```
   # Check if multicast is enabled

   ip maddr show
   ```

3. **Check logs**:

   ```
   Hazelcast logs will show connection attempts
   ```

### Performance Issues[​](#performance-issues "Direct link to Performance Issues")

1. **Increase backup count**:

   ```
   mapConfig.setBackupCount(2);
   ```

2. **Use async backups**:

   ```
   mapConfig.setAsyncBackupCount(1);
   ```

3. **Monitor map size**:

   ```
   IMap map = hazelcastInstance.getMap("spring-boot-admin-event-store");

   log.info("Map size: {}", map.size());
   ```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Use TCP/IP in Production**: Multicast may not work in cloud environments

2. **Configure Appropriate Backups**:

   ```
   mapConfig.setBackupCount(1);  // At least 1 backup
   ```

3. **Set Event Store Limits**:

   ```
   new HazelcastEventStore(500, map);  // Reasonable limit
   ```

4. **Monitor Cluster Health**: Use Management Center or JMX

5. **Use Load Balancer**: Distribute traffic across servers

6. **Enable Session Persistence**: For seamless failover

7. **Configure Network Properly**: Especially in Kubernetes/Docker

## Complete Example[​](#complete-example "Direct link to Complete Example")

See the [spring-boot-admin-sample-hazelcast](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-hazelcast/) project for a complete working example.

## See Also[​](#see-also "Direct link to See Also")

* [Clustering](/4.1.4-SNAPSHOT/docs/server/Clustering.md) - Clustering overview
* [Persistence](/4.1.4-SNAPSHOT/docs/server/persistence.md) - Event store details
* [Hazelcast Sample](/4.1.4-SNAPSHOT/docs/samples/sample-hazelcast.md) - Detailed sample walkthrough
