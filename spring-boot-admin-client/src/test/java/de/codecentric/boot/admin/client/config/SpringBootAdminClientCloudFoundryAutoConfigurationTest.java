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

package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.CloudFoundryApplicationFactory;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CloudFoundryMetadataContributor;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootAdminClientCloudFoundryAutoConfigurationTest {
    private final AutoConfigurations autoConfigurations = AutoConfigurations.of(
        SpringBootAdminClientAutoConfiguration.class, SpringBootAdminClientCloudFoundryAutoConfiguration.class);
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner().withConfiguration(
        autoConfigurations).withUserConfiguration(TestClientApplication.class);

    @Test
    public void non_cloud_platform() {
        this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081").run(context -> {
            assertThat(context.getBeansOfType(CloudFoundryMetadataContributor.class)).isEmpty();
            assertThat(context.getBean(ApplicationFactory.class)).isInstanceOf(DefaultApplicationFactory.class);
        });
    }

    @Test
    public void cloudfoundry() {
        this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
                          .withPropertyValues("VCAP_APPLICATION:{}")
                          .run(context -> {
                              assertThat(context.getBean(CloudFoundryMetadataContributor.class)).isInstanceOf(
                                  CloudFoundryMetadataContributor.class);
                              assertThat(context.getBean(ApplicationFactory.class)).isInstanceOf(
                                  CloudFoundryApplicationFactory.class);
                          });
    }

    @Test
    public void cloudfoundry_disabled() {
        this.contextRunner.withPropertyValues("VCAP_APPLICATION:{}").run(context -> {
            assertThat(context.getBeansOfType(CloudFoundryMetadataContributor.class)).isEmpty();
            assertThat(context.getBeansOfType(ApplicationFactory.class)).isEmpty();
        });
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestClientApplication {
    }
}
