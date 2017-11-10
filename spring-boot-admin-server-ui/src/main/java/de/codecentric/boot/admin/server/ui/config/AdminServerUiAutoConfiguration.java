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
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.server.ui.web.UiController;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

@Configuration
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter(AdminServerWebConfiguration.class)
public class AdminServerUiAutoConfiguration {

    //FIXME make property of it
    public static final String SBA_RESOURCE_LOC = "file:/Users/jedmeier/projects/spring-boot-admin/spring-boot-admin-server-ui/target/dist/";
    //public static final String SBA_RESOURCE_LOC = "classpath:/META-INF/spring-boot-admin-server-ui/";

    @Configuration
    public static class UiConfiguration {
        private final ApplicationContext applicationContext;

        public UiConfiguration(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Bean
        @ConditionalOnMissingBean
        public UiController homeUiController() {
            return new UiController();
        }

        @Bean
        public SpringResourceTemplateResolver adminTemplateResolver() {
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setApplicationContext(this.applicationContext);
            resolver.setPrefix(SBA_RESOURCE_LOC);
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            //FIXME make property of it
            resolver.setCacheable(false);
            resolver.setOrder(10);
            resolver.setCheckExistence(true);
            return resolver;
        }

    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Configuration
    public static class ReactiveUiConfiguration implements WebFluxConfigurer {

        @Override
        public void addResourceHandlers(org.springframework.web.reactive.config.ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**").addResourceLocations(SBA_RESOURCE_LOC)
                    //FIXME make property of it
                    .setCacheControl(CacheControl.noStore());
        }
    }

    //FIXME move to ui project
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Configuration
    public static class ServletUiConfiguration implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**").addResourceLocations(SBA_RESOURCE_LOC)
                    //FIXME make property of it
                    .setCacheControl(CacheControl.noStore());
        }
    }
}
