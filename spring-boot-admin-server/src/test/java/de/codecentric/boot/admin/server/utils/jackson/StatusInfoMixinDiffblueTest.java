package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class StatusInfoMixinDiffblueTest {
  /**
   * Test {@link StatusInfoMixin#valueOf(String, Map)}.
   * <ul>
   *   <li>When {@code not blank}.</li>
   *   <li>Then return Status is {@code NOT BLANK}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfoMixin#valueOf(String, Map)}
   */
  @Test
  public void testValueOf_whenNotBlank_thenReturnStatusIsNotBlank() {
    // Arrange and Act
    StatusInfo actualValueOfResult = StatusInfoMixin.valueOf("not blank", null);

    // Assert
    assertEquals("NOT BLANK", actualValueOfResult.getStatus());
    assertFalse(actualValueOfResult.isDown());
    assertFalse(actualValueOfResult.isOffline());
    assertFalse(actualValueOfResult.isUnknown());
    assertFalse(actualValueOfResult.isUp());
    assertTrue(actualValueOfResult.getDetails().isEmpty());
  }

  /**
   * Test {@link StatusInfoMixin#valueOf(String, Map)}.
   * <ul>
   *   <li>When {@code Status Code}.</li>
   *   <li>Then return Status is {@code STATUS CODE}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfoMixin#valueOf(String, Map)}
   */
  @Test
  public void testValueOf_whenStatusCode_thenReturnStatusIsStatusCode() {
    // Arrange and Act
    StatusInfo actualValueOfResult = StatusInfoMixin.valueOf("Status Code", new HashMap<>());

    // Assert
    assertEquals("STATUS CODE", actualValueOfResult.getStatus());
    assertFalse(actualValueOfResult.isDown());
    assertFalse(actualValueOfResult.isOffline());
    assertFalse(actualValueOfResult.isUnknown());
    assertFalse(actualValueOfResult.isUp());
    assertTrue(actualValueOfResult.getDetails().isEmpty());
  }
}
