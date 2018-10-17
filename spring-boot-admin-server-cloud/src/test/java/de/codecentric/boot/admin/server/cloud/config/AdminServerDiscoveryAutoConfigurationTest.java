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

package de.codecentric.boot.admin.server.cloud.config;

import de.codecentric.boot.admin.server.cloud.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.ServiceInstanceConverter;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.domain.values.Registration;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import com.netflix.discovery.EurekaClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerDiscoveryAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(UtilAutoConfiguration.class,
            AdminServerAutoConfiguration.class,
            AdminServerDiscoveryAutoConfiguration.class
        )).withUserConfiguration(AdminServerMarkerConfiguration.class);

    @Test
    public void defaultServiceInstanceConverter() {
        contextRunner.withUserConfiguration(SimpleDiscoveryClientAutoConfiguration.class)
                     .run(context -> assertThat(context.getBean(ServiceInstanceConverter.class)).isInstanceOf(
                         DefaultServiceInstanceConverter.class));
    }

    @Test
    public void eurekaServiceInstanceConverter() {
        contextRunner.withUserConfiguration(EurekaClientConfig.class)
                     .run(context -> assertThat(context).getBean(ServiceInstanceConverter.class)
                                                        .isInstanceOf(EurekaServiceInstanceConverter.class));
    }

    @Test
    public void customServiceInstanceConverter() {
        contextRunner.withUserConfiguration(SimpleDiscoveryClientAutoConfiguration.class,
            TestCustomServiceInstanceConverterConfig.class
        )
                     .run(context -> assertThat(context).getBean(ServiceInstanceConverter.class)
                                                        .isInstanceOf(CustomServiceInstanceConverter.class));
    }

    static class TestCustomServiceInstanceConverterConfig {
        @Bean
        public CustomServiceInstanceConverter converter() {
            return new CustomServiceInstanceConverter();
        }
    }

    static class CustomServiceInstanceConverter implements ServiceInstanceConverter {
        @Override
        public Registration convert(ServiceInstance instance) {
            return null;
        }
    }

    static class EurekaClientConfig {
        @Bean
        public EurekaClient eurekaClient() {
            return Mockito.mock(EurekaClient.class);
        }

        @Bean
        public DiscoveryClient discoveryClient() {
            return Mockito.mock(DiscoveryClient.class);
        }
    }
}
