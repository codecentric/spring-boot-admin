# Persistence and Event Store

Spring Boot Admin uses an event-sourced architecture to track the state of registered applications. All changes to application instances are stored as events in an `InstanceEventStore`, allowing the server to rebuild application state and maintain a complete audit trail.

## Event Store Architecture[​](#event-store-architecture "Direct link to Event Store Architecture")

The `InstanceEventStore` is responsible for storing all instance-related events. Spring Boot Admin provides two built-in implementations:

### InMemoryEventStore[​](#inmemoryeventstore "Direct link to InMemoryEventStore")

The default implementation stores events in memory using a `ConcurrentHashMap`. This is suitable for single-instance deployments and development environments.

**Characteristics:**

* Fast and lightweight
* Non-persistent (data lost on restart)
* Limited by available memory
* Configurable maximum log size per instance

**Configuration:**

```
@Bean
public InstanceEventStore eventStore() {
    return new InMemoryEventStore(100); // Max 100 events per instance
}
```

The default configuration creates an `InMemoryEventStore` with a maximum of 100 events per instance aggregate. Older events are automatically removed when the limit is reached.

### HazelcastEventStore[​](#hazelcasteventstore "Direct link to HazelcastEventStore")

For clustered deployments, the `HazelcastEventStore` provides distributed persistence using Hazelcast's `IMap`.

**Characteristics:**

* Distributed across cluster nodes
* Survives single-node failures
* Automatic synchronization between nodes
* Real-time event publishing across the cluster

**Configuration:**

First, add the Hazelcast dependency:

pom.xml

```
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
</dependency>
```

Then configure the Hazelcast-backed event store:

```
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;

@Configuration
public class HazelcastConfig {

    @Bean
    public Config hazelcastConfig() {
        MapConfig mapConfig = new MapConfig("spring-boot-admin-event-store")
            .setBackupCount(1)
            .setMergePolicyConfig(new MergePolicyConfig(
                PutIfAbsentMergePolicy.class.getName(), 100));

        Config config = new Config();
        config.addMapConfig(mapConfig);
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    @Bean
    public InstanceEventStore eventStore(HazelcastInstance hazelcastInstance) {
        IMap<InstanceId, List<InstanceEvent>> map =
            hazelcastInstance.getMap("spring-boot-admin-event-store");
        return new HazelcastEventStore(100, map);
    }
}
```

**How it works:**

The `HazelcastEventStore` listens to map entry updates and publishes new events to all cluster nodes:

```
eventLog.addEntryListener(new EntryAdapter<InstanceId, List<InstanceEvent>>() {
    @Override
    public void entryUpdated(EntryEvent<InstanceId, List<InstanceEvent>> event) {
        long lastKnownVersion = getLastVersion(event.getOldValue());
        List<InstanceEvent> newEvents = event.getValue()
            .stream()
            .filter((e) -> e.getVersion() > lastKnownVersion)
            .toList();
        publish(newEvents);
    }
}, true);
```

## Event Types[​](#event-types "Direct link to Event Types")

The event store manages different types of instance events:

* `InstanceRegisteredEvent` - Application registers with the server
* `InstanceDeregisteredEvent` - Application unregisters or is removed
* `InstanceStatusChangedEvent` - Health status changes
* `InstanceEndpointsDetectedEvent` - Actuator endpoints discovered
* `InstanceInfoChangedEvent` - Application info updated
* `InstanceRegistrationUpdatedEvent` - Registration details changed

Each event contains:

* Instance ID
* Timestamp
* Version (for optimistic locking)
* Event-specific data

## InstanceEventStore Interface[​](#instanceeventstore-interface "Direct link to InstanceEventStore Interface")

```
public interface InstanceEventStore extends Publisher<InstanceEvent> {

    Flux<InstanceEvent> findAll();

    Flux<InstanceEvent> find(InstanceId id);

    Mono<Void> append(List<InstanceEvent> events);
}
```

### Methods[​](#methods "Direct link to Methods")

* **`findAll()`** - Returns all events for all instances
* **`find(InstanceId id)`** - Returns events for a specific instance
* **`append(List<InstanceEvent> events)`** - Appends new events to the store

The store also implements `Publisher<InstanceEvent>`, allowing components to subscribe to new events in real-time.

## Event Versioning and Optimistic Locking[​](#event-versioning-and-optimistic-locking "Direct link to Event Versioning and Optimistic Locking")

Events are versioned to prevent concurrent modification issues. Each event includes a version number that increments with each change:

```
public abstract class InstanceEvent implements Serializable {
    private final InstanceId instance;
    private final long version;
    private final long timestamp;

    // ...
}
```

When appending events, the event store checks that the version matches the expected sequence, throwing an `OptimisticLockingException` if there's a conflict.

## Event Publishing[​](#event-publishing "Direct link to Event Publishing")

The event store publishes events to subscribers, enabling reactive processing:

```
eventStore.subscribe(event -> {
    if (event instanceof InstanceStatusChangedEvent statusEvent) {
        // React to status changes
        System.out.println("Instance " + event.getInstance() +
                          " changed to " + statusEvent.getStatusInfo().getStatus());
    }
});
```

## Configuring Event Store Size[​](#configuring-event-store-size "Direct link to Configuring Event Store Size")

Control the maximum number of events stored per instance:

```
@Bean
public InstanceEventStore eventStore() {
    return new InMemoryEventStore(500); // Store up to 500 events per instance
}
```

When the limit is reached, the oldest events are removed. This prevents unbounded memory growth while maintaining recent history.

## Custom Event Store Implementation[​](#custom-event-store-implementation "Direct link to Custom Event Store Implementation")

You can implement your own event store for custom persistence requirements (e.g., database, external cache):

```
public class CustomEventStore implements InstanceEventStore {

    @Override
    public Flux<InstanceEvent> findAll() {
        // Load all events from your storage
    }

    @Override
    public Flux<InstanceEvent> find(InstanceId id) {
        // Load events for specific instance
    }

    @Override
    public Mono<Void> append(List<InstanceEvent> events) {
        // Persist events and publish to subscribers
    }

    @Override
    public void subscribe(Subscriber<? super InstanceEvent> subscriber) {
        // Handle event subscriptions
    }
}
```

Then register your custom implementation as a bean:

```
@Bean
public InstanceEventStore eventStore() {
    return new CustomEventStore();
}
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **For Development**: Use `InMemoryEventStore` for simplicity
2. **For Single Instance Deployments**: Use `InMemoryEventStore` if restart data loss is acceptable
3. **For Clustered Deployments**: Use `HazelcastEventStore` for high availability
4. **For Large Deployments**: Tune the max log size to balance memory usage and history retention
5. **For Custom Requirements**: Implement your own event store with database or distributed cache backing

## Monitoring Event Store[​](#monitoring-event-store "Direct link to Monitoring Event Store")

Monitor event store health through actuator endpoints or by subscribing to events:

```
@Component
public class EventStoreMonitor {

    public EventStoreMonitor(InstanceEventStore eventStore) {
        eventStore.subscribe(event -> {
            // Log or metric collection
            log.debug("Event: {} for instance {}",
                     event.getType(), event.getInstance());
        });
    }
}
```

## See Also[​](#see-also "Direct link to See Also")

* [Clustering](/4.0.1/docs/server/Clustering.md) - Learn about clustering with Hazelcast
* [Events](/4.0.1/docs/server/Events.md) - Understand the event system
* [Instance Registry](/4.0.1/docs/server/instance-registry.md) - How instances are managed
