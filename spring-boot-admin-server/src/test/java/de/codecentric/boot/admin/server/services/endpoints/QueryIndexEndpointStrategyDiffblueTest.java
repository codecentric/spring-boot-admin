package de.codecentric.boot.admin.server.services.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.ApiMediaTypeHandler;
import de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy.Response;
import de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy.Response.EndpointRef;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class QueryIndexEndpointStrategyDiffblueTest {

	/**
	 * Test {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}.
	 * <p>
	 * Method under test:
	 * {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}
	 */
	@Test
	public void testAlignWithManagementUrl() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		// Act
		Function<Endpoints, Endpoints> actualAlignWithManagementUrlResult = queryIndexEndpointStrategy
			.alignWithManagementUrl(InstanceId.of("42"), "https://example.org/example");
		Endpoints singleResult = Endpoints.single("42", "https://example.org/example");
		Endpoints actualApplyResult = actualAlignWithManagementUrlResult.apply(singleResult);

		// Assert
		verify(builder).build();
		assertSame(singleResult, actualApplyResult);
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}.
	 * <ul>
	 * <li>Then return apply empty is empty.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}
	 */
	@Test
	public void testAlignWithManagementUrl_thenReturnApplyEmptyIsEmpty() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		// Act
		Function<Endpoints, Endpoints> actualAlignWithManagementUrlResult = queryIndexEndpointStrategy
			.alignWithManagementUrl(InstanceId.of("42"), "https://example.org/example");
		Endpoints emptyResult = Endpoints.empty();
		Endpoints actualApplyResult = actualAlignWithManagementUrlResult.apply(emptyResult);

		// Assert
		verify(builder).build();
		assertSame(emptyResult, actualApplyResult);
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}.
	 * <ul>
	 * <li>Then return apply single {@code 42} and {@code http:} iterator next Id is
	 * {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}
	 */
	@Test
	public void testAlignWithManagementUrl_thenReturnApplySingle42AndHttpIteratorNextIdIs42() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		// Act
		Function<Endpoints, Endpoints> actualAlignWithManagementUrlResult = queryIndexEndpointStrategy
			.alignWithManagementUrl(InstanceId.of("42"), "https://example.org/example");
		Endpoints actualApplyResult = actualAlignWithManagementUrlResult.apply(Endpoints.single("42", "http:"));

		// Assert
		verify(builder).build();
		Iterator<Endpoint> iteratorResult = actualApplyResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		assertEquals("42", nextResult.getId());
		assertEquals("https:", nextResult.getUrl());
		Stream<Endpoint> streamResult = actualApplyResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(1, collectResult.size());
		assertFalse(iteratorResult.hasNext());
		assertSame(nextResult, collectResult.get(0));
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}.
	 * <ul>
	 * <li>When {@code Management Url}.</li>
	 * <li>Then return apply empty is empty.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link QueryIndexEndpointStrategy#alignWithManagementUrl(InstanceId, String)}
	 */
	@Test
	public void testAlignWithManagementUrl_whenManagementUrl_thenReturnApplyEmptyIsEmpty() {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		// Act
		Function<Endpoints, Endpoints> actualAlignWithManagementUrlResult = queryIndexEndpointStrategy
			.alignWithManagementUrl(InstanceId.of("42"), "Management Url");
		Endpoints emptyResult = Endpoints.empty();
		Endpoints actualApplyResult = actualAlignWithManagementUrlResult.apply(emptyResult);

		// Assert
		verify(builder).build();
		assertSame(emptyResult, actualApplyResult);
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#convertResponse(Response)}.
	 * <p>
	 * Method under test: {@link QueryIndexEndpointStrategy#convertResponse(Response)}
	 */
	@Test
	public void testConvertResponse() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		HashMap<String, EndpointRef> links = new HashMap<>();
		links.put("42", new EndpointRef("Href", true));
		links.putIfAbsent("foo", new EndpointRef("Href", true));

		Response response = new Response();
		response.setLinks(links);

		// Act and Assert
		FirstStep<Endpoints> createResult = StepVerifier.create(queryIndexEndpointStrategy.convertResponse(response));
		createResult.expectComplete().verify();
		verify(builder).build();
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#convertResponse(Response)}.
	 * <p>
	 * Method under test: {@link QueryIndexEndpointStrategy#convertResponse(Response)}
	 */
	@Test
	public void testConvertResponse2() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		HashMap<String, EndpointRef> links = new HashMap<>();
		links.put("42", new EndpointRef("Href", true));
		links.putIfAbsent("self", new EndpointRef("Href", true));

		Response response = new Response();
		response.setLinks(links);

		// Act and Assert
		FirstStep<Endpoints> createResult = StepVerifier.create(queryIndexEndpointStrategy.convertResponse(response));
		createResult.expectComplete().verify();
		verify(builder).build();
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#convertResponse(Response)}.
	 * <p>
	 * Method under test: {@link QueryIndexEndpointStrategy#convertResponse(Response)}
	 */
	@Test
	public void testConvertResponse3() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		HashMap<String, EndpointRef> links = new HashMap<>();
		links.put("42", new EndpointRef("Href", true));
		links.putIfAbsent("foo", new EndpointRef("Href", false));

		Response response = new Response();
		response.setLinks(links);

		// Act and Assert
		FirstStep<Endpoints> createResult = StepVerifier.create(queryIndexEndpointStrategy.convertResponse(response));
		createResult.assertNext(e -> {
			Endpoints endpoints = e;
			Iterator<Endpoint> iteratorResult = endpoints.iterator();
			Endpoint nextResult = iteratorResult.next();
			assertFalse(iteratorResult.hasNext());
			assertEquals("foo", nextResult.getId());
			assertEquals("Href", nextResult.getUrl());
			Stream<Endpoint> streamResult = endpoints.stream();
			assertEquals(1, streamResult.limit(5).collect(Collectors.toList()).size());
			return;
		}).expectComplete().verify();
		verify(builder).build();
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#convertResponse(Response)}.
	 * <ul>
	 * <li>Given {@link HashMap#HashMap()} {@code foo} is
	 * {@link EndpointRef#EndpointRef(String, boolean)} with {@code Href} and templated is
	 * {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link QueryIndexEndpointStrategy#convertResponse(Response)}
	 */
	@Test
	public void testConvertResponse_givenHashMapFooIsEndpointRefWithHrefAndTemplatedIsTrue() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		HashMap<String, EndpointRef> links = new HashMap<>();
		links.put("foo", new EndpointRef("Href", true));

		Response response = new Response();
		response.setLinks(links);

		// Act and Assert
		FirstStep<Endpoints> createResult = StepVerifier.create(queryIndexEndpointStrategy.convertResponse(response));
		createResult.expectComplete().verify();
		verify(builder).build();
	}

	/**
	 * Test {@link QueryIndexEndpointStrategy#convertResponse(Response)}.
	 * <ul>
	 * <li>Given {@link HashMap#HashMap()}.</li>
	 * <li>Then calls {@link Builder#build()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link QueryIndexEndpointStrategy#convertResponse(Response)}
	 */
	@Test
	public void testConvertResponse_givenHashMap_thenCallsBuild() throws AssertionError {
		// Arrange
		Builder builder = mock(Builder.class);
		when(builder.build()).thenReturn(mock(WebClient.class));
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
		QueryIndexEndpointStrategy queryIndexEndpointStrategy = new QueryIndexEndpointStrategy(instanceWebClient,
				new ApiMediaTypeHandler());

		Response response = new Response();
		response.setLinks(new HashMap<>());

		// Act and Assert
		FirstStep<Endpoints> createResult = StepVerifier.create(queryIndexEndpointStrategy.convertResponse(response));
		createResult.expectComplete().verify();
		verify(builder).build();
	}

	/**
	 * Test Response_EndpointRef {@link EndpointRef#EndpointRef(String, boolean)}.
	 * <p>
	 * Method under test: {@link EndpointRef#EndpointRef(String, boolean)}
	 */
	@Test
	public void testResponse_EndpointRefNewEndpointRef() {
		// Arrange and Act
		EndpointRef actualEndpointRef = new EndpointRef("Href", true);

		// Assert
		assertEquals("Href", actualEndpointRef.getHref());
		assertTrue(actualEndpointRef.isTemplated());
	}

}
