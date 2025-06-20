package de.codecentric.boot.admin.server.web.client.reactive;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class CompositeReactiveHttpHeadersProviderDiffblueTest {

	/**
	 * Test {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}.
	 * <p>
	 * Method under test:
	 * {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<HttpHeaders> createResult = StepVerifier
			.create(new CompositeReactiveHttpHeadersProvider(new ArrayList<>()).getHeaders(null));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}.
	 * <ul>
	 * <li>Then calls {@link ReactiveHttpHeadersProvider#getHeaders(Instance)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders_thenCallsGetHeaders() throws AssertionError {
		// Arrange
		ReactiveHttpHeadersProvider reactiveHttpHeadersProvider = mock(ReactiveHttpHeadersProvider.class);
		Mono<HttpHeaders> justResult = Mono.just(new HttpHeaders());
		when(reactiveHttpHeadersProvider.getHeaders(Mockito.<Instance>any())).thenReturn(justResult);

		ArrayList<ReactiveHttpHeadersProvider> delegates = new ArrayList<>();
		delegates.add(reactiveHttpHeadersProvider);

		// Act and Assert
		FirstStep<HttpHeaders> createResult = StepVerifier
			.create(new CompositeReactiveHttpHeadersProvider(delegates).getHeaders(null));
		createResult.assertNext(h -> {
			assertTrue(h.isEmpty());
			return;
		}).expectComplete().verify();
		verify(reactiveHttpHeadersProvider).getHeaders(isNull());
	}

	/**
	 * Test {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}.
	 * <ul>
	 * <li>Then calls {@link ReactiveHttpHeadersProvider#getHeaders(Instance)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link CompositeReactiveHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders_thenCallsGetHeaders2() throws AssertionError {
		// Arrange
		ReactiveHttpHeadersProvider reactiveHttpHeadersProvider = mock(ReactiveHttpHeadersProvider.class);
		Mono<HttpHeaders> justResult = Mono.just(new HttpHeaders());
		when(reactiveHttpHeadersProvider.getHeaders(Mockito.<Instance>any())).thenReturn(justResult);
		ReactiveHttpHeadersProvider reactiveHttpHeadersProvider2 = mock(ReactiveHttpHeadersProvider.class);
		Mono<HttpHeaders> justResult2 = Mono.just(new HttpHeaders());
		when(reactiveHttpHeadersProvider2.getHeaders(Mockito.<Instance>any())).thenReturn(justResult2);

		ArrayList<ReactiveHttpHeadersProvider> delegates = new ArrayList<>();
		delegates.add(reactiveHttpHeadersProvider2);
		delegates.add(reactiveHttpHeadersProvider);

		// Act and Assert
		FirstStep<HttpHeaders> createResult = StepVerifier
			.create(new CompositeReactiveHttpHeadersProvider(delegates).getHeaders(null));
		createResult.assertNext(h -> {
			assertTrue(h.isEmpty());
			return;
		}).expectComplete().verify();
		verify(reactiveHttpHeadersProvider2).getHeaders(isNull());
		verify(reactiveHttpHeadersProvider).getHeaders(isNull());
	}

}
