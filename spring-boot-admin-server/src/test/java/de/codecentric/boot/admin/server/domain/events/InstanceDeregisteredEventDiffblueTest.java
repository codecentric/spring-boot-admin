package de.codecentric.boot.admin.server.domain.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class InstanceDeregisteredEventDiffblueTest {
  /**
   * Test {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long)}.
   * <ul>
   *   <li>Then return Version is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long)}
   */
  @Test
  public void testNewInstanceDeregisteredEvent_thenReturnVersionIsOne() {
    // Arrange
    InstanceId instance = InstanceId.of("42");

    // Act
    InstanceDeregisteredEvent actualInstanceDeregisteredEvent = new InstanceDeregisteredEvent(instance, 1L);

    // Assert
    assertEquals(1L, actualInstanceDeregisteredEvent.getVersion());
    assertEquals(InstanceDeregisteredEvent.TYPE, actualInstanceDeregisteredEvent.getType());
    assertSame(instance, actualInstanceDeregisteredEvent.getInstance());
  }

  /**
   * Test {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long, Instant)}.
   * <ul>
   *   <li>Then return Version is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long, Instant)}
   */
  @Test
  public void testNewInstanceDeregisteredEvent_thenReturnVersionIsOne2() {
    // Arrange
    InstanceId instance = InstanceId.of("42");
    Instant timestamp = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

    // Act
    InstanceDeregisteredEvent actualInstanceDeregisteredEvent = new InstanceDeregisteredEvent(instance, 1L, timestamp);

    // Assert
    assertEquals(1L, actualInstanceDeregisteredEvent.getVersion());
    assertEquals(InstanceDeregisteredEvent.TYPE, actualInstanceDeregisteredEvent.getType());
    assertSame(instance, actualInstanceDeregisteredEvent.getInstance());
    Instant expectedTimestamp = timestamp.EPOCH;
    assertSame(expectedTimestamp, actualInstanceDeregisteredEvent.getTimestamp());
  }
}
