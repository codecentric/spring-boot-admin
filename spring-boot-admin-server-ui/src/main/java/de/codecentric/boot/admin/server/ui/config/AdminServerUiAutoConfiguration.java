/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.ui.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.server.config.SpringBootAdminServerEnabledCondition;
import de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensions;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensionsScanner;
import de.codecentric.boot.admin.server.ui.extensions.UiRoutesScanner;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import de.codecentric.boot.admin.server.ui.web.UiController;
import de.codecentric.boot.admin.server.ui.web.UiController.Settings;
import de.codecentric.boot.admin.server.web.PathUtils;

import static java.util.Arrays.asList;

@Configuration(proxyBeanMethods = false)
@Conditional(SpringBootAdminServerEnabledCondition.class)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter(AdminServerWebConfiguration.class)
@EnableConfigurationProperties(AdminServerUiProperties.class)
public class AdminServerUiAutoConfiguration {

	private static final Logger log = LoggerFactory.getLogger(AdminServerUiAutoConfiguration.class);

	/**
	 * path patterns that will be forwarded to the homepage (webapp)
	 */
	private static final List<String> DEFAULT_UI_ROUTES = asList("/about/**", "/applications/**", "/instances/**",
			"/journal/**", "/wallboard/**", "/external/**");

	/**
	 * path patterns that will be excluded from forwarding to the homepage (webapp), can
	 * be extended via property: spring.boot.admin.ui.additionalRouteExcludes
	 */
	private static final List<String> DEFAULT_UI_ROUTE_EXCLUDES = asList("/extensions/**", "/instances/*/actuator/**");

	private final AdminServerUiProperties adminUi;

	private final AdminServerProperties adminServer;

	private final ApplicationContext applicationContext;

	public AdminServerUiAutoConfiguration(AdminServerUiProperties adminUi, AdminServerProperties serverProperties,
			ApplicationContext applicationContext) {
		this.adminUi = adminUi;
		this.adminServer = serverProperties;
		this.applicationContext = applicationContext;
	}

	@Bean
	public CssColorUtils cssColorUtils() {
		return new CssColorUtils();
	}

	@Bean
	@ConditionalOnMissingBean
	public UiController homeUiController(UiExtensions uiExtensions) throws IOException {
		List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext)
			.scan(this.adminUi.getExtensionResourceLocations());
		List<String> routes = Stream.concat(DEFAULT_UI_ROUTES.stream(), extensionRoutes.stream())
			.collect(Collectors.toList());

		Settings uiSettings = Settings.builder()
			.brand(this.adminUi.getBrand())
			.title(this.adminUi.getTitle())
			.loginIcon(this.adminUi.getLoginIcon())
			.favicon(this.adminUi.getFavicon())
			.faviconDanger(this.adminUi.getFaviconDanger())
			.enableToasts(this.adminUi.getEnableToasts())
			.hideInstanceUrl(this.adminUi.getHideInstanceUrl())
			.notificationFilterEnabled(
					!this.applicationContext.getBeansOfType(NotificationFilterController.class).isEmpty())
			.routes(routes)
			.rememberMeEnabled(this.adminUi.isRememberMeEnabled())
			.availableLanguages(this.adminUi.getAvailableLanguages())
			.externalViews(this.adminUi.getExternalViews())
			.pollTimer(this.adminUi.getPollTimer())
			.viewSettings(this.adminUi.getViewSettings())
			.theme(this.adminUi.getTheme())
			.build();

