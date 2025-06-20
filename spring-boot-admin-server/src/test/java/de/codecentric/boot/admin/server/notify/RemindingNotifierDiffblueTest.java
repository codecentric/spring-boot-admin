package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.util.ArrayList;
import java.util.function.Function;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.server.reactive.ChannelSendOperator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class RemindingNotifierDiffblueTest {

	/**
	 * Test {@link RemindingNotifier#RemindingNotifier(Notifier, InstanceRepository)}.
	 * <ul>
	 * <li>When {@link Notifier}.</li>
	 * <li>Then return Enabled.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link RemindingNotifier#RemindingNotifier(Notifier, InstanceRepository)}
	 */
	@Test
	public void testNewRemindingNotifier_whenNotifier_thenReturnEnabled() {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act and Assert
		assertTrue(new RemindingNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore()))
			.isEnabled());
	}

	/**
	 * Test {@link RemindingNotifier#doNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then calls {@link Notifier#notify(InstanceEvent)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link RemindingNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify_thenCallsNotify() throws AssertionError {
		// Arrange
		Notifier delegate = mock(Notifier.class);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		when(delegate.notify(Mockito.<InstanceEvent>any()))
			.thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
		RemindingNotifier remindingNotifier = new RemindingNotifier(delegate,
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(remindingNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectComplete().verify();
		verify(delegate).notify(isA(InstanceEvent.class));
	}

	/**
	 * Test {@link RemindingNotifier#sendReminders()}.
	 * <p>
	 * Method under test: {@link RemindingNotifier#sendReminders()}
	 */
	@Test
	public void testSendReminders() throws AssertionError {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(new RemindingNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore()))
				.sendReminders());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link RemindingNotifier#shouldStartReminder(InstanceEvent)}.
	 * <ul>
	 * <li>When {@link InstanceId} with value is {@code 42}.</li>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link RemindingNotifier#shouldStartReminder(InstanceEvent)}
	 */
	@Test
	public void testShouldStartReminder_whenInstanceIdWithValueIs42_thenReturnFalse() {
		// Arrange
		Notifier delegate = mock(Notifier.class);
		RemindingNotifier remindingNotifier = new RemindingNotifier(delegate,
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		assertFalse(remindingNotifier.shouldStartReminder(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
	}

	/**
	 * Test {@link RemindingNotifier#shouldEndReminder(InstanceEvent)}.
	 * <ul>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link RemindingNotifier#shouldEndReminder(InstanceEvent)}
	 */
	@Test
	public void testShouldEndReminder_thenReturnFalse() {
		// Arrange
		Notifier delegate = mock(Notifier.class);
		RemindingNotifier remindingNotifier = new RemindingNotifier(delegate,
				new EventsourcingInstanceRepository(new InMemoryEventStore()));
		InstanceId instance = InstanceId.of("42");

		// Act and Assert
		assertFalse(remindingNotifier
			.shouldEndReminder(new InstanceEndpointsDetectedEvent(instance, 1L, Endpoints.empty())));
	}

	/**
	 * Test {@link RemindingNotifier#shouldEndReminder(InstanceEvent)}.
	 * <ul>
	 * <li>Then return {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link RemindingNotifier#shouldEndReminder(InstanceEvent)}
	 */
	@Test
	public void testShouldEndReminder_thenReturnTrue() {
		// Arrange
		Notifier delegate = mock(Notifier.class);
		RemindingNotifier remindingNotifier = new RemindingNotifier(delegate,
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		assertTrue(remindingNotifier.shouldEndReminder(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
	}

}
