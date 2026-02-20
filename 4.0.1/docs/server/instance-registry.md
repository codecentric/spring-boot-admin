# Instance Registry

The Instance Registry is the core component responsible for managing registered applications in Spring Boot Admin. It uses an event-sourced architecture to track application state through the `InstanceRepository` interface.

## InstanceRepository[​](#instancerepository "Direct link to InstanceRepository")

The `InstanceRepository` is the primary interface for storing and retrieving application instances. It provides reactive methods for managing instance lifecycle:

```
public interface InstanceRepository {

    Mono<Instance> save(Instance app);

    Flux<Instance> findAll();

    Mono<Instance> find(InstanceId id);

    Flux<Instance> findByName(String name);

    Mono<Instance> compute(InstanceId id,
        BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);

    Mono<Instance> computeIfPresent(InstanceId id,
        BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);
}
```

## Event-Sourced Implementation[​](#event-sourced-implementation "Direct link to Event-Sourced Implementation")

Spring Boot Admin uses `EventsourcingInstanceRepository`, which rebuilds instance state from events stored in the `InstanceEventStore`.

### How It Works[​](#how-it-works "Direct link to How It Works")

Instead of directly storing instance state, the repository stores events that represent state changes:

1. **Registration**: When an application registers, an `InstanceRegisteredEvent` is created
2. **State Changes**: Each state change (health, info, endpoints) generates a new event
3. **Reconstruction**: The current instance state is rebuilt by replaying all events

```
public class EventsourcingInstanceRepository implements InstanceRepository {

    private final InstanceEventStore eventStore;

    @Override
    public Mono<Instance> save(Instance instance) {
        return eventStore.append(instance.getUnsavedEvents())
                        .then(Mono.just(instance.clearUnsavedEvents()));
    }

    @Override
    public Mono<Instance> find(InstanceId id) {
        return eventStore.find(id)
                        .collectList()
                        .filter(e -> !e.isEmpty())
                        .map(events -> Instance.create(id).apply(events));
    }

    @Override
    public Flux<Instance> findAll() {
        return eventStore.findAll()
                        .groupBy(InstanceEvent::getInstance)
                        .flatMap(f -> f.reduce(Instance.create(f.key()),
                                              Instance::apply));
    }
}
```

### Benefits of Event Sourcing[​](#benefits-of-event-sourcing "Direct link to Benefits of Event Sourcing")

* **Complete Audit Trail**: Every change is recorded as an event
* **Temporal Queries**: Can reconstruct state at any point in time
* **Event Replay**: Can rebuild state from events after crashes
* **Debugging**: Full history of state changes for troubleshooting

## Instance Lifecycle[​](#instance-lifecycle "Direct link to Instance Lifecycle")

### 1. Registration[​](#1-registration "Direct link to 1. Registration")

When an application registers, a new instance is created:

```
InstanceId id = idGenerator.generateId(registration);
Instance newInstance = Instance.create(id).register(registration);
repository.save(newInstance);
```

This generates an `InstanceRegisteredEvent`.

### 2. Endpoint Detection[​](#2-endpoint-detection "Direct link to 2. Endpoint Detection")

After registration, the server detects available actuator endpoints:

```
instance = instance.withEndpoints(detectedEndpoints);
repository.save(instance);
```

This generates an `InstanceEndpointsDetectedEvent`.

### 3. Status Updates[​](#3-status-updates "Direct link to 3. Status Updates")

The server periodically polls health endpoints:

```
instance = instance.withStatusInfo(statusInfo);
repository.save(instance);
```

This generates an `InstanceStatusChangedEvent` when status changes.

### 4. Info Updates[​](#4-info-updates "Direct link to 4. Info Updates")

Application info is periodically refreshed:

```
instance = instance.withInfo(info);
repository.save(instance);
```

This generates an `InstanceInfoChangedEvent` when info changes.

### 5. Deregistration[​](#5-deregistration "Direct link to 5. Deregistration")

When an application shuts down or is removed:

```
instance = instance.deregister();
repository.save(instance);
```

This generates an `InstanceDeregisteredEvent`.

## Optimistic Locking[​](#optimistic-locking "Direct link to Optimistic Locking")

The repository uses optimistic locking to handle concurrent updates:

```
private final Retry retryOptimisticLockException = Retry.max(10)
    .doBeforeRetry(s -> log.debug("Retrying after OptimisticLockingException",
                                  s.failure()))
    .filter(OptimisticLockingException.class::isInstance);

@Override
public Mono<Instance> compute(InstanceId id,
        BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction) {
    return find(id)
        .flatMap(app -> remappingFunction.apply(id, app))
        .switchIfEmpty(Mono.defer(() -> remappingFunction.apply(id, null)))
        .flatMap(this::save)
        .retryWhen(retryOptimisticLockException);
}
```

If two updates conflict (based on event version numbers), the operation is automatically retried up to 10 times.

## Querying Instances[​](#querying-instances "Direct link to Querying Instances")

### Find All Instances[​](#find-all-instances "Direct link to Find All Instances")

```
Flux<Instance> instances = repository.findAll();
instances.subscribe(instance -> {
    System.out.println("Instance: " + instance.getRegistration().getName());
});
```

### Find by Instance ID[​](#find-by-instance-id "Direct link to Find by Instance ID")

```
Mono<Instance> instance = repository.find(instanceId);
instance.subscribe(inst -> {
    System.out.println("Found: " + inst.getRegistration().getName());
});
```

### Find by Application Name[​](#find-by-application-name "Direct link to Find by Application Name")

