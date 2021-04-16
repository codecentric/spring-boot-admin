/*
 * Copyright 2014-2019 the original author or authors.
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

import com.netflix.discovery.EurekaClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.kubernetes.client.discovery.KubernetesInformerDiscoveryClient;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesDiscoveryClient;

import de.codecentric.boot.admin.server.cloud.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.KubernetesServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.ServiceInstanceConverter;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AdminServerDiscoveryAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(UtilAutoConfiguration.class,
					ClientHttpConnectorAutoConfiguration.class, WebClientAutoConfiguration.class,
					AdminServerAutoConfiguration.class, AdminServerDiscoveryAutoConfiguration.class))
			.withUserConfiguration(AdminServerMarkerConfiguration.class);

	@Test
	public void defaultServiceInstanceConverter() {
		this.contextRunner.withUserConfiguration(SimpleDiscoveryClientAutoConfiguration.class)
				.run((context) -> assertThat(context.getBean(ServiceInstanceConverter.class))
						.isInstanceOf(DefaultServiceInstanceConverter.class));
	}

	@Test
	public void eurekaServiceInstanceConverter() {
		this.contextRunner.withBean(EurekaClient.class, () -> mock(EurekaClient.class))
				.withBean(DiscoveryClient.class, () -> mock(DiscoveryClient.class)).run((context) -> assertThat(context)
						.getBean(ServiceInstanceConverter.class).isInstanceOf(EurekaServiceInstanceConverter.class));
	}

	@Test
	public void officialKubernetesServiceInstanceConverter() {
		this.contextRunner
				.withBean(KubernetesInformerDiscoveryClient.class, () -> mock(KubernetesInformerDiscoveryClient.class))
				.run((context) -> assertThat(context).getBean(ServiceInstanceConverter.class)
						.isInstanceOf(KubernetesServiceInstanceConverter.class));
	}

	@Test
	public void fabric8KubernetesServiceInstanceConverter() {
		this.contextRunner.withBean(KubernetesDiscoveryClient.class, () -> mock(KubernetesDiscoveryClient.class))
				.run((context) -> assertThat(context).getBean(ServiceInstanceConverter.class)
						.isInstanceOf(KubernetesServiceInstanceConverter.class));
	}

	@Test
	public void customServiceInstanceConverter() {
		this.contextRunner.withUserConfiguration(SimpleDiscoveryClientAutoConfiguration.class)
				.withBean(CustomServiceInstanceConverter.class).run((context) -> assertThat(context)
						.getBean(ServiceInstanceConverter.class).isInstanceOf(CustomServiceInstanceConverter.class));
	}

	public static class CustomServiceInstanceConverter implements ServiceInstanceConverter {

		@Override
		public Registration convert(ServiceInstance instance) {
			return null;
		}

	}

}
