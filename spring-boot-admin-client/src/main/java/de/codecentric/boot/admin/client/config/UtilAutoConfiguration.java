/*
 * Copyright 2017 the original author or authors.
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
package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.utils.InetUtils;
import de.codecentric.boot.admin.client.utils.InetUtilsProperties;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.util.enabled", matchIfMissing = true)
@AutoConfigureOrder(0)
@EnableConfigurationProperties
public class UtilAutoConfiguration {

	@Bean
	public InetUtilsProperties inetUtilsProperties() {
		return new InetUtilsProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public InetUtils inetUtils(InetUtilsProperties properties) {
		return new InetUtils(properties);
	}
}
