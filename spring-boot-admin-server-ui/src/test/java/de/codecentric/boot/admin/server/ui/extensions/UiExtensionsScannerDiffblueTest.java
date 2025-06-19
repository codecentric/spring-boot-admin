package de.codecentric.boot.admin.server.ui.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class UiExtensionsScannerDiffblueTest {
  /**
   * Test {@link UiExtensionsScanner#scan(String[])}.
   * <ul>
   *   <li>Then return {@link UiExtensions#EMPTY}.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensionsScanner#scan(String[])}
   */
  @Test
  @DisplayName("Test scan(String[]); then return EMPTY")
  @Tag("MaintainedByDiffblue")
  void testScan_thenReturnEmpty() throws IOException {
    // Arrange and Act
    UiExtensions actualScanResult = new UiExtensionsScanner(new PathMatchingResourcePatternResolver())
        .scan("Locations");

    // Assert
    assertEquals(actualScanResult.EMPTY, actualScanResult);
  }
}
