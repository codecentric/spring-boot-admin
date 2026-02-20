# Creating Custom Notifiers

Spring Boot Admin makes it easy to create custom notifiers to integrate with your preferred notification channels. You can extend the built-in notifier base classes or implement the `Notifier` interface directly.

## Overview[​](#overview "Direct link to Overview")

Notifiers are Spring beans that implement the `Notifier` interface and react to instance events such as status changes, registration, or deregistration.

## Using AbstractEventNotifier[​](#using-abstracteventnotifier "Direct link to Using AbstractEventNotifier")

The recommended approach is to extend `AbstractEventNotifier`, which provides built-in support for:

* Filtering events
* Enabling/disabling notifications
* Accessing instance details
* Error handling

### Basic Example[​](#basic-example "Direct link to Basic Example")

```
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CustomNotifier extends AbstractEventNotifier {

	private static final Logger log = LoggerFactory.getLogger(CustomNotifier.class);

	public CustomNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono.fromRunnable(() -> {
			if (event instanceof InstanceStatusChangedEvent statusEvent) {
				log.info("Instance {} ({}) is {}",
						instance.getRegistration().getName(),
						event.getInstance(),
						statusEvent.getStatusInfo().getStatus());
			} else {
				log.info("Instance {} ({}) {}",
						instance.getRegistration().getName(),
						event.getInstance(),
						event.getType());
			}
		});
	}
}
```

### Registering the Notifier[​](#registering-the-notifier "Direct link to Registering the Notifier")

Register your custom notifier as a Spring bean:

```
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifierConfiguration {

	@Bean
	public CustomNotifier customNotifier(InstanceRepository repository) {
		return new CustomNotifier(repository);
	}
}
```

## Advanced Custom Notifier[​](#advanced-custom-notifier "Direct link to Advanced Custom Notifier")

Here's a more advanced example that sends notifications to an external API:

```
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class WebhookNotifier extends AbstractEventNotifier {

	private static final Logger log = LoggerFactory.getLogger(WebhookNotifier.class);

	private final WebClient webClient;
	private final String webhookUrl;

	public WebhookNotifier(InstanceRepository repository,
						   WebClient.Builder webClientBuilder,
						   String webhookUrl) {
		super(repository);
		this.webhookUrl = webhookUrl;
		this.webClient = webClientBuilder.build();
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono.fromSupplier(() -> createNotificationPayload(event, instance))
				.flatMap(this::sendWebhookNotification)
				.doOnError(ex -> log.error("Failed to send webhook notification", ex))
				.then();
	}

	private NotificationPayload createNotificationPayload(InstanceEvent event,
														  Instance instance) {
		return NotificationPayload.builder()
				.instanceId(instance.getId().getValue())
				.instanceName(instance.getRegistration().getName())
				.eventType(event.getType())
				.status(instance.getStatusInfo().getStatus())
				.timestamp(event.getTimestamp())
				.serviceUrl(instance.getRegistration().getServiceUrl())
				.build();
	}

	private Mono<Void> sendWebhookNotification(NotificationPayload payload) {
		return webClient.post()
				.uri(webhookUrl)
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(Void.class)
				.doOnSuccess(v -> log.info("Webhook notification sent successfully"))
				.onErrorResume(ex -> {
					log.error("Webhook call failed: {}", ex.getMessage());
					return Mono.empty();
				});
	}

	@lombok.Data
	@lombok.Builder
	private static class NotificationPayload {
		private String instanceId;
		private String instanceName;
		private String eventType;
		private String status;
		private long timestamp;
		private String serviceUrl;
	}
}
```

### Configuration[​](#configuration "Direct link to Configuration")

```

@Configuration
public class WebhookNotifierConfiguration {

	@Bean
	public WebhookNotifier webhookNotifier(InstanceRepository repository,
										   WebClient.Builder webClientBuilder,
										   @Value("${webhook.url}") String webhookUrl) {
		return new WebhookNotifier(repository, webClientBuilder, webhookUrl);
	}
}
```

application.yml

```
webhook:
  url: https://your-webhook-endpoint.com/notifications
```

## Filtering Events[​](#filtering-events "Direct link to Filtering Events")

You can override `shouldNotify` to filter which events trigger notifications:

```
public class FilteredNotifier extends AbstractEventNotifier {

	public FilteredNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected boolean shouldNotify(InstanceEvent event, Instance instance) {
		// Only notify for production instances
		String environment = instance.getRegistration()
				.getMetadata()
				.get("environment");
		return "production".equals(environment);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		// Send notification
		return Mono.fromRunnable(() -> {
			log.info("Production instance event: {}", event.getType());
		});
	}
}
```

## Using AbstractStatusChangeNotifier[​](#using-abstractstatuschangenotifier "Direct link to Using AbstractStatusChangeNotifier")

If you only care about status changes (UP/DOWN/OFFLINE), extend `AbstractStatusChangeNotifier`:

```
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;

import reactor.core.publisher.Mono;

public class StatusChangeNotifier extends AbstractStatusChangeNotifier {

	public StatusChangeNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		StatusInfo statusInfo = instance.getStatusInfo();
		String status = statusInfo.getStatus();

		return Mono.fromRunnable(() -> {
			if ("DOWN".equals(status)) {
				// Send critical alert
				log.error("CRITICAL: Instance {} is DOWN!",
						instance.getRegistration().getName());
			} else if ("UP".equals(status)) {
				// Send recovery notification
				log.info("Instance {} is back UP",
						instance.getRegistration().getName());
			}
		});
	}
}
```

