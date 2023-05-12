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

package de.codecentric.boot.admin.server.ui.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.SpringBootAdminServerEnabledCondition;
import de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AdminServerUiAutoConfigurationTest implements WithAssertions {

	@Test
	void testNormalizeHomepageUrl() {
		assertThat(AdminServerUiAutoConfiguration.normalizeHomepageUrl("/test/")).isEqualTo("/test");
	}

	@Nested
	public class ReactiveUiConfigurationTest {

		private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
			.withPropertyValues("--spring.boot.admin.ui.available-languages=de", "--spring.webflux.base-path=test")
			.withBean(AdminServerProperties.class)
			.withBean(WebFluxProperties.class)
			.withConfiguration(AutoConfigurations.of(AdminServerUiAutoConfiguration.class));

		@Mock
		WebFilterChain webFilterChain;

		@ParameterizedTest
		@CsvSource({ "/test/extensions/myextension", "/test/instances/1/actuator/heapdump",
				"/test/instances/1/actuator/logfile" })
		public void contextPathIsRespectedInExcludedRoutes(String routeExcludes) {
			MockServerHttpRequest serverHttpRequest = MockServerHttpRequest.get(routeExcludes)
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
				.build();

			ServerWebExchange serverWebExchange = spy(MockServerWebExchange.from(serverHttpRequest));

			this.contextRunner
				.withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
						AdminServerMarkerConfiguration.Marker.class)
				.run((context) -> {
					HomepageForwardingFilter bean = context.getBean(HomepageForwardingFilter.class);
					bean.filter(serverWebExchange, webFilterChain);

					verify(serverWebExchange, never()).mutate();
				});
		}

		@ParameterizedTest
		@CsvSource({ "/test/about", "/test/applications", "/test/instances", "/test/journal", "/test/wallboard",
				"/test/external" })
		public void contextPathIsRespectedInIncludedRoutes(String routeIncludes) {
			MockServerHttpRequest serverHttpRequest = MockServerHttpRequest.get(routeIncludes)
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
				.build();

			ServerWebExchange serverWebExchange = spy(MockServerWebExchange.from(serverHttpRequest));

			this.contextRunner
				.withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
						AdminServerMarkerConfiguration.Marker.class)
				.run((context) -> {
					HomepageForwardingFilter bean = context.getBean(HomepageForwardingFilter.class);
					bean.filter(serverWebExchange, webFilterChain);

					verify(serverWebExchange, atMostOnce()).mutate();
				});
		}

	}

	@Nested
	public class ServletUiConfiguration {

		private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withPropertyValues("--spring.boot.admin.ui.available-languages=de", "--spring.boot.admin.contextPath=test")
			.withBean(AdminServerProperties.class)
			.withConfiguration(AutoConfigurations.of(AdminServerUiAutoConfiguration.class));

		@ParameterizedTest
		@CsvSource({ "/test/extensions/myextension", "/test/instances/1/actuator/heapdump",
				"/test/instances/1/actuator/logfile" })
		public void contextPathIsRespectedInExcludedRoutes(String routeExcludes) {
			MockHttpServletRequest httpServletRequest = spy(new MockHttpServletRequest("GET", routeExcludes));
			httpServletRequest.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

			this.contextRunner
				.withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
						AdminServerMarkerConfiguration.Marker.class)
				.run((context) -> {
					de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter bean = context
						.getBean(de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter.class);
					bean.doFilter(httpServletRequest, mock(ServletResponse.class), mock(FilterChain.class));

					verify(httpServletRequest, never()).getRequestDispatcher(any());
				});
		}

		@ParameterizedTest
		@CsvSource({ "/test/about", "/test/applications", "/test/instances", "/test/journal", "/test/wallboard",
				"/test/external" })
		public void contextPathIsRespectedInIncludedRoutes(String routeIncludes) {
			MockHttpServletRequest httpServletRequest = spy(new MockHttpServletRequest("GET", routeIncludes));
			httpServletRequest.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

			this.contextRunner
				.withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
						AdminServerMarkerConfiguration.Marker.class)
				.run((context) -> {
					de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter bean = context
						.getBean(de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter.class);
					bean.doFilter(httpServletRequest, new MockHttpServletResponse(), mock(FilterChain.class));

					verify(httpServletRequest, atMostOnce()).getRequestDispatcher(any());
				});
		}

	}

}
