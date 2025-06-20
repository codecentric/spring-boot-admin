package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import org.junit.Test;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class LoggingNotifierDiffblueTest {

	/**
	 * Test {@link LoggingNotifier#LoggingNotifier(InstanceRepository)}.
	 * <p>
	 * Method under test: {@link LoggingNotifier#LoggingNotifier(InstanceRepository)}
	 */
	@Test
	public void testNewLoggingNotifier() {
		// Arrange and Act
		LoggingNotifier actualLoggingNotifier = new LoggingNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Assert
		assertTrue(actualLoggingNotifier.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualLoggingNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link LoggingNotifier#doNotify(InstanceEvent, Instance)}.
	 * <p>
	 * Method under test: {@link LoggingNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify() throws AssertionError {
		// Arrange
		LoggingNotifier loggingNotifier = new LoggingNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(loggingNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

}
