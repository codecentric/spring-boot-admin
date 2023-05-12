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

package de.codecentric.boot.admin.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

public class ClientServletApplicationTest extends AbstractClientApplicationTest {

	private ConfigurableApplicationContext instance;

	@BeforeEach
	@Override
	public void setUp() throws Exception {
		super.setUp();

		SpringApplication application = new SpringApplication(TestClientApplication.class);
		application.setWebApplicationType(WebApplicationType.SERVLET);
		instance = application.run("--spring.application.name=Test-Client", "--server.port=0",
				"--management.endpoints.web.base-path=/mgmt", "--endpoints.health.enabled=true",
				"--spring.boot.admin.client.url=" + wireMock.url("/"));
	}

	@AfterEach
	public void shutdown() {
		instance.close();
	}

	@Override
	protected int getServerPort() {
		return instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);
	}

	@Override
	protected int getManagementPort() {
		return instance.getEnvironment().getProperty("local.management.port", Integer.class, 0);

	}

}
