/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.CloudFoundryApplicationFactory;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CloudFoundryMetadataContributor;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootAdminClientCloudFoundryAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
				WebMvcAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
				RestTemplateAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class,
				SpringBootAdminClientCloudFoundryAutoConfiguration.class));

	@Test
	public void non_cloud_platform() {
		this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081").run((context) -> {
			assertThat(context).doesNotHaveBean(CloudFoundryMetadataContributor.class);
			assertThat(context).getBean(ApplicationFactory.class).isInstanceOf(DefaultApplicationFactory.class);
		});
	}

	@Test
	public void cloudfoundry() {
		this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.withPropertyValues("VCAP_APPLICATION:{}")
			.run((context) -> {
				assertThat(context).hasSingleBean(CloudFoundryMetadataContributor.class);
				assertThat(context).getBean(ApplicationFactory.class)
					.isInstanceOf(CloudFoundryApplicationFactory.class);
			});
	}

	@Test
	public void cloudfoundry_sba_disabled() {
		this.contextRunner.withPropertyValues("VCAP_APPLICATION:{}").run((context) -> {
			assertThat(context).doesNotHaveBean(CloudFoundryMetadataContributor.class);
			assertThat(context).doesNotHaveBean(ApplicationFactory.class);
		});
	}

}
