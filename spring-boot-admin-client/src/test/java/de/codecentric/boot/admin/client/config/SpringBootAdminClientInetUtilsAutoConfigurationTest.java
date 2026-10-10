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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.CloudFoundryApplicationFactory;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.InetUtilsReactiveApplicationFactory;
import de.codecentric.boot.admin.client.registration.InetUtilsServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.ReactiveApplicationFactory;
import de.codecentric.boot.admin.client.registration.ServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringBootAdminClientInetUtilsAutoConfigurationTest {

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void without_spring_cloud(WebApplicationType type) {
		contextRunner(type).withClassLoader(new FilteredClassLoader("org.springframework.cloud")).run((context) -> {
			assertThat(context).hasSingleBean(ApplicationFactory.class);
			assertThat(context.getBean(ApplicationFactory.class)).isExactlyInstanceOf(defaultFactoryType(type));
		});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void without_inetutils_bean(WebApplicationType type) {
		contextRunner(type).run((context) -> {
			assertThat(context).doesNotHaveBean(InetUtils.class);
			assertThat(context).hasSingleBean(ApplicationFactory.class);
			assertThat(context.getBean(ApplicationFactory.class)).isExactlyInstanceOf(defaultFactoryType(type));
		});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void with_inetutils_bean(WebApplicationType type) {
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class)).run((context) -> {
			assertThat(context).hasSingleBean(ApplicationFactory.class);
			assertThat(context.getBean(ApplicationFactory.class)).isExactlyInstanceOf(inetUtilsFactoryType(type));
		});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void with_spring_cloud_auto_configuration(WebApplicationType type) {
		contextRunner(type).withConfiguration(AutoConfigurations.of(UtilAutoConfiguration.class))
			.withPropertyValues("spring.cloud.inetutils.preferred-networks[0]=192.168.",
					"spring.cloud.inetutils.ignored-interfaces[0]=docker.*")
			.run((context) -> {
				assertThat(context).hasSingleBean(InetUtils.class);
				assertThat(context).hasSingleBean(ApplicationFactory.class);
				assertThat(context.getBean(ApplicationFactory.class)).isExactlyInstanceOf(inetUtilsFactoryType(type));
				InetUtilsProperties properties = context.getBean(InetUtilsProperties.class);
				assertThat(properties.getPreferredNetworks()).containsExactly("192.168.");
				assertThat(properties.getIgnoredInterfaces()).containsExactly("docker.*");
			});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void with_spring_cloud_util_disabled(WebApplicationType type) {
		contextRunner(type).withConfiguration(AutoConfigurations.of(UtilAutoConfiguration.class))
			.withPropertyValues("spring.cloud.util.enabled=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(InetUtils.class);
				assertThat(context).hasSingleBean(ApplicationFactory.class);
				assertThat(context.getBean(ApplicationFactory.class)).isExactlyInstanceOf(defaultFactoryType(type));
			});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void disabled_client(WebApplicationType type) {
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class))
			.withPropertyValues("spring.boot.admin.client.enabled=false")
			.run((context) -> assertThat(context).doesNotHaveBean(ApplicationFactory.class));
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void unconfigured_client(WebApplicationType type) {
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class))
			.withPropertyValues("spring.boot.admin.client.url=")
			.run((context) -> assertThat(context).doesNotHaveBean(ApplicationFactory.class));
	}

	@Test
	void non_web_application() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SpringBootAdminClientAutoConfiguration.class,
					SpringBootAdminClientInetUtilsAutoConfiguration.class, UtilAutoConfiguration.class))
			.withPropertyValues("spring.boot.admin.client.url=http://localhost:8081")
			.run((context) -> assertThat(context).doesNotHaveBean(ApplicationFactory.class));
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void custom_factory_takes_precedence(WebApplicationType type) {
		ApplicationFactory factory = mock(ApplicationFactory.class);
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class))
			.withBean(ApplicationFactory.class, () -> factory)
			.run((context) -> {
				assertThat(context).hasSingleBean(ApplicationFactory.class);
				assertThat(context.getBean(ApplicationFactory.class)).isSameAs(factory);
			});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void cloud_foundry_takes_precedence(WebApplicationType type) {
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class))
			.withPropertyValues("VCAP_APPLICATION:{}")
			.run((context) -> {
				assertThat(context).hasSingleBean(ApplicationFactory.class);
				assertThat(context.getBean(ApplicationFactory.class))
					.isExactlyInstanceOf(CloudFoundryApplicationFactory.class);
			});
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_metadata_contributors(WebApplicationType type) {
		contextRunner(type).withBean(InetUtils.class, () -> mock(InetUtils.class))
			.withBean(MetadataContributor.class, () -> () -> Map.of("custom", "contributor", "component", "test"))
			.withPropertyValues("spring.boot.admin.client.instance.service-url=https://service.example",
					"spring.boot.admin.client.instance.management-url=https://management.example",
					"spring.boot.admin.client.instance.health-url=https://health.example",
					"spring.boot.admin.client.instance.metadata.custom=instance")
			.run((context) -> assertThat(context.getBean(ApplicationFactory.class).createApplication().getMetadata())
				.containsEntry("component", "test")
				.containsEntry("custom", "instance"));
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void uses_primary_inetutils(WebApplicationType type) throws UnknownHostException {
		InetUtils primary = mock(InetUtils.class);
		when(primary.findFirstNonLoopbackAddress())
			.thenReturn(InetAddress.getByAddress(new byte[] { (byte) 192, 0, 2, 20 }));
		contextRunner(type).withBean("secondaryInetUtils", InetUtils.class, () -> mock(InetUtils.class))
			.withBean("primaryInetUtils", InetUtils.class, () -> primary, (definition) -> definition.setPrimary(true))
			.withPropertyValues("spring.boot.admin.client.instance.service-host-type=IP",
					"spring.boot.admin.client.instance.health-url=http://localhost:8080/actuator/health")
			.run((context) -> {
				DefaultApplicationFactory factory = context.getBean(DefaultApplicationFactory.class);
				WebServerInitializedEvent event = mock(WebServerInitializedEvent.class);
				WebServerApplicationContext applicationContext = mock(WebServerApplicationContext.class);
				WebServer webServer = mock(WebServer.class);
				when(applicationContext.getServerNamespace()).thenReturn("server");
				when(webServer.getPort()).thenReturn(8080);
				when(event.getApplicationContext()).thenReturn(applicationContext);
				when(event.getWebServer()).thenReturn(webServer);
				factory.onWebServerInitialized(event);
				assertThat(factory.createApplication().getServiceUrl()).isEqualTo("http://192.0.2.20:8080/");
			});
	}

	private AbstractApplicationContextRunner<?, ?, ?> contextRunner(WebApplicationType type) {
		AutoConfigurations configurations = AutoConfigurations.of(EndpointAutoConfiguration.class,
				WebEndpointAutoConfiguration.class, HttpClientAutoConfiguration.class,
				RestClientAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class,
				SpringBootAdminClientInetUtilsAutoConfiguration.class,
				SpringBootAdminClientCloudFoundryAutoConfiguration.class);
		if (type == WebApplicationType.SERVLET) {
			return new WebApplicationContextRunner().withConfiguration(configurations)
				.withConfiguration(AutoConfigurations.of(DispatcherServletAutoConfiguration.class))
				.withPropertyValues("spring.boot.admin.client.url=http://localhost:8081");
		}
		return new ReactiveWebApplicationContextRunner().withConfiguration(configurations)
			.withBean(WebFluxProperties.class)
			.withPropertyValues("spring.boot.admin.client.url=http://localhost:8081");
	}

	private Class<? extends ApplicationFactory> defaultFactoryType(WebApplicationType type) {
		return (type == WebApplicationType.SERVLET) ? ServletApplicationFactory.class
				: ReactiveApplicationFactory.class;
	}

	private Class<? extends ApplicationFactory> inetUtilsFactoryType(WebApplicationType type) {
		return (type == WebApplicationType.SERVLET) ? InetUtilsServletApplicationFactory.class
				: InetUtilsReactiveApplicationFactory.class;
	}

}