## Implementing Notifier Interface Directly[​](#implementing-notifier-interface-directly "Direct link to Implementing Notifier Interface Directly")

For full control, implement the `Notifier` interface:

```
import de.codecentric.boot.admin.server.notify.Notifier;

import reactor.core.publisher.Mono;

public class DirectNotifier implements Notifier {

	private final InstanceRepository repository;
	private boolean enabled = true;

	public DirectNotifier(InstanceRepository repository) {
		this.repository = repository;
	}

	@Override
	public Mono<Void> notify(InstanceEvent event) {
		if (!enabled) {
			return Mono.empty();
		}

		return repository.find(event.getInstance())
				.flatMap(instance -> processNotification(event, instance))
				.then();
	}

	private Mono<Void> processNotification(InstanceEvent event, Instance instance) {
		// Custom notification logic
		return Mono.fromRunnable(() -> {
			// Send notification
		});
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
```

## Configuration Properties[​](#configuration-properties "Direct link to Configuration Properties")

Make your notifier configurable through application properties:

```
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.boot.admin.notify.custom")
public class CustomNotifierProperties {
	private final boolean enabled = true;
	private String apiUrl;
	private String apiKey;
	private final int timeout = 5000;

	// Getters and setters
}
```

```

@Configuration
@EnableConfigurationProperties(CustomNotifierProperties.class)
public class CustomNotifierConfiguration {

	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.custom",
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true)
	public CustomNotifier customNotifier(InstanceRepository repository,
										 CustomNotifierProperties properties) {
		CustomNotifier notifier = new CustomNotifier(repository);
		notifier.setApiUrl(properties.getApiUrl());
		notifier.setApiKey(properties.getApiKey());
		notifier.setTimeout(properties.getTimeout());
		return notifier;
	}
}
```

application.yml

```
spring:
  boot:
    admin:
      notify:
        custom:
          enabled: true
          api-url: https://api.example.com/notifications
          api-key: ${NOTIFICATION_API_KEY}
          timeout: 10000
```

## Combining with FilteringNotifier[​](#combining-with-filteringnotifier "Direct link to Combining with FilteringNotifier")

Use `FilteringNotifier` to allow runtime control:

```

@Configuration
public class NotifierConfig {

	@Bean
	public FilteringNotifier filteringNotifier(InstanceRepository repository,
											   ObjectProvider<List<Notifier>> otherNotifiers) {
		CompositeNotifier delegate = new CompositeNotifier(
				otherNotifiers.getIfAvailable(Collections::emptyList));
		return new FilteringNotifier(delegate, repository);
	}

	@Primary
	@Bean(initMethod = "start", destroyMethod = "stop")
	public RemindingNotifier remindingNotifier(FilteringNotifier filteringNotifier,
											   InstanceRepository repository) {
		RemindingNotifier notifier = new RemindingNotifier(
				filteringNotifier, repository);
		notifier.setReminderPeriod(Duration.ofMinutes(10));
		notifier.setCheckReminderInverval(Duration.ofSeconds(10));
		return notifier;
	}

	@Bean
	public CustomNotifier customNotifier(InstanceRepository repository) {
		return new CustomNotifier(repository);
	}
}
```

## Testing Custom Notifiers[​](#testing-custom-notifiers "Direct link to Testing Custom Notifiers")

```
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

public class CustomNotifierTest {

	@Test
	public void testNotification() {
		InstanceRepository repository = Mockito.mock(InstanceRepository.class);
		CustomNotifier notifier = new CustomNotifier(repository);

		Instance instance = Instance.create(InstanceId.of("test-instance"))
				.register(Registration.create("test-app", "http://localhost:8080")
						.build());

		InstanceEvent event = new InstanceStatusChangedEvent(
				instance.getId(),
				instance.getVersion(),
				StatusInfo.ofUp()
		);

		Mockito.when(repository.find(instance.getId()))
				.thenReturn(Mono.just(instance));

		StepVerifier.create(notifier.notify(event))
				.verifyComplete();
	}
}
```

## Best Practices[​](#best-practices "Direct link to Best Practices")

1. **Extend AbstractEventNotifier** for most use cases - it provides essential features
2. **Handle errors gracefully** - don't let notification failures affect the server
3. **Use reactive programming** - return `Mono<Void>` for async operations
4. **Make it configurable** - use `@ConfigurationProperties` for flexibility
5. **Filter appropriately** - override `shouldNotify` to reduce noise
6. **Log failures** - always log when notifications fail for debugging
7. **Use WebClient** for HTTP calls - it's reactive and efficient
8. **Consider rate limiting** - prevent notification storms
9. **Test thoroughly** - ensure your notifier handles all event types
10. **Document configuration** - provide clear examples for users

## See Also[​](#see-also "Direct link to See Also")

* [Notifications Overview](/4.0.1/docs/server/notifications/.md) - Learn about the notification system
* [Filtering Notifications](/4.0.1/docs/server/notifications/.md#filtering-notifications) - Control which notifications are sent
* [Notification Reminders](/4.0.1/docs/server/notifications/.md#notification-reminder) - Set up reminder notifications
* [Events](/4.0.1/docs/server/Events.md) - Understand instance events
