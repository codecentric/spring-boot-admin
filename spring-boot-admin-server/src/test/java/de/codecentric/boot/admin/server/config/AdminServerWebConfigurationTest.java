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
package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.entities.EventSourcingApplicationRepository;
import de.codecentric.boot.admin.server.eventstore.ClientApplicationEventStore;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.notify.MailNotifier;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import com.hazelcast.config.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerWebConfigurationTest {

    private AnnotationConfigWebApplicationContext context;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void jacksonMapperPresentFromDefault() {
        AdminServerWebConfiguration config = new AdminServerWebConfiguration(null);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());

        config.extendMessageConverters(converters);

        assertThat(converters).hasOnlyElementsOfType(MappingJackson2HttpMessageConverter.class);
        assertThat(converters).hasSize(1);
    }

    @Test
    public void jacksonMapperPresentNeedExtend() {
        AdminServerWebConfiguration config = new AdminServerWebConfiguration(null);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        config.extendMessageConverters(converters);

        assertThat(converters).hasOnlyElementsOfType(MappingJackson2HttpMessageConverter.class);
        assertThat(converters).hasSize(1);
    }

    @Test
    public void simpleConfig() {
        load();

        assertThat(context.getBean(ApplicationRepository.class)).isInstanceOf(EventSourcingApplicationRepository.class);
        assertThat(context.getBeansOfType(MailNotifier.class)).isEmpty();
        assertThat(context.getBean(ClientApplicationEventStore.class)).isInstanceOf(ConcurrentMapEventStore.class);
    }

    @Test
    public void hazelcastConfig() {
        load(TestHazelcastConfig.class);
        assertThat(context.getBean(ClientApplicationEventStore.class)).isInstanceOf(HazelcastEventStore.class);
    }

    @Test
    public void discoveryConfig() {
        load(SimpleDiscoveryClientAutoConfiguration.class);
        assertThat(context.getBean(ApplicationRepository.class)).isInstanceOf(EventSourcingApplicationRepository.class);
        context.getBean(ApplicationDiscoveryListener.class);
    }

    @Configuration
    static class TestHazelcastConfig {
        @Bean
        public Config config() {
            return new Config();
        }
    }

    private void load(String... environment) {
        load(null, environment);
    }

    private void load(Class<?> config, String... environment) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        if (config != null) {
            applicationContext.register(config);
        }
        applicationContext.register(RestTemplateAutoConfiguration.class);
        applicationContext.register(HazelcastAutoConfiguration.class);
        applicationContext.register(HazelcastStoreConfiguration.class);
        applicationContext.register(UtilAutoConfiguration.class);
        applicationContext.register(DiscoveryClientConfiguration.class);
        applicationContext.register(AdminServerCoreConfiguration.class);
        applicationContext.register(AdminServerWebConfiguration.class);

        TestPropertyValues.of(environment).applyTo(applicationContext);
        applicationContext.refresh();
        this.context = applicationContext;
    }
}
