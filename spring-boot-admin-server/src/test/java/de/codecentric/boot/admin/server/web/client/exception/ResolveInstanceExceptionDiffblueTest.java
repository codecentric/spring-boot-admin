package de.codecentric.boot.admin.server.web.client.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ResolveInstanceExceptionDiffblueTest {
  /**
   * Test {@link ResolveInstanceException#ResolveInstanceException(String)}.
   * <ul>
   *   <li>When {@code An error occurred}.</li>
   *   <li>Then return Cause is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ResolveInstanceException#ResolveInstanceException(String)}
   */
  @Test
  public void testNewResolveInstanceException_whenAnErrorOccurred_thenReturnCauseIsNull() {
    // Arrange and Act
    ResolveInstanceException actualResolveInstanceException = new ResolveInstanceException("An error occurred");

    // Assert
    assertEquals("An error occurred", actualResolveInstanceException.getMessage());
    assertNull(actualResolveInstanceException.getCause());
    assertEquals(0, actualResolveInstanceException.getSuppressed().length);
  }

  /**
   * Test {@link ResolveInstanceException#ResolveInstanceException(String, Throwable)}.
   * <ul>
   *   <li>When {@link Throwable#Throwable()}.</li>
   *   <li>Then return Cause is {@link Throwable#Throwable()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ResolveInstanceException#ResolveInstanceException(String, Throwable)}
   */
  @Test
  public void testNewResolveInstanceException_whenThrowable_thenReturnCauseIsThrowable() {
    // Arrange
    Throwable cause = new Throwable();

    // Act
    ResolveInstanceException actualResolveInstanceException = new ResolveInstanceException("An error occurred", cause);

    // Assert
    assertEquals("An error occurred", actualResolveInstanceException.getMessage());
    assertEquals(0, actualResolveInstanceException.getSuppressed().length);
    assertSame(cause, actualResolveInstanceException.getCause());
  }
}
