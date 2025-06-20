package de.codecentric.boot.admin.server.ui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiAutoConfiguration.ReactiveUiConfiguration;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiAutoConfiguration.ReactiveUiConfiguration.AdminUiWebfluxConfig;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiAutoConfiguration.ServletUiConfiguration;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiAutoConfiguration.ServletUiConfiguration.AdminUiWebMvcConfig;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.Cache;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.Palette;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.PollTimer;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.UiTheme;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensions;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import de.codecentric.boot.admin.server.ui.web.UiController;
import de.codecentric.boot.admin.server.ui.web.UiController.Settings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.io.ApplicationResourceLoader;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@ContextConfiguration(classes = { AdminUiWebMvcConfig.class, AdminServerUiProperties.class, AdminServerProperties.class,
		AdminUiWebfluxConfig.class, WebFluxProperties.class, AdminServerUiAutoConfiguration.class })
@ExtendWith(SpringExtension.class)
class AdminServerUiAutoConfigurationDiffblueTest {

	@Autowired
	private AdminServerProperties adminServerProperties;

	@Autowired
	private AdminServerUiProperties adminServerUiProperties;

	@Autowired
	private AdminUiWebMvcConfig adminUiWebMvcConfig;

	@Autowired
	private AdminUiWebfluxConfig adminUiWebfluxConfig;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private WebFluxProperties webFluxProperties;

	/**
	 * Test {@link AdminServerUiAutoConfiguration#homeUiController(UiExtensions)}.
	 * <ul>
	 * <li>Given {@code Object}.</li>
	 * <li>Then return UiSettings Brand is {@code Brand}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#homeUiController(UiExtensions)}
	 */
	@Test
	@DisplayName("Test homeUiController(UiExtensions); given 'java.lang.Object'; then return UiSettings Brand is 'Brand'")
	@Tag("MaintainedByDiffblue")
	void testHomeUiController_givenJavaLangObject_thenReturnUiSettingsBrandIsBrand() throws IOException {
		// Arrange
		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(new Cache());
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "Extension Resource Locations" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());
		AdminServerProperties serverProperties = new AdminServerProperties();
		Class<Object> forNameResult = Object.class;

		// Act
		UiController actualHomeUiControllerResult = new AdminServerUiAutoConfiguration(adminUi, serverProperties,
				new AnnotationConfigApplicationContext(forNameResult))
			.homeUiController(UiExtensions.EMPTY);

