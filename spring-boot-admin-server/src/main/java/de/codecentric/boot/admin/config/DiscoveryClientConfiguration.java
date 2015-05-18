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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

@Configuration
@ConditionalOnClass({ DiscoveryClient.class })
@ConditionalOnProperty(prefix = "spring.boot.admin.discovery", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({ NoopDiscoveryClientAutoConfiguration.class })
public  class DiscoveryClientConfiguration {

	@Value("${spring.boot.admin.discovery.management.context-path:}")
	private String managementPath;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private ApplicationRegistry registry;

	@Bean
	ApplicationListener<ApplicationEvent> applicationDiscoveryListener() {
		ApplicationDiscoveryListener listener = new ApplicationDiscoveryListener(discoveryClient, registry);
		listener.setManagementContextPath(managementPath);
		return listener;
	}
}
