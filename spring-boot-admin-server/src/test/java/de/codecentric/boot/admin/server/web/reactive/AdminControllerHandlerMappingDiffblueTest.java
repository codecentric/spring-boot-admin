package de.codecentric.boot.admin.server.web.reactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.util.pattern.PathPatternParser;

@ContextConfiguration(classes = {AdminControllerHandlerMapping.class, String.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class AdminControllerHandlerMappingDiffblueTest {
  @Autowired
  private AdminControllerHandlerMapping adminControllerHandlerMapping;

  /**
   * Test {@link AdminControllerHandlerMapping#AdminControllerHandlerMapping(String)}.
   * <p>
   * Method under test: {@link AdminControllerHandlerMapping#AdminControllerHandlerMapping(String)}
   */
  @Test
  public void testNewAdminControllerHandlerMapping() throws IllegalStateException {
    // Arrange and Act
    AdminControllerHandlerMapping actualAdminControllerHandlerMapping = new AdminControllerHandlerMapping(
        "Admin Context Path");

    // Assert
    assertTrue(actualAdminControllerHandlerMapping.getCorsProcessor() instanceof DefaultCorsProcessor);
    PathPatternParser pathPatternParser = actualAdminControllerHandlerMapping.getPathPatternParser();
    assertEquals('/', pathPatternParser.getPathOptions().separator());
    assertNull(actualAdminControllerHandlerMapping.getApplicationContext());
    assertFalse(pathPatternParser.isMatchOptionalTrailingSeparator());
    assertTrue(actualAdminControllerHandlerMapping.getHandlerMethods().isEmpty());
    assertTrue(actualAdminControllerHandlerMapping.getPathPrefixes().isEmpty());
    assertTrue(pathPatternParser.isCaseSensitive());
    assertEquals(Integer.MAX_VALUE, actualAdminControllerHandlerMapping.getOrder());
  }

  /**
   * Test {@link AdminControllerHandlerMapping#isHandler(Class)}.
   * <ul>
   *   <li>When {@code Object}.</li>
   *   <li>Then return {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminControllerHandlerMapping#isHandler(Class)}
   */
  @Test
  public void testIsHandler_whenJavaLangObject_thenReturnFalse() {
    // Arrange
    Class<Object> beanType = Object.class;

    // Act and Assert
    assertFalse(adminControllerHandlerMapping.isHandler(beanType));
  }
}