		String publicUrl = (this.adminUi.getPublicUrl() != null) ? this.adminUi.getPublicUrl()
				: this.adminServer.getContextPath();
		return new UiController(publicUrl, uiExtensions, uiSettings);
	}

	@Bean
	UiExtensions uiExtensions() throws IOException {
		UiExtensionsScanner scanner = new UiExtensionsScanner(this.applicationContext);
		UiExtensions uiExtensions = scanner.scan(this.adminUi.getExtensionResourceLocations());
		uiExtensions.forEach((e) -> log.info("Loaded Spring Boot Admin UI Extension: {}", e));
		return uiExtensions;
	}

	@Bean
	public SpringResourceTemplateResolver adminTemplateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(this.applicationContext);
		resolver.setPrefix(this.adminUi.getTemplateLocation());
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resolver.setCacheable(this.adminUi.isCacheTemplates());
		resolver.setOrder(10);
		resolver.setCheckExistence(true);
		return resolver;
	}

	static String normalizeHomepageUrl(String homepage) {
		if (!"/".equals(homepage)) {
			homepage = PathUtils.normalizePath(homepage);
		}
		return homepage;
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
	public static class ReactiveUiConfiguration {

		@Configuration(proxyBeanMethods = false)
		public static class AdminUiWebfluxConfig implements WebFluxConfigurer {

			private final AdminServerUiProperties adminUi;

			private final AdminServerProperties adminServer;

			private final WebFluxProperties webFluxProperties;

			private final ApplicationContext applicationContext;

			public AdminUiWebfluxConfig(AdminServerUiProperties adminUi, AdminServerProperties adminServer,
					WebFluxProperties webFluxProperties, ApplicationContext applicationContext) {
				this.adminUi = adminUi;
				this.adminServer = adminServer;
				this.webFluxProperties = webFluxProperties;
				this.applicationContext = applicationContext;
			}

			@Bean
			public HomepageForwardingFilterConfig homepageForwardingFilterConfig() throws IOException {
				String webFluxBasePath = webFluxProperties.getBasePath();
				boolean webfluxBasePathSet = webFluxBasePath != null;
				String homepage = normalizeHomepageUrl(
						webfluxBasePathSet ? webFluxBasePath + "/" : this.adminServer.path("/"));

				List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext)
					.scan(this.adminUi.getExtensionResourceLocations());
				List<String> routesIncludes = Stream.concat(DEFAULT_UI_ROUTES.stream(), extensionRoutes.stream())
					.map((path) -> webfluxBasePathSet ? webFluxBasePath + path : this.adminServer.path(path))
					.collect(Collectors.toList());
				routesIncludes.add("");

				List<String> routesExcludes = Stream
					.concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.adminUi.getAdditionalRouteExcludes().stream())
					.map((path) -> webfluxBasePathSet ? webFluxBasePath + path : this.adminServer.path(path))
					.collect(Collectors.toList());

				return new HomepageForwardingFilterConfig(homepage, routesIncludes, routesExcludes);
			}

			@Override
			public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
				registry.addResourceHandler(this.adminServer.path("/**"))
					.addResourceLocations(this.adminUi.getResourceLocations())
					.setCacheControl(this.adminUi.getCache().toCacheControl())
					.setMediaTypes(Map.of("js", new MediaType("application", "javascript")));

				registry.addResourceHandler(this.adminServer.path("/extensions/**"))
					.addResourceLocations(this.adminUi.getExtensionResourceLocations())
					.setCacheControl(this.adminUi.getCache().toCacheControl())
					.setMediaTypes(Map.of("js", new MediaType("application", "javascript")));
			}

			@Bean
			@ConditionalOnMissingBean
			public de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter homepageForwardFilter(
					HomepageForwardingFilterConfig homepageForwardingFilterConfig) throws IOException {
				return new de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter(
						homepageForwardingFilterConfig);
			}

		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	public static class ServletUiConfiguration {

		@Configuration(proxyBeanMethods = false)
		public static class AdminUiWebMvcConfig implements WebMvcConfigurer {

			private final AdminServerUiProperties adminUi;

			private final AdminServerProperties adminServer;

			private final ApplicationContext applicationContext;

			public AdminUiWebMvcConfig(AdminServerUiProperties adminUi, AdminServerProperties adminServer,
					ApplicationContext applicationContext) {
				this.adminUi = adminUi;
				this.adminServer = adminServer;
				this.applicationContext = applicationContext;
			}

			@Override
			public void configurePathMatch(PathMatchConfigurer configurer) {
				configurer.setUseTrailingSlashMatch(true);
			}

			@Bean
			public HomepageForwardingFilterConfig homepageForwardingFilterConfig() throws IOException {
				String homepage = normalizeHomepageUrl(this.adminServer.path("/"));

				List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext)
					.scan(this.adminUi.getExtensionResourceLocations());
				List<String> routesIncludes = Stream
					.concat(DEFAULT_UI_ROUTES.stream(), Stream.concat(extensionRoutes.stream(), Stream.of("/")))
					.map(this.adminServer::path)
					.collect(Collectors.toList());

				List<String> routesExcludes = Stream
					.concat(DEFAULT_UI_ROUTE_EXCLUDES.stream(), this.adminUi.getAdditionalRouteExcludes().stream())
					.map(this.adminServer::path)
					.collect(Collectors.toList());

				return new HomepageForwardingFilterConfig(homepage, routesIncludes, routesExcludes);
			}

			@Override
			public void addResourceHandlers(
					org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
				registry.addResourceHandler(this.adminServer.path("/**"))
					.addResourceLocations(this.adminUi.getResourceLocations())
					.setCacheControl(this.adminUi.getCache().toCacheControl());
				registry.addResourceHandler(this.adminServer.path("/extensions/**"))
					.addResourceLocations(this.adminUi.getExtensionResourceLocations())
					.setCacheControl(this.adminUi.getCache().toCacheControl());
			}

			@Bean
			@ConditionalOnMissingBean
			public de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter homepageForwardFilter(
					HomepageForwardingFilterConfig homepageForwardingFilterConfig) throws IOException {
				return new de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter(
						homepageForwardingFilterConfig);
			}

		}

	}

}
