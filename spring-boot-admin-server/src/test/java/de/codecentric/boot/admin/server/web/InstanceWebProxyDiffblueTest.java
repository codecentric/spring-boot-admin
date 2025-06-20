package de.codecentric.boot.admin.server.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.web.InstanceWebProxy.ForwardRequest;
import de.codecentric.boot.admin.server.web.InstanceWebProxy.InstanceResponse;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.util.ArrayList;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class InstanceWebProxyDiffblueTest {

	/**
	 * Test {@link InstanceWebProxy#forward(Flux, ForwardRequest)} with {@code instances},
	 * {@code forwardRequest}.
	 * <p>
	 * Method under test: {@link InstanceWebProxy#forward(Flux, ForwardRequest)}
	 */
	@Test
	public void testForwardWithInstancesForwardRequest() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		InstanceWebProxy instanceWebProxy = new InstanceWebProxy(instanceWebClient);
		Flux<Instance> instances = Flux.fromIterable(new ArrayList<>());
		HttpMethod method = HttpMethod.valueOf("https://example.org/example");

		// Act and Assert
		FirstStep<InstanceResponse> createResult = StepVerifier
			.create(instanceWebProxy.forward(instances, new ForwardRequest(PagerdutyNotifier.DEFAULT_URI, method,
					new HttpHeaders(), mock(BodyInserter.class))));
		createResult.expectComplete().verify();
		verify(builder).build();
	}

}
