package de.codecentric.boot.admin.server.ui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {CssColorUtils.class})
@ExtendWith(SpringExtension.class)
class CssColorUtilsDiffblueTest {
  @Autowired
  private CssColorUtils cssColorUtils;

  /**
   * Test {@link CssColorUtils#hexToRgb(String)}.
   * <ul>
   *   <li>When {@code #999999}.</li>
   *   <li>Then return {@code 153, 153, 153}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CssColorUtils#hexToRgb(String)}
   */
  @Test
  @DisplayName("Test hexToRgb(String); when '#999999'; then return '153, 153, 153'")
  @Tag("MaintainedByDiffblue")
  void testHexToRgb_when999999_thenReturn153153153() {
    // Arrange, Act and Assert
    assertEquals("153, 153, 153", cssColorUtils.hexToRgb("#999999"));
  }

  /**
   * Test {@link CssColorUtils#hexToRgb(String)}.
   * <ul>
   *   <li>When {@code 0123456789ABCDEF}.</li>
   *   <li>Then throw {@link IllegalArgumentException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CssColorUtils#hexToRgb(String)}
   */
  @Test
  @DisplayName("Test hexToRgb(String); when '0123456789ABCDEF'; then throw IllegalArgumentException")
  @Tag("MaintainedByDiffblue")
  void testHexToRgb_when0123456789abcdef_thenThrowIllegalArgumentException() {
    // Arrange, Act and Assert
    assertThrows(IllegalArgumentException.class, () -> cssColorUtils.hexToRgb("0123456789ABCDEF"));
  }
}
