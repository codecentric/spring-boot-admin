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

package de.codecentric.boot.admin.server.cloud.config;

import com.netflix.discovery.EurekaClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.client.discovery.KubernetesInformerDiscoveryClient;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.server.cloud.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.InstanceDiscoveryListener;
import de.codecentric.boot.admin.server.cloud.discovery.KubernetesServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.ServiceInstanceConverter;
import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

@Configuration(proxyBeanMethods = false)
@ConditionalOnSingleCandidate(DiscoveryClient.class)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = "spring.boot.admin.discovery", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter(value = AdminServerAutoConfiguration.class,
		name = { "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration",
				"org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration" })
public class AdminServerDiscoveryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "spring.boot.admin.discovery")
	public InstanceDiscoveryListener instanceDiscoveryListener(ServiceInstanceConverter serviceInstanceConverter,
			DiscoveryClient discoveryClient, InstanceRegistry registry, InstanceRepository repository) {
		InstanceDiscoveryListener listener = new InstanceDiscoveryListener(discoveryClient, registry, repository);
		listener.setConverter(serviceInstanceConverter);
		return listener;
	}

	@Bean
	@ConditionalOnMissingBean({ ServiceInstanceConverter.class })
	@ConfigurationProperties(prefix = "spring.boot.admin.discovery.converter")
	public DefaultServiceInstanceConverter serviceInstanceConverter() {
		return new DefaultServiceInstanceConverter();
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean({ ServiceInstanceConverter.class })
	@ConditionalOnBean(EurekaClient.class)
	public static class EurekaConverterConfiguration {

		@Bean
		@ConfigurationProperties(prefix = "spring.boot.admin.discovery.converter")
		public EurekaServiceInstanceConverter serviceInstanceConverter() {
			return new EurekaServiceInstanceConverter();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingBean({ ServiceInstanceConverter.class })
	@Conditional(KubernetesDiscoveryClientCondition.class)
	public static class KubernetesConverterConfiguration {

		@Bean
		@ConfigurationProperties(prefix = "spring.boot.admin.discovery.converter")
		public KubernetesServiceInstanceConverter serviceInstanceConverter(
				KubernetesDiscoveryProperties discoveryProperties) {
			return new KubernetesServiceInstanceConverter(discoveryProperties);
		}

	}

	private static class KubernetesDiscoveryClientCondition extends AnyNestedCondition {

		KubernetesDiscoveryClientCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnBean(KubernetesInformerDiscoveryClient.class)
		static class OfficialKubernetesCondition {

		}

		@ConditionalOnBean(KubernetesDiscoveryClient.class)
		static class Fabric8KubernetesCondition {

		}

	}

}
