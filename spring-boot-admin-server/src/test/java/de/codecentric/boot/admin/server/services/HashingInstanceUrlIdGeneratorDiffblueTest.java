package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertEquals;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {HashingInstanceUrlIdGenerator.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class HashingInstanceUrlIdGeneratorDiffblueTest {
  @Autowired
  private HashingInstanceUrlIdGenerator hashingInstanceUrlIdGenerator;

  /**
   * Test {@link HashingInstanceUrlIdGenerator#generateId(Registration)}.
   * <ul>
   *   <li>Then return Value is {@code 504149e8a3fa}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HashingInstanceUrlIdGenerator#generateId(Registration)}
   */
  @Test
  public void testGenerateId_thenReturnValueIs504149e8a3fa() {
    // Arrange
    Registration registration = Registration.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .source("Source")
        .build();

    // Act
    InstanceId actualGenerateIdResult = hashingInstanceUrlIdGenerator.generateId(registration);

    // Assert
    assertEquals("504149e8a3fa", actualGenerateIdResult.getValue());
    assertEquals("504149e8a3fa", actualGenerateIdResult.toString());
  }
}
