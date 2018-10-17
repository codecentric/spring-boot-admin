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

package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.services.CloudFoundryInstanceIdGenerator;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.web.client.CloudFoundryHttpHeaderProvider;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerCloudFoundryAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(
            RestTemplateAutoConfiguration.class,
            HazelcastAutoConfiguration.class,
            WebMvcAutoConfiguration.class,
            AdminServerAutoConfiguration.class,
            AdminServerCloudFoundryAutoConfiguration.class
        )).withUserConfiguration(AdminServerMarkerConfiguration.class);

    @Test
    public void non_cloud_platform() {
        this.contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(CloudFoundryHttpHeaderProvider.class);
            assertThat(context).getBean(InstanceIdGenerator.class).isInstanceOf(HashingInstanceUrlIdGenerator.class);
        });
    }

    @Test
    public void cloudfoundry() {
        this.contextRunner.withPropertyValues("VCAP_APPLICATION:{}").run(context -> {
            assertThat(context).hasSingleBean(CloudFoundryHttpHeaderProvider.class);
            assertThat(context).getBean(InstanceIdGenerator.class).isInstanceOf(CloudFoundryInstanceIdGenerator.class);
        });
    }
}
