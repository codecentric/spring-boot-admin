package de.codecentric.boot.admin.server.web.client;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.exception.ResolveInstanceException;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = { InstanceWebClient.Builder.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
public class InstanceWebClientDiffblueTest {

	@Autowired
	private InstanceWebClient.Builder builder;

	/**
	 * Test Builder
	 * {@link InstanceWebClient.Builder#filter(InstanceExchangeFilterFunction)}.
	 * <p>
	 * Method under test:
	 * {@link InstanceWebClient.Builder#filter(InstanceExchangeFilterFunction)}
	 */
	@Test
	public void testBuilderFilter() {
		// Arrange, Act and Assert
		assertSame(builder, builder.filter(mock(InstanceExchangeFilterFunction.class)));
	}

	/**
	 * Test Builder {@link InstanceWebClient.Builder#filters(Consumer)}.
	 * <p>
	 * Method under test: {@link InstanceWebClient.Builder#filters(Consumer)}
	 */
	@Test
	public void testBuilderFilters() {
		// Arrange
		Consumer<List<InstanceExchangeFilterFunction>> filtersConsumer = mock(Consumer.class);
		doNothing().when(filtersConsumer).accept(Mockito.<List<InstanceExchangeFilterFunction>>any());

		// Act
		InstanceWebClient.Builder actualFiltersResult = builder.filters(filtersConsumer);

		// Assert
		verify(filtersConsumer).accept(isA(List.class));
		assertSame(builder, actualFiltersResult);
	}

	/**
	 * Test {@link InstanceWebClient#instance(Instance)} with {@code Instance}.
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Instance)}
	 */
	@Test
	public void testInstanceWithInstance() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenThrow(new ResolveInstanceException("An error occurred"));
		Builder builder2 = mock(Builder.class);
		when(builder2.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any())).thenReturn(builder);
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder2);

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> new InstanceWebClient(webClient).instance((Instance) null));
		verify(webClient).mutate();
		verify(builder).build();
		verify(builder2).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Instance)} with {@code Instance}.
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Instance)}
	 */
	@Test
	public void testInstanceWithInstance2() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any()))
			.thenThrow(new ResolveInstanceException("An error occurred"));
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder);

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> new InstanceWebClient(webClient).instance((Instance) null));
		verify(webClient).mutate();
		verify(builder).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Instance)} with {@code Instance}.
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Instance)}
	 */
	@Test
	public void testInstanceWithInstance3() {
		// Arrange
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenThrow(new ResolveInstanceException("An error occurred"));

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> new InstanceWebClient(webClient).instance((Instance) null));
		verify(webClient).mutate();
	}

	/**
	 * Test {@link InstanceWebClient#instance(Instance)} with {@code Instance}.
	 * <ul>
	 * <li>Given {@link Builder} {@link Builder#build()} return {@link WebClient}.</li>
	 * <li>Then calls {@link Builder#build()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Instance)}
	 */
	@Test
	public void testInstanceWithInstance_givenBuilderBuildReturnWebClient_thenCallsBuild() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		Builder builder2 = mock(Builder.class);
		when(builder2.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any())).thenReturn(builder);
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder2);

		// Act
		new InstanceWebClient(webClient).instance((Instance) null);

		// Assert
		verify(webClient).mutate();
		verify(builder).build();
		verify(builder2).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Mono)} with {@code Mono}.
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Mono)}
	 */
	@Test
	public void testInstanceWithMono() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenThrow(new ResolveInstanceException("An error occurred"));
		Builder builder2 = mock(Builder.class);
		when(builder2.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any())).thenReturn(builder);
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder2);
		Builder builder3 = mock(Builder.class);
		when(builder3.build()).thenReturn(webClient);
		InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder3).build();

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> buildResult.instance(mock(Mono.class)));
		verify(webClient).mutate();
		verify(builder3).build();
		verify(builder).build();
		verify(builder2).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Mono)} with {@code Mono}.
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Mono)}
	 */
	@Test
	public void testInstanceWithMono2() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any()))
			.thenThrow(new ResolveInstanceException("An error occurred"));
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder);
		Builder webClient2 = mock(Builder.class);
		when(webClient2.build()).thenReturn(webClient);
		InstanceWebClient.Builder builder2 = mock(InstanceWebClient.Builder.class);
		when(builder2.webClient(Mockito.<Builder>any())).thenReturn(InstanceWebClient.builder(webClient2));
		InstanceWebClient buildResult = builder2.webClient(mock(Builder.class)).build();

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> buildResult.instance(mock(Mono.class)));
		verify(builder2).webClient(isA(Builder.class));
		verify(webClient).mutate();
		verify(webClient2).build();
		verify(builder).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Mono)} with {@code Mono}.
	 * <ul>
	 * <li>Given {@link Builder} {@link Builder#filters(Consumer)} return
	 * {@link Builder}.</li>
	 * <li>When {@link Mono}.</li>
	 * <li>Then calls {@link Builder#filters(Consumer)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Mono)}
	 */
	@Test
	public void testInstanceWithMono_givenBuilderFiltersReturnBuilder_whenMono_thenCallsFilters() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		Builder builder2 = mock(Builder.class);
		when(builder2.filters(Mockito.<Consumer<List<ExchangeFilterFunction>>>any())).thenReturn(builder);
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenReturn(builder2);
		Builder builder3 = mock(Builder.class);
		when(builder3.build()).thenReturn(webClient);
		InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder3).build();

		// Act
		buildResult.instance(mock(Mono.class));

		// Assert
		verify(webClient).mutate();
		verify(builder3).build();
		verify(builder).build();
		verify(builder2).filters(isA(Consumer.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Mono)} with {@code Mono}.
	 * <ul>
	 * <li>Given {@link InstanceWebClient.Builder}
	 * {@link InstanceWebClient.Builder#webClient(Builder)} return builder.</li>
	 * <li>Then calls {@link InstanceWebClient.Builder#webClient(Builder)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Mono)}
	 */
	@Test
	public void testInstanceWithMono_givenBuilderWebClientReturnBuilder_thenCallsWebClient() {
		// Arrange
		InstanceWebClient.Builder builder = mock(InstanceWebClient.Builder.class);
		when(builder.webClient(Mockito.<Builder>any())).thenReturn(InstanceWebClient.builder());
		InstanceWebClient buildResult = builder.webClient(mock(Builder.class)).build();

		// Act
		buildResult.instance(mock(Mono.class));

		// Assert
		verify(builder).webClient(isA(Builder.class));
	}

	/**
	 * Test {@link InstanceWebClient#instance(Mono)} with {@code Mono}.
	 * <ul>
	 * <li>Then calls {@link InstanceWebClient.Builder#build()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceWebClient#instance(Mono)}
	 */
	@Test
	public void testInstanceWithMono_thenCallsBuild() {
		// Arrange
		WebClient webClient = mock(WebClient.class);
		when(webClient.mutate()).thenThrow(new ResolveInstanceException("An error occurred"));
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(webClient);
		InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
		InstanceWebClient.Builder builder2 = mock(InstanceWebClient.Builder.class);
		when(builder2.build()).thenReturn(buildResult);
		InstanceWebClient.Builder builder3 = mock(InstanceWebClient.Builder.class);
		when(builder3.webClient(Mockito.<Builder>any())).thenReturn(builder2);
		InstanceWebClient buildResult2 = builder3.webClient(mock(Builder.class)).build();

		// Act and Assert
		assertThrows(ResolveInstanceException.class, () -> buildResult2.instance(mock(Mono.class)));
		verify(builder2).build();
		verify(builder3).webClient(isA(Builder.class));
		verify(webClient).mutate();
		verify(builder).build();
	}

}
