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
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.LegacyEndpointConverter;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerInstanceWebClientConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(
				AutoConfigurations.of(RestTemplateAutoConfiguration.class, ClientHttpConnectorAutoConfiguration.class,
						WebClientAutoConfiguration.class, WebMvcAutoConfiguration.class,
						AdminServerAutoConfiguration.class, AdminServerInstanceWebClientConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class);

	@Test
	public void simpleConfig() {
		this.contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(InstanceWebClient.Builder.class);
			assertThat(context).hasBean("filterInstanceWebClientCustomizer");
			assertThat(context).hasSingleBean(BasicAuthHttpHeaderProvider.class);
			assertThat(context).getBeanNames(InstanceExchangeFilterFunction.class)
				.containsExactly("addHeadersInstanceExchangeFilter", "rewriteEndpointUrlInstanceExchangeFilter",
						"setDefaultAcceptHeaderInstanceExchangeFilter", "legacyEndpointConverterInstanceExchangeFilter",
						"logfileAcceptWorkaround", "cookieHandlingInstanceExchangeFilter",
						"retryInstanceExchangeFilter", "timeoutInstanceExchangeFilter");
			assertThat(context).getBeanNames(LegacyEndpointConverter.class)
				.containsExactly("healthLegacyEndpointConverter", "infoLegacyEndpointConverter",
						"envLegacyEndpointConverter", "httptraceLegacyEndpointConverter",
						"threaddumpLegacyEndpointConverter", "liquibaseLegacyEndpointConverter",
						"flywayLegacyEndpointConverter", "beansLegacyEndpointConverter",
						"configpropsLegacyEndpointConverter", "mappingsLegacyEndpointConverter",
						"startupLegacyEndpointConverter");
		});
	}

}