```
Flux<Instance> instances = repository.findByName("my-application");
instances.subscribe(instance -> {
    System.out.println("Instance ID: " + instance.getId());
});
```

## Compute Operations[​](#compute-operations "Direct link to Compute Operations")

The `compute` methods provide atomic read-modify-write operations:

### compute()[​](#compute "Direct link to compute()")

Updates an instance or creates it if it doesn't exist:

```
repository.compute(instanceId, (id, instance) -> {
    if (instance == null) {
        // Create new instance
        return Mono.just(Instance.create(id).register(registration));
    } else {
        // Update existing instance
        return Mono.just(instance.withStatusInfo(newStatus));
    }
}).subscribe();
```

### computeIfPresent()[​](#computeifpresent "Direct link to computeIfPresent()")

Updates only if the instance exists:

```
repository.computeIfPresent(instanceId, (id, instance) -> {
    return Mono.just(instance.withInfo(updatedInfo));
}).subscribe();
```

## Instance State[​](#instance-state "Direct link to Instance State")

An `Instance` object contains:

```
public class Instance {
    private final InstanceId id;
    private final long version;
    private final Registration registration;
    private final boolean registered;
    private final StatusInfo statusInfo;
    private final Info info;
    private final Endpoints endpoints;
    private final BuildVersion buildVersion;
    private final Tags tags;
    private final List<InstanceEvent> unsavedEvents;
}
```

### Key Properties[​](#key-properties "Direct link to Key Properties")

* **`id`**: Unique identifier for the instance
* **`version`**: Event version for optimistic locking
* **`registration`**: Registration details (name, URL, metadata)
* **`registered`**: Whether the instance is currently registered
* **`statusInfo`**: Current health status
* **`info`**: Application info from `/actuator/info`
* **`endpoints`**: Discovered actuator endpoints
* **`buildVersion`**: Application version from build-info
* **`tags`**: Custom tags for classification
* **`unsavedEvents`**: Events pending persistence

## Instance ID Generation[​](#instance-id-generation "Direct link to Instance ID Generation")

Instance IDs are generated by `InstanceIdGenerator` implementations:

### Default: HashingInstanceUrlIdGenerator[​](#default-hashinginstanceurlidgenerator "Direct link to Default: HashingInstanceUrlIdGenerator")

Generates stable IDs based on the service URL:

```
public class HashingInstanceUrlIdGenerator implements InstanceIdGenerator {
    @Override
    public InstanceId generateId(Registration registration) {
        String serviceUrl = registration.getServiceUrl();
        // Generate hash-based ID from URL
        return InstanceId.of(hash(serviceUrl));
    }
}
```

### Cloud Foundry: CloudFoundryInstanceIdGenerator[​](#cloud-foundry-cloudfoundryinstanceidgenerator "Direct link to Cloud Foundry: CloudFoundryInstanceIdGenerator")

Uses Cloud Foundry's application instance ID:

```
public class CloudFoundryInstanceIdGenerator implements InstanceIdGenerator {
    @Override
    public InstanceId generateId(Registration registration) {
        String cfInstanceId = registration.getMetadata()
                                         .get("applicationId")
            + ":" + registration.getMetadata().get("instanceId");
        return InstanceId.of(cfInstanceId);
    }
}
```

### Custom ID Generator[​](#custom-id-generator "Direct link to Custom ID Generator")

Implement your own ID generation strategy:

```
@Component
public class CustomInstanceIdGenerator implements InstanceIdGenerator {

    @Override
    public InstanceId generateId(Registration registration) {
        // Custom logic to generate instance ID
        String customId = registration.getName()
            + "-" + UUID.randomUUID().toString();
        return InstanceId.of(customId);
    }
}
```

## Working with the Repository[​](#working-with-the-repository "Direct link to Working with the Repository")

### Injecting the Repository[​](#injecting-the-repository "Direct link to Injecting the Repository")

```
@Component
public class InstanceManager {

    private final InstanceRepository repository;

    public InstanceManager(InstanceRepository repository) {
        this.repository = repository;
    }

    public Flux<String> getApplicationNames() {
        return repository.findAll()
                        .filter(Instance::isRegistered)
                        .map(i -> i.getRegistration().getName())
                        .distinct();
    }
}
```

### Reacting to Changes[​](#reacting-to-changes "Direct link to Reacting to Changes")

Subscribe to the event store to react to instance changes:

```
@Component
public class InstanceChangeListener {

    public InstanceChangeListener(InstanceEventStore eventStore,
                                  InstanceRepository repository) {
        eventStore.subscribe(event -> {
            if (event instanceof InstanceStatusChangedEvent statusEvent) {
                repository.find(event.getInstance())
                         .subscribe(instance -> {
                             log.info("Instance {} status: {}",
                                     instance.getRegistration().getName(),
                                     instance.getStatusInfo().getStatus());
                         });
            }
        });
    }
}
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Use compute methods** for atomic updates to avoid race conditions
2. **Don't modify Instance objects directly** - use the builder-style methods (withXxx)
3. **Let the system retry** optimistic locking failures automatically
4. **Subscribe to events** for reactive processing instead of polling
5. **Use findByName** for multi-instance applications to find all instances of a service

## See Also[​](#see-also "Direct link to See Also")

* [Persistence](/4.0.1/docs/server/persistence.md) - Learn about event storage
* [Events](/4.0.1/docs/server/Events.md) - Understand the event system
* [Clustering](/4.0.1/docs/server/Clustering.md) - Distributed instance management
