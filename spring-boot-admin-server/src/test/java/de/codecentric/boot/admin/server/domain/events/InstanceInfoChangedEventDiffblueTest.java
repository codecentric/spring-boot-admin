package de.codecentric.boot.admin.server.domain.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceInfoChangedEventDiffblueTest {

	/**
	 * Test
	 * {@link InstanceInfoChangedEvent#InstanceInfoChangedEvent(InstanceId, long, Info)}.
	 * <ul>
	 * <li>Then return Version is one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceInfoChangedEvent#InstanceInfoChangedEvent(InstanceId, long, Info)}
	 */
	@Test
	public void testNewInstanceInfoChangedEvent_thenReturnVersionIsOne() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Info info = Info.empty();

		// Act
		InstanceInfoChangedEvent actualInstanceInfoChangedEvent = new InstanceInfoChangedEvent(instance, 1L, info);

		// Assert
		assertEquals(1L, actualInstanceInfoChangedEvent.getVersion());
		Info info2 = actualInstanceInfoChangedEvent.getInfo();
		assertTrue(info2.getValues().isEmpty());
		assertEquals(InstanceInfoChangedEvent.TYPE, actualInstanceInfoChangedEvent.getType());
		assertSame(info, info2);
		assertSame(instance, actualInstanceInfoChangedEvent.getInstance());
	}

	/**
	 * Test
	 * {@link InstanceInfoChangedEvent#InstanceInfoChangedEvent(InstanceId, long, Instant, Info)}.
	 * <ul>
	 * <li>Then return Version is one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceInfoChangedEvent#InstanceInfoChangedEvent(InstanceId, long, Instant, Info)}
	 */
	@Test
	public void testNewInstanceInfoChangedEvent_thenReturnVersionIsOne2() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
		Info info = Info.empty();

		// Act
		InstanceInfoChangedEvent actualInstanceInfoChangedEvent = new InstanceInfoChangedEvent(instance, 1L, timestamp,
				info);

		// Assert
		assertEquals(1L, actualInstanceInfoChangedEvent.getVersion());
		Info info2 = actualInstanceInfoChangedEvent.getInfo();
		assertTrue(info2.getValues().isEmpty());
		assertEquals(InstanceInfoChangedEvent.TYPE, actualInstanceInfoChangedEvent.getType());
		assertSame(info, info2);
		assertSame(instance, actualInstanceInfoChangedEvent.getInstance());
		Instant expectedTimestamp = timestamp.EPOCH;
		assertSame(expectedTimestamp, actualInstanceInfoChangedEvent.getTimestamp());
	}

}
