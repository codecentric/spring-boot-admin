package de.codecentric.boot.admin.server.domain.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceEndpointsDetectedEventDiffblueTest {

	/**
	 * Test
	 * {@link InstanceEndpointsDetectedEvent#InstanceEndpointsDetectedEvent(InstanceId, long, Endpoints)}.
	 * <ul>
	 * <li>Then return Version is one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceEndpointsDetectedEvent#InstanceEndpointsDetectedEvent(InstanceId, long, Endpoints)}
	 */
	@Test
	public void testNewInstanceEndpointsDetectedEvent_thenReturnVersionIsOne() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Endpoints endpoints = Endpoints.empty();

		// Act
		InstanceEndpointsDetectedEvent actualInstanceEndpointsDetectedEvent = new InstanceEndpointsDetectedEvent(
				instance, 1L, endpoints);

		// Assert
		assertEquals(1L, actualInstanceEndpointsDetectedEvent.getVersion());
		assertEquals(InstanceEndpointsDetectedEvent.TYPE, actualInstanceEndpointsDetectedEvent.getType());
		assertSame(endpoints, actualInstanceEndpointsDetectedEvent.getEndpoints());
		assertSame(instance, actualInstanceEndpointsDetectedEvent.getInstance());
	}

	/**
	 * Test
	 * {@link InstanceEndpointsDetectedEvent#InstanceEndpointsDetectedEvent(InstanceId, long, Instant, Endpoints)}.
	 * <ul>
	 * <li>Then return Version is one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceEndpointsDetectedEvent#InstanceEndpointsDetectedEvent(InstanceId, long, Instant, Endpoints)}
	 */
	@Test
	public void testNewInstanceEndpointsDetectedEvent_thenReturnVersionIsOne2() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
		Endpoints endpoints = Endpoints.empty();

		// Act
		InstanceEndpointsDetectedEvent actualInstanceEndpointsDetectedEvent = new InstanceEndpointsDetectedEvent(
				instance, 1L, timestamp, endpoints);

		// Assert
		assertEquals(1L, actualInstanceEndpointsDetectedEvent.getVersion());
		assertEquals(InstanceEndpointsDetectedEvent.TYPE, actualInstanceEndpointsDetectedEvent.getType());
		assertSame(endpoints, actualInstanceEndpointsDetectedEvent.getEndpoints());
		assertSame(instance, actualInstanceEndpointsDetectedEvent.getInstance());
		Instant expectedTimestamp = timestamp.EPOCH;
		assertSame(expectedTimestamp, actualInstanceEndpointsDetectedEvent.getTimestamp());
	}

}
