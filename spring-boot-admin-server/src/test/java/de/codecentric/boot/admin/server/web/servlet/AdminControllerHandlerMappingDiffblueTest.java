package de.codecentric.boot.admin.server.web.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;

@ContextConfiguration(classes = { AdminControllerHandlerMapping.class, String.class })
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AdminControllerHandlerMappingDiffblueTest {

	@Autowired
	private AdminControllerHandlerMapping adminControllerHandlerMapping;

	/**
	 * Test {@link AdminControllerHandlerMapping#AdminControllerHandlerMapping(String)}.
	 * <p>
	 * Method under test:
	 * {@link AdminControllerHandlerMapping#AdminControllerHandlerMapping(String)}
	 */
	@Test
	public void testNewAdminControllerHandlerMapping() {
		// Arrange and Act
		AdminControllerHandlerMapping actualAdminControllerHandlerMapping = new AdminControllerHandlerMapping(
				"Admin Context Path");

		// Assert
		assertTrue(actualAdminControllerHandlerMapping.getPathMatcher() instanceof AntPathMatcher);
		assertTrue(actualAdminControllerHandlerMapping.getCorsProcessor() instanceof DefaultCorsProcessor);
		assertTrue(actualAdminControllerHandlerMapping
			.getNamingStrategy() instanceof RequestMappingInfoHandlerMethodMappingNamingStrategy);
		assertNull(actualAdminControllerHandlerMapping.getDefaultHandler());
		assertNull(actualAdminControllerHandlerMapping.getFileExtensions());
		assertNull(actualAdminControllerHandlerMapping.getCorsConfigurationSource());
		assertNull(actualAdminControllerHandlerMapping.getAdaptedInterceptors());
		assertFalse(actualAdminControllerHandlerMapping.useRegisteredSuffixPatternMatch());
		assertFalse(actualAdminControllerHandlerMapping.useSuffixPatternMatch());
		assertFalse(actualAdminControllerHandlerMapping.useTrailingSlashMatch());
		assertTrue(actualAdminControllerHandlerMapping.getHandlerMethods().isEmpty());
		assertTrue(actualAdminControllerHandlerMapping.getPathPrefixes().isEmpty());
		assertEquals(Integer.MAX_VALUE, actualAdminControllerHandlerMapping.getOrder());
	}

	/**
	 * Test {@link AdminControllerHandlerMapping#isHandler(Class)}.
	 * <ul>
	 * <li>When {@code Object}.</li>
	 * <li>Then return {@code false}.</li>
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
