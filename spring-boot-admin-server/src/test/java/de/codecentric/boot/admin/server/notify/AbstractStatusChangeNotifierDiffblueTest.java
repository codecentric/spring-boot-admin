package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class AbstractStatusChangeNotifierDiffblueTest {

	/**
	 * Test {@link AbstractStatusChangeNotifier#notify(InstanceEvent)} with
	 * {@code InstanceEvent}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#notify(InstanceEvent)}
	 */
	@Test
	public void testNotifyWithInstanceEvent() throws AssertionError {
		// Arrange
		LoggingNotifier loggingNotifier = new LoggingNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(loggingNotifier.notify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#notify(InstanceEvent)} with
	 * {@code InstanceEvent}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#notify(InstanceEvent)}
	 */
	@Test
	public void testNotifyWithInstanceEvent2() throws AssertionError {
		// Arrange
		DingTalkNotifier dingTalkNotifier = new DingTalkNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		dingTalkNotifier.setEnabled(false);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(dingTalkNotifier.notify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#notify(InstanceEvent)} with
	 * {@code InstanceEvent}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#notify(InstanceEvent)}
	 */
	@Test
	public void testNotifyWithInstanceEvent3() throws AssertionError {
		// Arrange
		LoggingNotifier loggingNotifier = new LoggingNotifier(
				new SnapshottingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(loggingNotifier.notify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#shouldNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AbstractStatusChangeNotifier#shouldNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testShouldNotify_whenNull_thenReturnFalse() {
		// Arrange, Act and Assert
		assertFalse(new LoggingNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()))
			.shouldNotify(null, null));
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#getLastStatus(InstanceId)}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#getLastStatus(InstanceId)}
	 */
	@Test
	public void testGetLastStatus() {
		// Arrange
		LoggingNotifier loggingNotifier = new LoggingNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act and Assert
		assertEquals("UNKNOWN", loggingNotifier.getLastStatus(InstanceId.of("42")));
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#setIgnoreChanges(String[])}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#setIgnoreChanges(String[])}
	 */
	@Test
	public void testSetIgnoreChanges() {
		// Arrange
		LoggingNotifier loggingNotifier = new LoggingNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()));

		// Act
		loggingNotifier.setIgnoreChanges(new String[] { "Ignore Changes" });

		// Assert
		assertArrayEquals(new String[] { "Ignore Changes" }, loggingNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link AbstractStatusChangeNotifier#getIgnoreChanges()}.
	 * <p>
	 * Method under test: {@link AbstractStatusChangeNotifier#getIgnoreChanges()}
	 */
	@Test
	public void testGetIgnoreChanges() {
		// Arrange, Act and Assert
		assertArrayEquals(new String[] { "UNKNOWN:UP" },
				new LoggingNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore())).getIgnoreChanges());
	}

}
