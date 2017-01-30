/*
 * Copyright 2014-2017 the original author or authors.
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

package de.codecentric.boot.admin.config;

import de.codecentric.boot.admin.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.discovery.ServiceInstanceConverter;
import de.codecentric.boot.admin.model.Application;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import com.netflix.discovery.EurekaClient;

import static org.assertj.core.api.Assertions.assertThat;

public class DiscoveryClientConfigurationTest {
    private AnnotationConfigWebApplicationContext context;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultServiceInstanceConverter() {
        load(SimpleDiscoveryClientAutoConfiguration.class);
        assertThat(context.getBean(ServiceInstanceConverter.class)).isInstanceOf(DefaultServiceInstanceConverter.class);
    }

    @Test
    public void eurekaServiceInstanceConverter() {
        load(EurekaClientConfig.class);
        assertThat(context.getBean(ServiceInstanceConverter.class)).isInstanceOf(EurekaServiceInstanceConverter.class);
    }

    @Test
    public void customServiceInstanceConverter() {
        load(SimpleDiscoveryClientAutoConfiguration.class, TestCustomServiceInstanceConverterConfig.class);
        assertThat(context.getBean(ServiceInstanceConverter.class)).isInstanceOf(CustomServiceInstanceConverter.class);
    }

    @Configuration
    static class TestCustomServiceInstanceConverterConfig {
        @Bean
        public CustomServiceInstanceConverter converter() {
            return new CustomServiceInstanceConverter();
        }
    }

    static class CustomServiceInstanceConverter implements ServiceInstanceConverter {
        @Override
        public Application convert(ServiceInstance instance) {
            return null;
        }
    }

    @Configuration
    protected static class EurekaClientConfig {
        @Bean
        public EurekaClient eurekaClient() {
            return Mockito.mock(EurekaClient.class);
        }

        @Bean
        public DiscoveryClient discoveryClient() {
            return Mockito.mock(DiscoveryClient.class);
        }
    }

    private void load(Class<?>... configs) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        for (Class<?> config : configs) {
            applicationContext.register(config);
        }
        applicationContext.register(UtilAutoConfiguration.class);
        applicationContext.register(RestTemplateAutoConfiguration.class);
        applicationContext.register(AdminServerCoreConfiguration.class);
        applicationContext.register(AdminServerWebConfiguration.class);
        applicationContext.register(UtilAutoConfiguration.class);
        applicationContext.register(DiscoveryClientConfiguration.class);

        applicationContext.refresh();
        this.context = applicationContext;
    }
}
