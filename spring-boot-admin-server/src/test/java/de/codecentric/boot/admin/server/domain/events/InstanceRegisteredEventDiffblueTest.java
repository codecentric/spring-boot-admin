package de.codecentric.boot.admin.server.domain.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceRegisteredEventDiffblueTest {

	/**
	 * Test
	 * {@link InstanceRegisteredEvent#InstanceRegisteredEvent(InstanceId, long, Registration)}.
	 * <ul>
	 * <li>Then return Registration is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceRegisteredEvent#InstanceRegisteredEvent(InstanceId, long, Registration)}
	 */
	@Test
	public void testNewInstanceRegisteredEvent_thenReturnRegistrationIsNull() {
		// Arrange
		InstanceId instance = InstanceId.of("42");

		// Act
		InstanceRegisteredEvent actualInstanceRegisteredEvent = new InstanceRegisteredEvent(instance, 1L, null);

		// Assert
		assertNull(actualInstanceRegisteredEvent.getRegistration());
		assertEquals(1L, actualInstanceRegisteredEvent.getVersion());
		assertEquals(InstanceRegisteredEvent.TYPE, actualInstanceRegisteredEvent.getType());
		assertSame(instance, actualInstanceRegisteredEvent.getInstance());
	}

	/**
	 * Test
	 * {@link InstanceRegisteredEvent#InstanceRegisteredEvent(InstanceId, long, Instant, Registration)}.
	 * <ul>
	 * <li>Then return Registration is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceRegisteredEvent#InstanceRegisteredEvent(InstanceId, long, Instant, Registration)}
	 */
	@Test
	public void testNewInstanceRegisteredEvent_thenReturnRegistrationIsNull2() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

		// Act
		InstanceRegisteredEvent actualInstanceRegisteredEvent = new InstanceRegisteredEvent(instance, 1L, timestamp,
				null);

		// Assert
		assertNull(actualInstanceRegisteredEvent.getRegistration());
		assertEquals(1L, actualInstanceRegisteredEvent.getVersion());
		assertEquals(InstanceRegisteredEvent.TYPE, actualInstanceRegisteredEvent.getType());
		assertSame(instance, actualInstanceRegisteredEvent.getInstance());
		Instant expectedTimestamp = timestamp.EPOCH;
		assertSame(expectedTimestamp, actualInstanceRegisteredEvent.getTimestamp());
	}

}
