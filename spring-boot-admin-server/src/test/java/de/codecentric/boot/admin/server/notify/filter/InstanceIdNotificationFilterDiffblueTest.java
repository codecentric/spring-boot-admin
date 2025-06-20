package de.codecentric.boot.admin.server.notify.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceIdNotificationFilterDiffblueTest {

	/**
	 * Test
	 * {@link InstanceIdNotificationFilter#InstanceIdNotificationFilter(InstanceId, Instant)}.
	 * <p>
	 * Method under test:
	 * {@link InstanceIdNotificationFilter#InstanceIdNotificationFilter(InstanceId, Instant)}
	 */
	@Test
	public void testNewInstanceIdNotificationFilter() {
		// Arrange
		InstanceId instanceId = InstanceId.of("42");
		Instant expiry = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

		// Act
		InstanceIdNotificationFilter actualInstanceIdNotificationFilter = new InstanceIdNotificationFilter(instanceId,
				expiry);

		// Assert
		assertSame(instanceId, actualInstanceIdNotificationFilter.getInstanceId());
		Instant expectedExpiry = expiry.EPOCH;
		assertSame(expectedExpiry, actualInstanceIdNotificationFilter.getExpiry());
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link InstanceIdNotificationFilter#toString()}
	 * <li>{@link InstanceIdNotificationFilter#getInstanceId()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		InstanceId instanceId = InstanceId.of("42");
		InstanceIdNotificationFilter instanceIdNotificationFilter = new InstanceIdNotificationFilter(instanceId,
				LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		// Act
		String actualToStringResult = instanceIdNotificationFilter.toString();

		// Assert
		assertEquals("NotificationFilter [instanceId=42, expiry=1970-01-01T00:00:00Z]", actualToStringResult);
		assertSame(instanceId, instanceIdNotificationFilter.getInstanceId());
	}

}
