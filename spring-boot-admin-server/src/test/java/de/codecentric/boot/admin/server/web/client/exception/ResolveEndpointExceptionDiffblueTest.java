package de.codecentric.boot.admin.server.web.client.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ResolveEndpointExceptionDiffblueTest {
  /**
   * Test {@link ResolveEndpointException#ResolveEndpointException(String)}.
   * <ul>
   *   <li>When {@code An error occurred}.</li>
   *   <li>Then return Cause is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ResolveEndpointException#ResolveEndpointException(String)}
   */
  @Test
  public void testNewResolveEndpointException_whenAnErrorOccurred_thenReturnCauseIsNull() {
    // Arrange and Act
    ResolveEndpointException actualResolveEndpointException = new ResolveEndpointException("An error occurred");

    // Assert
    assertEquals("An error occurred", actualResolveEndpointException.getMessage());
    assertNull(actualResolveEndpointException.getCause());
    assertEquals(0, actualResolveEndpointException.getSuppressed().length);
  }

  /**
   * Test {@link ResolveEndpointException#ResolveEndpointException(String, Throwable)}.
   * <ul>
   *   <li>When {@link Throwable#Throwable()}.</li>
   *   <li>Then return Cause is {@link Throwable#Throwable()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ResolveEndpointException#ResolveEndpointException(String, Throwable)}
   */
  @Test
  public void testNewResolveEndpointException_whenThrowable_thenReturnCauseIsThrowable() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    ResolveEndpointException actualResolveEndpointException = new ResolveEndpointException("An error occurred", cause);

    // Assert
    assertEquals("An error occurred", actualResolveEndpointException.getMessage());
    assertEquals(0, actualResolveEndpointException.getSuppressed().length);
    assertSame(cause, actualResolveEndpointException.getCause());
  }
}
