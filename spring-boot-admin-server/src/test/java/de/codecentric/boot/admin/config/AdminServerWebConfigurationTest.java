/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.config;

import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.journal.store.HazelcastJournaledEventStore;
import de.codecentric.boot.admin.journal.store.JournaledEventStore;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.notify.MailNotifier;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.HazelcastApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration.RestTemplateConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import com.hazelcast.config.Config;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        AdminServerWebConfiguration config = new AdminServerWebConfiguration(null, null, null, null);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());

        config.extendMessageConverters(converters);

        assertThat(converters, hasItem(isA(MappingJackson2HttpMessageConverter.class)));
        assertThat(converters.size(), is(1));
    }

    @Test
    public void jacksonMapperPresentNeedExtend() {
        AdminServerWebConfiguration config = new AdminServerWebConfiguration(null, null, null, null);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        config.extendMessageConverters(converters);

        assertThat(converters, hasItem(isA(MappingJackson2HttpMessageConverter.class)));
        assertThat(converters.size(), is(1));
    }

    @Test
    public void simpleConfig() {
        load();
        assertThat(context.getBean(ApplicationStore.class), is(instanceOf(SimpleApplicationStore.class)));
        assertTrue(context.getBeansOfType(ApplicationDiscoveryListener.class).isEmpty());
        assertTrue(context.getBeansOfType(MailNotifier.class).isEmpty());
        assertThat(context.getBean(JournaledEventStore.class), is(instanceOf(SimpleJournaledEventStore.class)));
    }

    @Test
    public void hazelcastConfig() {
        load(TestHazelcastConfig.class);
        assertThat(context.getBean(ApplicationStore.class), is(instanceOf(HazelcastApplicationStore.class)));
        assertThat(context.getBean(JournaledEventStore.class), is(instanceOf(HazelcastJournaledEventStore.class)));
        assertTrue(context.getBeansOfType(ApplicationDiscoveryListener.class).isEmpty());
    }

    @Test
    public void discoveryConfig() {
        load(SimpleDiscoveryClientAutoConfiguration.class);
        assertThat(context.getBean(ApplicationStore.class), is(instanceOf(SimpleApplicationStore.class)));
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
        applicationContext.register(PropertyPlaceholderAutoConfiguration.class);
        applicationContext.register(RestTemplateConfiguration.class);
        applicationContext.register(ServerPropertiesAutoConfiguration.class);
        applicationContext.register(HazelcastAutoConfiguration.class);
        applicationContext.register(HazelcastStoreConfiguration.class);
        applicationContext.register(UtilAutoConfiguration.class);
        applicationContext.register(DiscoveryClientConfiguration.class);
        applicationContext.register(AdminServerCoreConfiguration.class);
        applicationContext.register(AdminServerWebConfiguration.class);

        EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.refresh();
        this.context = applicationContext;
    }
}
