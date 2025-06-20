package de.codecentric.boot.admin.server.ui.web.reactive;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import java.util.ArrayList;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class HomepageForwardingFilterDiffblueTest {

	/**
	 * Test {@link HomepageForwardingFilter#filter(ServerWebExchange, WebFilterChain)}.
	 * <p>
	 * Method under test:
	 * {@link HomepageForwardingFilter#filter(ServerWebExchange, WebFilterChain)}
	 */
	@Test
	@DisplayName("Test filter(ServerWebExchange, WebFilterChain)")
	@Tag("MaintainedByDiffblue")
	void testFilter() {
		// Arrange
		ArrayList<String> routesIncludes = new ArrayList<>();
		HomepageForwardingFilter homepageForwardingFilter = new HomepageForwardingFilter(
				new HomepageForwardingFilterConfig("Homepage", routesIncludes, new ArrayList<>()));
		ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
		when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
		ServerWebExchangeDecorator exchange = mock(ServerWebExchangeDecorator.class);
		when(exchange.getRequest()).thenReturn(new ServerHttpRequestDecorator(delegate));
		doNothing().when(exchange).addUrlTransformer(Mockito.<Function<String, String>>any());
		exchange.addUrlTransformer(mock(Function.class));
		WebFilterChain chain = mock(WebFilterChain.class);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

		when(chain.filter(Mockito.<ServerWebExchange>any())).thenReturn(channelSendOperator);

		// Act
		Mono<Void> actualFilterResult = homepageForwardingFilter.filter(exchange, chain);

		// Assert
		verify(delegate).getMethod();
		verify(exchange).addUrlTransformer(isA(Function.class));
		verify(exchange).getRequest();
		verify(chain).filter(isA(ServerWebExchange.class));
		assertSame(channelSendOperator, actualFilterResult);
	}

}
