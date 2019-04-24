/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.ui.config;

import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController;
import de.codecentric.boot.admin.server.ui.extensions.UiExtension;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensionsScanner;
import de.codecentric.boot.admin.server.ui.extensions.UiRoutesScanner;
import de.codecentric.boot.admin.server.ui.web.UiController;
import de.codecentric.boot.admin.server.ui.web.UiController.Settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import static java.util.Arrays.asList;

@Configuration
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter(AdminServerWebConfiguration.class)
@EnableConfigurationProperties(AdminServerUiProperties.class)
public class AdminServerUiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AdminServerUiAutoConfiguration.class);
    private static final List<String> DEFAULT_UI_ROUTES = asList("/about/**",
        "/applications/**",
        "/instances/**",
        "/journal/**",
        "/wallboard/**",
        "/external/**"
    );
    private final AdminServerUiProperties adminUi;
    private final AdminServerProperties adminServer;
    private final ApplicationContext applicationContext;

    public AdminServerUiAutoConfiguration(AdminServerUiProperties adminUi,
                                          AdminServerProperties serverProperties,
                                          ApplicationContext applicationContext) {
        this.adminUi = adminUi;
        this.adminServer = serverProperties;
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public UiController homeUiController() throws IOException {
        List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext).scan(this.adminUi.getExtensionResourceLocations());
        List<String> routes = Stream.concat(DEFAULT_UI_ROUTES.stream(), extensionRoutes.stream())
                                    .collect(Collectors.toList());

        Settings uiSettings = Settings.builder()
                                      .brand(this.adminUi.getBrand())
                                      .title(this.adminUi.getTitle())
                                      .favicon(this.adminUi.getFavicon())
                                      .faviconDanger(this.adminUi.getFaviconDanger())
                                      .notificationFilterEnabled(!this.applicationContext.getBeansOfType(
                                          NotificationFilterController.class).isEmpty())
                                      .routes(routes)
                                      .externalViews(this.adminUi.getExternalViews())
                                      .build();

        String publicUrl = this.adminUi.getPublicUrl() !=
                           null ? this.adminUi.getPublicUrl() : this.adminServer.getContextPath();
        return new UiController(publicUrl, this.uiExtensions(), uiSettings);
    }

    private List<UiExtension> uiExtensions() throws IOException {
        UiExtensionsScanner scanner = new UiExtensionsScanner(this.applicationContext);
        List<UiExtension> uiExtensions = scanner.scan(this.adminUi.getExtensionResourceLocations());
        uiExtensions.forEach(e -> log.info("Loaded Spring Boot Admin UI Extension: {}", e));
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

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Configuration
    public static class ReactiveUiConfiguration implements WebFluxConfigurer {
        @Autowired
        private AdminServerUiProperties adminUi;
        @Autowired
        private AdminServerProperties adminServer;
        @Autowired
        private ApplicationContext applicationContext;

        @Override
        public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
            registry.addResourceHandler(this.adminServer.path("/**"))
                    .addResourceLocations(this.adminUi.getResourceLocations())
                    .addResourceLocations(this.adminUi.getExtensionResourceLocations())
                    .setCacheControl(this.adminUi.getCache().toCacheControl());
        }

        @Bean
        @ConditionalOnMissingBean
        public de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter homepageForwardFilter() throws IOException {
            List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext).scan(this.adminUi.getExtensionResourceLocations());
            List<String> routes = Stream.concat(DEFAULT_UI_ROUTES.stream(), extensionRoutes.stream())
                                        .map(this.adminServer::path)
                                        .collect(Collectors.toList());
            String homepage = this.adminServer.path("/");
            return new de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter(homepage, routes);
        }
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Configuration
    public static class ServletUiConfiguration implements WebMvcConfigurer {
        @Autowired
        private AdminServerUiProperties adminUi;
        @Autowired
        private AdminServerProperties adminServer;
        @Autowired
        private ApplicationContext applicationContext;

        @Override
        public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
            registry.addResourceHandler(this.adminServer.path("/**"))
                    .addResourceLocations(this.adminUi.getResourceLocations())
                    .addResourceLocations(this.adminUi.getExtensionResourceLocations())
                    .setCacheControl(this.adminUi.getCache().toCacheControl());
        }

        @Bean
        @ConditionalOnMissingBean
        public de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter homepageForwardFilter() throws IOException {
            List<String> extensionRoutes = new UiRoutesScanner(this.applicationContext).scan(this.adminUi.getExtensionResourceLocations());
            List<String> routes = Stream.concat(DEFAULT_UI_ROUTES.stream(), extensionRoutes.stream())
                                        .map(this.adminServer::path)
                                        .collect(Collectors.toList());
            String homepage = this.adminServer.path("/");
            return new de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter(homepage, routes);
        }
    }
}
