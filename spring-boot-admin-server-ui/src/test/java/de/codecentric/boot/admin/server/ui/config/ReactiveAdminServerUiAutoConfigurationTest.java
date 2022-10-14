package de.codecentric.boot.admin.server.ui.config;

import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.SpringBootAdminServerEnabledCondition;
import de.codecentric.boot.admin.server.ui.web.reactive.HomepageForwardingFilter;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public abstract class ReactiveAdminServerUiAutoConfigurationTest {

	protected abstract ReactiveWebApplicationContextRunner getContextRunner();

	@Mock
	WebFilterChain webFilterChain;

	@ParameterizedTest
	@CsvSource({ "/test/extensions/myextension", "/test/instances/1/actuator/heapdump",
			"/test/instances/1/actuator/logfile" })
	public void contextPathIsRespectedInExcludedRoutes(String routeExcludes) {
		MockServerHttpRequest serverHttpRequest = MockServerHttpRequest.get(routeExcludes)
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE).build();

		ServerWebExchange serverWebExchange = spy(MockServerWebExchange.from(serverHttpRequest));

		this.getContextRunner().withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
				AdminServerMarkerConfiguration.Marker.class).run((context) -> {
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
				.header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE).build();

		ServerWebExchange serverWebExchange = spy(MockServerWebExchange.from(serverHttpRequest));

		this.getContextRunner().withUserConfiguration(SpringBootAdminServerEnabledCondition.class,
				AdminServerMarkerConfiguration.Marker.class).run((context) -> {
					HomepageForwardingFilter bean = context.getBean(HomepageForwardingFilter.class);
					bean.filter(serverWebExchange, webFilterChain);

					verify(serverWebExchange).mutate();
				});
	}

}
