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

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryEnvironmentPostProcessorTest {

    private CloudFoundryEnvironmentPostProcessor processor = new CloudFoundryEnvironmentPostProcessor();
    private ConfigurableEnvironment environment;

    @Test
    public void not_cloud_foundry() {
        this.environment = new MockEnvironment();
        this.processor.postProcessEnvironment(this.environment,
            new SpringApplication());
        assertThat(this.environment.getProperty("spring.boot.admin.client.auto-deregistration")).isNull();
    }

    @Test
    public void cloud_foundry() {
        this.environment = new MockEnvironment().withProperty("VCAP_SERVICES", "---");
        this.processor.postProcessEnvironment(this.environment,
            new SpringApplication());
        assertThat(this.environment.getProperty("spring.boot.admin.client.auto-deregistration")).isEqualTo("true");
    }

    @Test
    public void not_override_when_property_is_set() {
        this.environment = new MockEnvironment()
            .withProperty("VCAP_SERVICES", "---")
            .withProperty("spring.boot.admin.client.auto-deregistration", "false");
        this.processor.postProcessEnvironment(this.environment,
            new SpringApplication());
        assertThat(this.environment.getProperty("spring.boot.admin.client.auto-deregistration")).isEqualTo("false");
    }
}
