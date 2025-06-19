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

public class InstanceRegistrationUpdatedEventDiffblueTest {
  /**
   * Test {@link InstanceRegistrationUpdatedEvent#InstanceRegistrationUpdatedEvent(InstanceId, long, Registration)}.
   * <ul>
   *   <li>Then return Registration is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceRegistrationUpdatedEvent#InstanceRegistrationUpdatedEvent(InstanceId, long, Registration)}
   */
  @Test
  public void testNewInstanceRegistrationUpdatedEvent_thenReturnRegistrationIsNull() {
    // Arrange
    InstanceId instance = InstanceId.of("42");

    // Act
    InstanceRegistrationUpdatedEvent actualInstanceRegistrationUpdatedEvent = new InstanceRegistrationUpdatedEvent(
        instance, 1L, null);

    // Assert
    assertNull(actualInstanceRegistrationUpdatedEvent.getRegistration());
    assertEquals(1L, actualInstanceRegistrationUpdatedEvent.getVersion());
    assertEquals(InstanceRegistrationUpdatedEvent.TYPE, actualInstanceRegistrationUpdatedEvent.getType());
    assertSame(instance, actualInstanceRegistrationUpdatedEvent.getInstance());
  }

  /**
   * Test {@link InstanceRegistrationUpdatedEvent#InstanceRegistrationUpdatedEvent(InstanceId, long, Instant, Registration)}.
   * <ul>
   *   <li>Then return Registration is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceRegistrationUpdatedEvent#InstanceRegistrationUpdatedEvent(InstanceId, long, Instant, Registration)}
   */
  @Test
  public void testNewInstanceRegistrationUpdatedEvent_thenReturnRegistrationIsNull2() {
    // Arrange
    InstanceId instance = InstanceId.of("42");
    Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

    // Act
    InstanceRegistrationUpdatedEvent actualInstanceRegistrationUpdatedEvent = new InstanceRegistrationUpdatedEvent(
        instance, 1L, timestamp, null);

    // Assert
    assertNull(actualInstanceRegistrationUpdatedEvent.getRegistration());
    assertEquals(1L, actualInstanceRegistrationUpdatedEvent.getVersion());
    assertEquals(InstanceRegistrationUpdatedEvent.TYPE, actualInstanceRegistrationUpdatedEvent.getType());
    assertSame(instance, actualInstanceRegistrationUpdatedEvent.getInstance());
    Instant expectedTimestamp = timestamp.EPOCH;
    assertSame(expectedTimestamp, actualInstanceRegistrationUpdatedEvent.getTimestamp());
  }
}
