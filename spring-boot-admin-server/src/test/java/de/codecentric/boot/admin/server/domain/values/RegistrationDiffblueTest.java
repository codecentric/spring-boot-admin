package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RegistrationDiffblueTest {
  /**
   * Test {@link Registration#getMetadata()}.
   * <p>
   * Method under test: {@link Registration#getMetadata()}
   */
  @Test
  public void testGetMetadata() {
    // Arrange
    Registration buildResult = Registration.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .source("Source")
        .build();

    // Act and Assert
    assertTrue(buildResult.getMetadata().isEmpty());
  }
}
