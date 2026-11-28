/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.mcp.config;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Invokes every advertised MCP tool against a stub target and verifies that, with write
 * tools disabled, only GET requests reach the monitored application.
 */
class McpWriteToolsDisabledTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	@BeforeEach
	void setUp() {
		this.wireMock.start();
		this.wireMock.stubFor(any(anyUrl()).willReturn(okJson("{}")));
		Instance instance = Instance.create(InstanceId.of("id-app"))
			.register(Registration.create("app", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());
		this.instanceRepository = mock(InstanceRepository.class);
		when(this.instanceRepository.findByName(anyString())).thenReturn(Flux.just(instance));
		when(this.instanceRepository.find(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.just(instance));
		when(this.instanceRepository.findAll()).thenReturn(Flux.just(instance));
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	@ParameterizedTest
	@ValueSource(strings = { "spring.boot.admin.mcp.tools.operations=false", "spring.boot.admin.mcp.read-only=true" })
	void writeToolsDisabled_noToolSendsNonGetRequest(String property) {
		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class))
			.withBean(InstanceRepository.class, () -> this.instanceRepository)
			.withBean(InstanceWebClient.Builder.class, () -> InstanceWebClient.builder().filter(rewriteEndpointUrl()))
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", property)
			.run((context) -> {
				List<String> invoked = new ArrayList<>();
				for (String beanName : context.getBeanDefinitionNames()) {
					Object bean = context.getBean(beanName);
					for (Method method : bean.getClass().getDeclaredMethods()) {
						McpTool tool = method.getAnnotation(McpTool.class);
						if (tool == null) {
							continue;
						}
						((Mono<?>) method.invoke(bean, arguments(method))).block(Duration.ofSeconds(5));
						invoked.add(tool.name());
					}
				}
				assertThat(invoked).isNotEmpty();
				int total = this.wireMock.findAll(anyRequestedFor(anyUrl())).size();
				int gets = this.wireMock.findAll(getRequestedFor(anyUrl())).size();
				assertThat(total).isPositive().isEqualTo(gets);
			});
	}

	private static Object[] arguments(Method method) {
		Class<?>[] types = method.getParameterTypes();
		Object[] args = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			args[i] = (types[i] == String.class) ? "app" : null;
		}
		return args;
	}

}
