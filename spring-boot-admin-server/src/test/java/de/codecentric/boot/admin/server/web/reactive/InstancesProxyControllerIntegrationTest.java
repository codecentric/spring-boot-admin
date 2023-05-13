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

package de.codecentric.boot.admin.server.web.reactive;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

import de.codecentric.boot.admin.server.AdminReactiveApplicationTest;
import de.codecentric.boot.admin.server.web.AbstractInstancesProxyControllerIntegrationTest;

public class InstancesProxyControllerIntegrationTest extends AbstractInstancesProxyControllerIntegrationTest {

	@Nullable
	private static ConfigurableApplicationContext context;

	@BeforeEach
	public void setUpClient() {
		context = new SpringApplicationBuilder().sources(AdminReactiveApplicationTest.TestAdminApplication.class)
			.web(WebApplicationType.REACTIVE)
			.run("--server.port=0", "--spring.boot.admin.monitor.default-timeout=2500");

		super.setUpClient(context);
	}

	@AfterEach
	public void tearDownContext() {
		if (context != null) {
			context.close();
		}
	}

}