		// Assert
		Settings uiSettings = actualHomeUiControllerResult.getUiSettings();
		assertEquals("Brand", uiSettings.getBrand());
		assertEquals("Dr", uiSettings.getTitle());
		assertEquals("Favicon Danger", uiSettings.getFaviconDanger());
		assertEquals("Favicon", uiSettings.getFavicon());
		assertEquals("Login Icon", uiSettings.getLoginIcon());
		assertEquals("index", actualHomeUiControllerResult.index());
		assertEquals("login", actualHomeUiControllerResult.login());
		assertEquals(6, uiSettings.getRoutes().size());
		assertFalse(uiSettings.isNotificationFilterEnabled());
		assertTrue(uiSettings.getEnableToasts());
		assertTrue(uiSettings.getHideInstanceUrl());
		assertTrue(uiSettings.isRememberMeEnabled());
		assertTrue(actualHomeUiControllerResult.getCssExtensions().isEmpty());
		assertTrue(actualHomeUiControllerResult.getJsExtensions().isEmpty());
		assertTrue(uiSettings.getAvailableLanguages().isEmpty());
		assertTrue(uiSettings.getExternalViews().isEmpty());
		assertTrue(uiSettings.getViewSettings().isEmpty());
		assertSame(pollTimer, uiSettings.getPollTimer());
		assertSame(theme, uiSettings.getTheme());
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#adminTemplateResolver()}.
	 * <p>
	 * Method under test: {@link AdminServerUiAutoConfiguration#adminTemplateResolver()}
	 */
	@Test
	@DisplayName("Test adminTemplateResolver()")
	@Tag("MaintainedByDiffblue")
	void testAdminTemplateResolver() {
		// Arrange
		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(new Cache());
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "Extension Resource Locations" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());
		AdminServerProperties serverProperties = new AdminServerProperties();

		// Act
		SpringResourceTemplateResolver actualAdminTemplateResolverResult = new AdminServerUiAutoConfiguration(adminUi,
				serverProperties, new AnnotationConfigReactiveWebApplicationContext())
			.adminTemplateResolver();

		// Assert
		assertEquals(".html", actualAdminTemplateResolverResult.getSuffix());
		assertEquals("Template Location", actualAdminTemplateResolverResult.getPrefix());
		assertEquals("UTF-8", actualAdminTemplateResolverResult.getCharacterEncoding());
		assertEquals("org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver",
				actualAdminTemplateResolverResult.getName());
		assertNull(actualAdminTemplateResolverResult.getCacheTTLMs());
		assertEquals(10, actualAdminTemplateResolverResult.getOrder().intValue());
		assertEquals(TemplateMode.HTML, actualAdminTemplateResolverResult.getTemplateMode());
		assertFalse(actualAdminTemplateResolverResult.getForceSuffix());
		assertFalse(actualAdminTemplateResolverResult.getForceTemplateMode());
		assertFalse(actualAdminTemplateResolverResult.getUseDecoupledLogic());
		assertTrue(actualAdminTemplateResolverResult.getTemplateAliases().isEmpty());
		Set<String> cSSTemplateModePatterns = actualAdminTemplateResolverResult.getCSSTemplateModePatterns();
		assertTrue(cSSTemplateModePatterns.isEmpty());
		assertTrue(actualAdminTemplateResolverResult.isCacheable());
		assertTrue(actualAdminTemplateResolverResult.getCheckExistence());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getCacheablePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getHtmlTemplateModePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getJavaScriptTemplateModePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getNonCacheablePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getRawTemplateModePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getTextTemplateModePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getXmlTemplateModePatterns());
		assertSame(cSSTemplateModePatterns, actualAdminTemplateResolverResult.getResolvablePatterns());
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}.
	 * <ul>
	 * <li>Then return {@code /https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}
	 */
	@Test
	@DisplayName("Test normalizeHomepageUrl(String); then return '/https://example.org/example'")
	@Tag("MaintainedByDiffblue")
	void testNormalizeHomepageUrl_thenReturnHttpsExampleOrgExample() {
		// Arrange, Act and Assert
		assertEquals("/https://example.org/example",
				AdminServerUiAutoConfiguration.normalizeHomepageUrl("https://example.org/example"));
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}
	 */
	@Test
	@DisplayName("Test normalizeHomepageUrl(String); when 'null'; then return 'null'")
	@Tag("MaintainedByDiffblue")
	void testNormalizeHomepageUrl_whenNull_thenReturnNull() {
		// Arrange, Act and Assert
		assertNull(AdminServerUiAutoConfiguration.normalizeHomepageUrl(null));
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}.
	 * <ul>
	 * <li>When {@code ///}.</li>
	 * <li>Then return {@code /}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}
	 */
	@Test
	@DisplayName("Test normalizeHomepageUrl(String); when '///'; then return '/'")
	@Tag("MaintainedByDiffblue")
	void testNormalizeHomepageUrl_whenSlashSlashSlash_thenReturnSlash() {
		// Arrange, Act and Assert
		assertEquals("/", AdminServerUiAutoConfiguration.normalizeHomepageUrl("///"));
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}.
	 * <ul>
	 * <li>When {@code //}.</li>
	 * <li>Then return {@code /}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}
	 */
	@Test
	@DisplayName("Test normalizeHomepageUrl(String); when '//'; then return '/'")
	@Tag("MaintainedByDiffblue")
	void testNormalizeHomepageUrl_whenSlashSlash_thenReturnSlash() {
		// Arrange, Act and Assert
		assertEquals("/", AdminServerUiAutoConfiguration.normalizeHomepageUrl("//"));
	}

	/**
	 * Test {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}.
	 * <ul>
	 * <li>When {@code /}.</li>
	 * <li>Then return {@code /}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link AdminServerUiAutoConfiguration#normalizeHomepageUrl(String)}
	 */
	@Test
	@DisplayName("Test normalizeHomepageUrl(String); when '/'; then return '/'")
	@Tag("MaintainedByDiffblue")
	void testNormalizeHomepageUrl_whenSlash_thenReturnSlash() {
		// Arrange, Act and Assert
		assertEquals("/", AdminServerUiAutoConfiguration.normalizeHomepageUrl("/"));
	}

	/**
	 * Test ReactiveUiConfiguration_AdminUiWebfluxConfig
	 * {@link AdminUiWebfluxConfig#addResourceHandlers(ResourceHandlerRegistry)}.
	 * <p>
	 * Method under test:
	 * {@link AdminUiWebfluxConfig#addResourceHandlers(ResourceHandlerRegistry)}
	 */
	@Test
	@DisplayName("Test ReactiveUiConfiguration_AdminUiWebfluxConfig addResourceHandlers(ResourceHandlerRegistry)")
	@Tag("MaintainedByDiffblue")
	void testReactiveUiConfiguration_AdminUiWebfluxConfigAddResourceHandlers() {
		// Arrange
		Cache cache = mock(Cache.class);
		when(cache.toCacheControl()).thenReturn(CacheControl.empty());

		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(cache);
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "Extension Resource Locations" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());

		WebFluxProperties webFluxProperties = new WebFluxProperties();
		webFluxProperties.setBasePath("Base Path");
		webFluxProperties.setStaticPathPattern("Static Path Pattern");
		webFluxProperties.setWebjarsPathPattern("Webjars Path Pattern");
		AdminServerProperties adminServer = new AdminServerProperties();
		AdminUiWebfluxConfig adminUiWebfluxConfig = new AdminUiWebfluxConfig(adminUi, adminServer, webFluxProperties,
				new AnnotationConfigReactiveWebApplicationContext());

		// Act
		adminUiWebfluxConfig.addResourceHandlers(new ResourceHandlerRegistry(new ApplicationResourceLoader()));

		// Assert
		verify(cache, atLeast(1)).toCacheControl();
	}

	/**
	 * Test ReactiveUiConfiguration_AdminUiWebfluxConfig
	 * {@link AdminUiWebfluxConfig#homepageForwardingFilterConfig()}.
	 * <p>
	 * Method under test: {@link AdminUiWebfluxConfig#homepageForwardingFilterConfig()}
	 */
	@Test
	@DisplayName("Test ReactiveUiConfiguration_AdminUiWebfluxConfig homepageForwardingFilterConfig()")
	@Tag("MaintainedByDiffblue")
	void testReactiveUiConfiguration_AdminUiWebfluxConfigHomepageForwardingFilterConfig() throws IOException {
		// Arrange and Act
		HomepageForwardingFilterConfig actualHomepageForwardingFilterConfigResult = adminUiWebfluxConfig
			.homepageForwardingFilterConfig();

		// Assert
		List<String> routesIncludes = actualHomepageForwardingFilterConfigResult.getRoutesIncludes();
		assertEquals(7, routesIncludes.size());
		assertEquals("", routesIncludes.get(6));
		assertEquals("/", actualHomepageForwardingFilterConfigResult.getHomepage());
		assertEquals("/about/**", routesIncludes.get(0));
		assertEquals("/applications/**", routesIncludes.get(1));
		List<String> routesExcludes = actualHomepageForwardingFilterConfigResult.getRoutesExcludes();
		assertEquals(2, routesExcludes.size());
		assertEquals("/extensions/**", routesExcludes.get(0));
		assertEquals("/external/**", routesIncludes.get(5));
		assertEquals("/instances/**", routesIncludes.get(2));
		assertEquals("/instances/*/actuator/**", routesExcludes.get(1));
		assertEquals("/wallboard/**", routesIncludes.get(4));
	}

	/**
	 * Test ReactiveUiConfiguration_AdminUiWebfluxConfig
	 * {@link AdminUiWebfluxConfig#homepageForwardingFilterConfig()}.
	 * <p>
	 * Method under test: {@link AdminUiWebfluxConfig#homepageForwardingFilterConfig()}
	 */
	@Test
	@DisplayName("Test ReactiveUiConfiguration_AdminUiWebfluxConfig homepageForwardingFilterConfig()")
	@Tag("MaintainedByDiffblue")
	void testReactiveUiConfiguration_AdminUiWebfluxConfigHomepageForwardingFilterConfig2() throws IOException {
		// Arrange
		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(new Cache());
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "classpath:" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());

		WebFluxProperties webFluxProperties = new WebFluxProperties();
		webFluxProperties.setBasePath("Base Path");
		webFluxProperties.setStaticPathPattern("Static Path Pattern");
		webFluxProperties.setWebjarsPathPattern("Webjars Path Pattern");
		AdminServerProperties adminServer = new AdminServerProperties();

		// Act
		HomepageForwardingFilterConfig actualHomepageForwardingFilterConfigResult = new AdminUiWebfluxConfig(adminUi,
				adminServer, webFluxProperties, new AnnotationConfigReactiveWebApplicationContext())
			.homepageForwardingFilterConfig();

		// Assert
		assertEquals("/Base Path", actualHomepageForwardingFilterConfigResult.getHomepage());
		List<String> routesIncludes = actualHomepageForwardingFilterConfigResult.getRoutesIncludes();
		assertEquals(8, routesIncludes.size());
		assertEquals("/Base Path/about/**", routesIncludes.get(0));
		assertEquals("/Base Path/applications/**", routesIncludes.get(1));
		assertEquals("/Base Path/custom/**", routesIncludes.get(6));
		List<String> routesExcludes = actualHomepageForwardingFilterConfigResult.getRoutesExcludes();
		assertEquals(2, routesExcludes.size());
		assertEquals("/Base Path/extensions/**", routesExcludes.get(0));
		assertEquals("/Base Path/external/**", routesIncludes.get(5));
		assertEquals("/Base Path/instances/**", routesIncludes.get(2));
		assertEquals("/Base Path/instances/*/actuator/**", routesExcludes.get(1));
	}

	/**
	 * Test ServletUiConfiguration_AdminUiWebMvcConfig
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#addResourceHandlers(ResourceHandlerRegistry)}.
	 * <p>
	 * Method under test:
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry)}
	 */
	@Test
	@DisplayName("Test ServletUiConfiguration_AdminUiWebMvcConfig addResourceHandlers(ResourceHandlerRegistry)")
	@Tag("MaintainedByDiffblue")
	void testServletUiConfiguration_AdminUiWebMvcConfigAddResourceHandlers() {
		// Arrange
		Cache cache = mock(Cache.class);
		when(cache.toCacheControl()).thenReturn(CacheControl.empty());

		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(cache);
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "Extension Resource Locations" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());
		AdminServerProperties adminServer = new AdminServerProperties();
		AdminUiWebMvcConfig adminUiWebMvcConfig = new AdminUiWebMvcConfig(adminUi, adminServer,
				new AnnotationConfigReactiveWebApplicationContext());
		AnnotationConfigReactiveWebApplicationContext applicationContext = new AnnotationConfigReactiveWebApplicationContext();

		// Act
		adminUiWebMvcConfig.addResourceHandlers(
				new org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry(applicationContext,
						new MockServletContext()));

		// Assert
		verify(cache, atLeast(1)).toCacheControl();
	}

	/**
	 * Test ServletUiConfiguration_AdminUiWebMvcConfig
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#configurePathMatch(PathMatchConfigurer)}.
	 * <p>
	 * Method under test:
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#configurePathMatch(PathMatchConfigurer)}
	 */
	@Test
	@DisplayName("Test ServletUiConfiguration_AdminUiWebMvcConfig configurePathMatch(PathMatchConfigurer)")
	@Tag("MaintainedByDiffblue")
	void testServletUiConfiguration_AdminUiWebMvcConfigConfigurePathMatch() {
		// Arrange
		PathMatchConfigurer configurer = new PathMatchConfigurer();

		// Act
		adminUiWebMvcConfig.configurePathMatch(configurer);

		// Assert
		assertTrue(configurer.isUseTrailingSlashMatch());
	}

	/**
	 * Test ServletUiConfiguration_AdminUiWebMvcConfig
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#homepageForwardingFilterConfig()}.
	 * <p>
	 * Method under test:
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#homepageForwardingFilterConfig()}
	 */
	@Test
	@DisplayName("Test ServletUiConfiguration_AdminUiWebMvcConfig homepageForwardingFilterConfig()")
	@Tag("MaintainedByDiffblue")
	void testServletUiConfiguration_AdminUiWebMvcConfigHomepageForwardingFilterConfig() throws IOException {
		// Arrange and Act
		HomepageForwardingFilterConfig actualHomepageForwardingFilterConfigResult = adminUiWebMvcConfig
			.homepageForwardingFilterConfig();

		// Assert
		assertEquals("/", actualHomepageForwardingFilterConfigResult.getHomepage());
		List<String> routesIncludes = actualHomepageForwardingFilterConfigResult.getRoutesIncludes();
		assertEquals(7, routesIncludes.size());
		assertEquals("/", routesIncludes.get(6));
		assertEquals("/about/**", routesIncludes.get(0));
		assertEquals("/applications/**", routesIncludes.get(1));
		List<String> routesExcludes = actualHomepageForwardingFilterConfigResult.getRoutesExcludes();
		assertEquals(2, routesExcludes.size());
		assertEquals("/extensions/**", routesExcludes.get(0));
		assertEquals("/external/**", routesIncludes.get(5));
		assertEquals("/instances/**", routesIncludes.get(2));
		assertEquals("/instances/*/actuator/**", routesExcludes.get(1));
		assertEquals("/wallboard/**", routesIncludes.get(4));
	}

	/**
	 * Test ServletUiConfiguration_AdminUiWebMvcConfig
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#homepageForwardingFilterConfig()}.
	 * <p>
	 * Method under test:
	 * {@link ServletUiConfiguration.AdminUiWebMvcConfig#homepageForwardingFilterConfig()}
	 */
	@Test
	@DisplayName("Test ServletUiConfiguration_AdminUiWebMvcConfig homepageForwardingFilterConfig()")
	@Tag("MaintainedByDiffblue")
	void testServletUiConfiguration_AdminUiWebMvcConfigHomepageForwardingFilterConfig2() throws IOException {
		// Arrange
		PollTimer pollTimer = new PollTimer();
		pollTimer.setCache(1);
		pollTimer.setDatasource(1);
		pollTimer.setGc(1);
		pollTimer.setLogfile(1);
		pollTimer.setMemory(1);
		pollTimer.setProcess(1);
		pollTimer.setThreads(1);

		Palette palette = new Palette();
		palette.set100("Shade100");
		palette.set200("Shade200");
		palette.set300("Shade300");
		palette.set400("Shade400");
		palette.set50("Shade50");
		palette.set500("Shade500");
		palette.set600("Shade600");
		palette.set700("Shade700");
		palette.set800("Shade800");
		palette.set900("Shade900");

		UiTheme theme = new UiTheme();
		theme.setBackgroundEnabled(true);
		theme.setColor("Color");
		theme.setPalette(palette);

		AdminServerUiProperties adminUi = new AdminServerUiProperties();
		adminUi.setAdditionalRouteExcludes(new ArrayList<>());
		adminUi.setAvailableLanguages(new ArrayList<>());
		adminUi.setBrand("Brand");
		adminUi.setCache(new Cache());
		adminUi.setCacheTemplates(true);
		adminUi.setEnableToasts(true);
		adminUi.setExtensionResourceLocations(new String[] { "classpath:" });
		adminUi.setExternalViews(new ArrayList<>());
		adminUi.setFavicon("Favicon");
		adminUi.setFaviconDanger("Favicon Danger");
		adminUi.setHideInstanceUrl(true);
		adminUi.setLoginIcon("Login Icon");
		adminUi.setPollTimer(pollTimer);
		adminUi.setPublicUrl("https://example.org/example");
		adminUi.setRememberMeEnabled(true);
		adminUi.setResourceLocations(new String[] { "Resource Locations" });
		adminUi.setTemplateLocation("Template Location");
		adminUi.setTheme(theme);
		adminUi.setTitle("Dr");
		adminUi.setViewSettings(new ArrayList<>());
		AdminServerProperties adminServer = new AdminServerProperties();

		// Act
		HomepageForwardingFilterConfig actualHomepageForwardingFilterConfigResult = new AdminUiWebMvcConfig(adminUi,
				adminServer, new AnnotationConfigReactiveWebApplicationContext())
			.homepageForwardingFilterConfig();

		// Assert
		assertEquals("/", actualHomepageForwardingFilterConfigResult.getHomepage());
		List<String> routesIncludes = actualHomepageForwardingFilterConfigResult.getRoutesIncludes();
		assertEquals(8, routesIncludes.size());
		assertEquals("/", routesIncludes.get(7));
		assertEquals("/about/**", routesIncludes.get(0));
		assertEquals("/applications/**", routesIncludes.get(1));
		assertEquals("/custom/**", routesIncludes.get(6));
		List<String> routesExcludes = actualHomepageForwardingFilterConfigResult.getRoutesExcludes();
		assertEquals(2, routesExcludes.size());
		assertEquals("/extensions/**", routesExcludes.get(0));
		assertEquals("/external/**", routesIncludes.get(5));
		assertEquals("/instances/**", routesIncludes.get(2));
		assertEquals("/instances/*/actuator/**", routesExcludes.get(1));
	}

}
