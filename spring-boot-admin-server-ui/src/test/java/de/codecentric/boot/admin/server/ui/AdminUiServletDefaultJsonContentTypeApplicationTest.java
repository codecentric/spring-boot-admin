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

package de.codecentric.boot.admin.server.ui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for #5662: SBA settings endpoint should be served as javascript even when Spring
 * MVC is configured with default content-type application/json.
 */
class AdminUiServletDefaultJsonContentTypeApplicationTest {

	private static ConfigurableApplicationContext instance;

	private static WebTestClient webClient;

	@BeforeAll
	static void setUp() {
		instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
			.web(WebApplicationType.SERVLET)
			.run("--server.port=0",
					"--spring.boot.admin.ui.extension-resource-locations=classpath:/META-INF/test-extensions/",
					"--spring.boot.admin.ui.available-languages=de");

		int port = instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);
		webClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@AfterAll
	static void shutdown() {
		if (instance != null) {
			instance.close();
		}
	}

	@Test
	void should_return_sba_settings_for_wildcard_accept_when_default_content_type_is_json() {
		webClient.get()
			.uri("/sba-settings.js")
			.accept(MediaType.ALL)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentTypeCompatibleWith("application/javascript")
			.expectBody(String.class)
			.value((body) -> assertThat(body).contains("\"availableLanguages\":[\"de\"]"));
	}

	@Test
	void should_return_sba_settings_without_accept_header_when_default_content_type_is_json() {
		webClient.get()
			.uri("/sba-settings.js")
			.headers((headers) -> headers.remove(HttpHeaders.ACCEPT))
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentTypeCompatibleWith("application/javascript")
			.expectBody(String.class)
			.value((body) -> assertThat(body).contains("\"availableLanguages\":[\"de\"]"));
	}

	@Test
	void should_not_return_sba_settings_for_json_accept_when_default_content_type_is_json() {
		webClient.get()
			.uri("/sba-settings.js")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.NOT_ACCEPTABLE);
	}

	@Test
	void should_return_index_when_default_content_type_is_json() {
		webClient.get()
			.uri("/")
			.accept(MediaType.TEXT_HTML)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.TEXT_HTML)
			.expectBody(String.class)
			.value((body) -> assertThat(body).contains("<title>Spring Boot Admin</title>"));
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	public static class TestAdminApplication {

		@Configuration(proxyBeanMethods = false)
		public static class SecurityConfiguration {

			@Bean
			protected SecurityFilterChain filterChain(HttpSecurity http) {
				return http.csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests((authz) -> authz.anyRequest().permitAll())
					.anonymous((config) -> config.principal("anonymousUser"))
					.build();
			}

			@Bean
			WebMvcConfigurer defaultJsonContentNegotiation() {
				return new WebMvcConfigurer() {
					@Override
					public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
						configurer.defaultContentType(MediaType.APPLICATION_JSON);
					}
				};
			}

		}

	}

}
