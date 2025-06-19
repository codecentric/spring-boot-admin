package de.codecentric.boot.admin.client.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.core.env.Environment;

class ClientPropertiesDiffblueTest {
  /**
   * Test {@link ClientProperties#getAdminUrl()}.
   * <ul>
   *   <li>Given {@link ClientProperties} (default constructor).</li>
   *   <li>Then return array length is zero.</li>
   * </ul>
   * <p>
   * Method under test: {@link ClientProperties#getAdminUrl()}
   */
  @Test
  @DisplayName("Test getAdminUrl(); given ClientProperties (default constructor); then return array length is zero")
  @Tag("MaintainedByDiffblue")
  void testGetAdminUrl_givenClientProperties_thenReturnArrayLengthIsZero() {
    // Arrange, Act and Assert
    assertEquals(0, new ClientProperties().getAdminUrl().length);
  }

  /**
   * Test {@link ClientProperties#getAdminUrl()}.
   * <ul>
   *   <li>Then return array of {@link String} with {@code https://example.org/example/Api Path}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ClientProperties#getAdminUrl()}
   */
  @Test
  @DisplayName("Test getAdminUrl(); then return array of String with 'https://example.org/example/Api Path'")
  @Tag("MaintainedByDiffblue")
  void testGetAdminUrl_thenReturnArrayOfStringWithHttpsExampleOrgExampleApiPath() {
    // Arrange
    ClientProperties clientProperties = new ClientProperties();
    clientProperties.setApiPath("Api Path");
    clientProperties.setEnabled(true);
    clientProperties.setPassword("iloveyou");
    clientProperties.setRegisterOnce(true);
    clientProperties.setUrl(new String[]{"https://example.org/example"});
    clientProperties.setUsername("janedoe");

    // Act and Assert
    assertArrayEquals(new String[]{"https://example.org/example/Api Path"}, clientProperties.getAdminUrl());
  }

  /**
   * Test {@link ClientProperties#isAutoDeregistration(Environment)}.
   * <p>
   * Method under test: {@link ClientProperties#isAutoDeregistration(Environment)}
   */
  @Test
  @DisplayName("Test isAutoDeregistration(Environment)")
  @Tag("MaintainedByDiffblue")
  void testIsAutoDeregistration() {
    // Arrange
    ClientProperties clientProperties = new ClientProperties();

    // Act and Assert
    assertFalse(clientProperties.isAutoDeregistration(new StandardReactiveWebEnvironment()));
  }
}
