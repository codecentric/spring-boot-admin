/*
 * Copyright 2014-2018 the original author or authors.
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
import de.codecentric.boot.admin.server.ui.web.UiController;

import java.nio.charset.StandardCharsets;
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

@Configuration
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter(AdminServerWebConfiguration.class)
@EnableConfigurationProperties(AdminServerUiProperties.class)
public class AdminServerUiAutoConfiguration {
    private final AdminServerUiProperties uiProperties;
    private final AdminServerProperties adminServerProperties;
    private final ApplicationContext applicationContext;

    public AdminServerUiAutoConfiguration(AdminServerUiProperties uiProperties,
                                          AdminServerProperties serverProperties,
                                          ApplicationContext applicationContext) {
        this.uiProperties = uiProperties;
        this.adminServerProperties = serverProperties;
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public UiController homeUiController() {
        return new UiController(adminServerProperties.getContextPath(),
            uiProperties.getTitle(),
            uiProperties.getBrand());
    }

    @Bean
    public SpringResourceTemplateResolver adminTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(this.applicationContext);
        resolver.setPrefix(uiProperties.getTemplateLocation());
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(uiProperties.isCacheTemplates());
        resolver.setOrder(10);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Configuration
    public static class ReactiveUiConfiguration implements WebFluxConfigurer {
        @Autowired
        private AdminServerUiProperties uiProperties;
        @Autowired
        private AdminServerProperties adminServerProperties;

        @Override
        public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
            registry.addResourceHandler(adminServerProperties.getContextPath() + "/**")
                    .addResourceLocations(uiProperties.getResourceLocations())
                    .setCacheControl(uiProperties.getCache().toCacheControl());
        }
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Configuration
    public static class ServletUiConfiguration implements WebMvcConfigurer {
        @Autowired
        private AdminServerUiProperties uiProperties;
        @Autowired
        private AdminServerProperties adminServerProperties;

        @Override
        public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
            registry.addResourceHandler(adminServerProperties.getContextPath() + "/**")
                    .addResourceLocations(uiProperties.getResourceLocations())
                    .setCacheControl(uiProperties.getCache().toCacheControl());
        }
    }
}
