package de.codecentric.boot.admin.server.web.client.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class InstanceWebClientExceptionDiffblueTest {

	/**
	 * Test {@link InstanceWebClientException#InstanceWebClientException(String)}.
	 * <ul>
	 * <li>When {@code An error occurred}.</li>
	 * <li>Then return Cause is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceWebClientException#InstanceWebClientException(String)}
	 */
	@Test
	public void testNewInstanceWebClientException_whenAnErrorOccurred_thenReturnCauseIsNull() {
		// Arrange and Act
		InstanceWebClientException actualInstanceWebClientException = new InstanceWebClientException(
				"An error occurred");

		// Assert
		assertEquals("An error occurred", actualInstanceWebClientException.getMessage());
		assertNull(actualInstanceWebClientException.getCause());
		assertEquals(0, actualInstanceWebClientException.getSuppressed().length);
	}

	/**
	 * Test
	 * {@link InstanceWebClientException#InstanceWebClientException(String, Throwable)}.
	 * <ul>
	 * <li>When {@link Throwable#Throwable()}.</li>
	 * <li>Then return Cause is {@link Throwable#Throwable()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link InstanceWebClientException#InstanceWebClientException(String, Throwable)}
	 */
	@Test
	public void testNewInstanceWebClientException_whenThrowable_thenReturnCauseIsThrowable() {
		// Arrange
		Throwable cause = new Throwable();

		// Act
		InstanceWebClientException actualInstanceWebClientException = new InstanceWebClientException(
				"An error occurred", cause);

		// Assert
		assertEquals("An error occurred", actualInstanceWebClientException.getMessage());
		assertEquals(0, actualInstanceWebClientException.getSuppressed().length);
		assertSame(cause, actualInstanceWebClientException.getCause());
	}

}
