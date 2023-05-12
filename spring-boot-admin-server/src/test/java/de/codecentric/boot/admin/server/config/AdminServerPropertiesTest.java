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

package de.codecentric.boot.admin.server.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(AdminServerProperties.class)
@TestPropertySource("classpath:server-config-test.properties")
public class AdminServerPropertiesTest {

	@Autowired
	private AdminServerProperties serverConfig;

	@Test
	void testLoadConfigurationProperties() {
		assertThat(serverConfig.getContextPath()).isEqualTo("/admin");

		assertThat(serverConfig.getInstanceAuth().getDefaultUserName()).isEqualTo("admin");
		assertThat(serverConfig.getInstanceAuth().getDefaultPassword()).isEqualTo("topsecret");

		assertThat(serverConfig.getInstanceAuth().getServiceMap().get("my-service").getUserName()).isEqualTo("me");
		assertThat(serverConfig.getInstanceAuth().getServiceMap().get("my-service").getUserPassword())
			.isEqualTo("secret");
	}

}
