package de.codecentric.boot.admin.server.domain.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceStatusChangedEventDiffblueTest {
  /**
   * Test {@link InstanceStatusChangedEvent#InstanceStatusChangedEvent(InstanceId, long, StatusInfo)}.
   * <ul>
   *   <li>Then return StatusInfo is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceStatusChangedEvent#InstanceStatusChangedEvent(InstanceId, long, StatusInfo)}
   */
  @Test
  public void testNewInstanceStatusChangedEvent_thenReturnStatusInfoIsNull() {
    // Arrange
    InstanceId instance = InstanceId.of("42");

    // Act
    InstanceStatusChangedEvent actualInstanceStatusChangedEvent = new InstanceStatusChangedEvent(instance, 1L, null);

    // Assert
    assertNull(actualInstanceStatusChangedEvent.getStatusInfo());
    assertEquals(1L, actualInstanceStatusChangedEvent.getVersion());
    assertEquals(InstanceStatusChangedEvent.TYPE, actualInstanceStatusChangedEvent.getType());
    assertSame(instance, actualInstanceStatusChangedEvent.getInstance());
  }

  /**
   * Test {@link InstanceStatusChangedEvent#InstanceStatusChangedEvent(InstanceId, long, Instant, StatusInfo)}.
   * <ul>
   *   <li>Then return StatusInfo is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceStatusChangedEvent#InstanceStatusChangedEvent(InstanceId, long, Instant, StatusInfo)}
   */
  @Test
  public void testNewInstanceStatusChangedEvent_thenReturnStatusInfoIsNull2() {
    // Arrange
    InstanceId instance = InstanceId.of("42");
    Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

    // Act
    InstanceStatusChangedEvent actualInstanceStatusChangedEvent = new InstanceStatusChangedEvent(instance, 1L,
        timestamp, null);

    // Assert
    assertNull(actualInstanceStatusChangedEvent.getStatusInfo());
    assertEquals(1L, actualInstanceStatusChangedEvent.getVersion());
    assertEquals(InstanceStatusChangedEvent.TYPE, actualInstanceStatusChangedEvent.getType());
    assertSame(instance, actualInstanceStatusChangedEvent.getInstance());
    Instant expectedTimestamp = timestamp.EPOCH;
    assertSame(expectedTimestamp, actualInstanceStatusChangedEvent.getTimestamp());
  }
}
