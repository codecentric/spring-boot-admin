package de.codecentric.boot.admin.server.notify.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class ApplicationNameNotificationFilterDiffblueTest {

	/**
	 * Test
	 * {@link ApplicationNameNotificationFilter#ApplicationNameNotificationFilter(String, Instant)}.
	 * <p>
	 * Method under test:
	 * {@link ApplicationNameNotificationFilter#ApplicationNameNotificationFilter(String, Instant)}
	 */
	@Test
	public void testNewApplicationNameNotificationFilter() {
		// Arrange
		Instant expiry = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

		// Act
		ApplicationNameNotificationFilter actualApplicationNameNotificationFilter = new ApplicationNameNotificationFilter(
				"Application Name", expiry);

		// Assert
		assertEquals("Application Name", actualApplicationNameNotificationFilter.getApplicationName());
		Instant expectedExpiry = expiry.EPOCH;
		assertSame(expectedExpiry, actualApplicationNameNotificationFilter.getExpiry());
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link ApplicationNameNotificationFilter#toString()}
	 * <li>{@link ApplicationNameNotificationFilter#getApplicationName()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		ApplicationNameNotificationFilter applicationNameNotificationFilter = new ApplicationNameNotificationFilter(
				"Application Name", LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		// Act
		String actualToStringResult = applicationNameNotificationFilter.toString();

		// Assert
		assertEquals("Application Name", applicationNameNotificationFilter.getApplicationName());
		assertEquals("NotificationFilter [applicationName=Application Name, expiry=1970-01-01T00:00:00Z]",
				actualToStringResult);
	}

}
