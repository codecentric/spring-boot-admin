package de.codecentric.boot.admin.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.CloudFoundryInstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import org.junit.Test;

public class AdminServerCloudFoundryAutoConfigurationDiffblueTest {
  /**
   * Test {@link AdminServerCloudFoundryAutoConfiguration#instanceIdGenerator()}.
   * <p>
   * Method under test: {@link AdminServerCloudFoundryAutoConfiguration#instanceIdGenerator()}
   */
  @Test
  public void testInstanceIdGenerator() {
    // Arrange and Act
    InstanceIdGenerator actualInstanceIdGeneratorResult = new AdminServerCloudFoundryAutoConfiguration()
        .instanceIdGenerator();
    Registration registration = Registration.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .source("Source")
        .build();
    InstanceId actualGenerateIdResult = actualInstanceIdGeneratorResult.generateId(registration);

    // Assert
    assertTrue(actualInstanceIdGeneratorResult instanceof CloudFoundryInstanceIdGenerator);
    assertEquals("504149e8a3fa", actualGenerateIdResult.getValue());
    assertEquals("504149e8a3fa", actualGenerateIdResult.toString());
  }
}
