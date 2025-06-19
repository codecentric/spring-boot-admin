package de.codecentric.boot.admin.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class AdminServerPropertiesDiffblueTest {
  /**
   * Test {@link AdminServerProperties#setContextPath(String)}.
   * <ul>
   *   <li>Then {@link AdminServerProperties} (default constructor) ContextPath is {@code /Context Path}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerProperties#setContextPath(String)}
   */
  @Test
  public void testSetContextPath_thenAdminServerPropertiesContextPathIsContextPath() {
    // Arrange
    AdminServerProperties adminServerProperties = new AdminServerProperties();

    // Act
    adminServerProperties.setContextPath("Context Path");

    // Assert
    assertEquals("/Context Path", adminServerProperties.getContextPath());
  }

  /**
   * Test {@link AdminServerProperties#setContextPath(String)}.
   * <ul>
   *   <li>Then {@link AdminServerProperties} (default constructor) ContextPath is {@code /}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerProperties#setContextPath(String)}
   */
  @Test
  public void testSetContextPath_thenAdminServerPropertiesContextPathIsSlash() {
    // Arrange
    AdminServerProperties adminServerProperties = new AdminServerProperties();

    // Act
    adminServerProperties.setContextPath("///");

    // Assert
    assertEquals("/", adminServerProperties.getContextPath());
  }

  /**
   * Test {@link AdminServerProperties#setContextPath(String)}.
   * <ul>
   *   <li>When {@code null}.</li>
   *   <li>Then {@link AdminServerProperties} (default constructor) ContextPath is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerProperties#setContextPath(String)}
   */
  @Test
  public void testSetContextPath_whenNull_thenAdminServerPropertiesContextPathIsNull() {
    // Arrange
    AdminServerProperties adminServerProperties = new AdminServerProperties();

    // Act
    adminServerProperties.setContextPath(null);

    // Assert
    assertNull(adminServerProperties.getContextPath());
  }

  /**
   * Test {@link AdminServerProperties#setContextPath(String)}.
   * <ul>
   *   <li>When {@code /}.</li>
   *   <li>Then {@link AdminServerProperties} (default constructor) ContextPath is empty string.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerProperties#setContextPath(String)}
   */
  @Test
  public void testSetContextPath_whenSlash_thenAdminServerPropertiesContextPathIsEmptyString() {
    // Arrange
    AdminServerProperties adminServerProperties = new AdminServerProperties();

    // Act
    adminServerProperties.setContextPath("/");

    // Assert that nothing has changed
    assertEquals("", adminServerProperties.getContextPath());
  }

  /**
   * Test {@link AdminServerProperties#path(String)}.
   * <p>
   * Method under test: {@link AdminServerProperties#path(String)}
   */
  @Test
  public void testPath() {
    // Arrange, Act and Assert
    assertEquals("Path", new AdminServerProperties().path("Path"));
  }
}
