package de.codecentric.boot.admin.server.services.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.services.endpoints.ProbeEndpointsStrategy.DetectedEndpoint;
import de.codecentric.boot.admin.server.services.endpoints.ProbeEndpointsStrategy.EndpointDefinition;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class ProbeEndpointsStrategyDiffblueTest {
  /**
   * Test {@link ProbeEndpointsStrategy#convert(List)} with {@code endpoints}.
   * <ul>
   *   <li>Given {@link EndpointDefinition#EndpointDefinition(String, String)} with id is {@code 42} and {@code Path}.</li>
   *   <li>Then calls {@link Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ProbeEndpointsStrategy#convert(List)}
   */
  @Test
  public void testConvertWithEndpoints_givenEndpointDefinitionWithIdIs42AndPath_thenCallsBuild() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    ProbeEndpointsStrategy probeEndpointsStrategy = new ProbeEndpointsStrategy(instanceWebClient,
        new String[]{"https://config.us-east-2.amazonaws.com"});

    ArrayList<DetectedEndpoint> endpoints = new ArrayList<>();
    EndpointDefinition definition = new EndpointDefinition("42", "Path");

    String id = "42";
    String url = "https://example.org/example";
    Endpoint endpoint = Endpoint.of(id, url);
    endpoints.add(new DetectedEndpoint(definition, endpoint));

    // Act and Assert
    FirstStep<Endpoints> createResult = StepVerifier.create(probeEndpointsStrategy.convert(endpoints));
    createResult.assertNext(e -> {
      Endpoints endpoints2 = e;
      Iterator<Endpoint> iteratorResult = endpoints2.iterator();
      Endpoint nextResult = iteratorResult.next();
      assertFalse(iteratorResult.hasNext());
      assertEquals("42", nextResult.getId());
      assertSame(endpoint, nextResult);
      Stream<Endpoint> streamResult = endpoints2.stream();
      List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
      assertEquals(1, collectResult.size());
      assertSame(endpoint, collectResult.get(0));
      return;
    }).expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link ProbeEndpointsStrategy#convert(List)} with {@code endpoints}.
   * <ul>
   *   <li>Given {@link EndpointDefinition#EndpointDefinition(String, String)} with id is {@code 42} and {@code Path}.</li>
   *   <li>Then calls {@link Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ProbeEndpointsStrategy#convert(List)}
   */
  @Test
  public void testConvertWithEndpoints_givenEndpointDefinitionWithIdIs42AndPath_thenCallsBuild2()
      throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    ProbeEndpointsStrategy probeEndpointsStrategy = new ProbeEndpointsStrategy(instanceWebClient,
        new String[]{"https://config.us-east-2.amazonaws.com"});

    ArrayList<DetectedEndpoint> endpoints = new ArrayList<>();
    EndpointDefinition definition = new EndpointDefinition("42", "Path");

    String id = "42";
    String url = "https://example.org/example";
    Endpoint endpoint = Endpoint.of(id, url);
    endpoints.add(new DetectedEndpoint(definition, endpoint));
    EndpointDefinition definition2 = new EndpointDefinition("42", "Path");

    endpoints.add(new DetectedEndpoint(definition2, Endpoint.of("42", "https://example.org/example")));

    // Act and Assert
    FirstStep<Endpoints> createResult = StepVerifier.create(probeEndpointsStrategy.convert(endpoints));
    createResult.assertNext(e -> {
      Endpoints endpoints2 = e;
      Iterator<Endpoint> iteratorResult = endpoints2.iterator();
      Endpoint nextResult = iteratorResult.next();
      assertFalse(iteratorResult.hasNext());
      assertEquals("42", nextResult.getId());
      assertSame(endpoint, nextResult);
      Stream<Endpoint> streamResult = endpoints2.stream();
      List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
      assertEquals(1, collectResult.size());
      assertSame(endpoint, collectResult.get(0));
      return;
    }).expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link ProbeEndpointsStrategy#convert(List)} with {@code endpoints}.
   * <ul>
   *   <li>Given {@link Endpoint} with {@code Id} and url is {@code https://example.org/example}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ProbeEndpointsStrategy#convert(List)}
   */
  @Test
  public void testConvertWithEndpoints_givenEndpointWithIdAndUrlIsHttpsExampleOrgExample() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    ProbeEndpointsStrategy probeEndpointsStrategy = new ProbeEndpointsStrategy(instanceWebClient,
        new String[]{"https://config.us-east-2.amazonaws.com"});

    ArrayList<DetectedEndpoint> endpoints = new ArrayList<>();
    EndpointDefinition definition = new EndpointDefinition("42", "Path");

    String id = "Id";
    String url = "https://example.org/example";
    Endpoint endpoint = Endpoint.of(id, url);
    endpoints.add(new DetectedEndpoint(definition, endpoint));
    EndpointDefinition definition2 = new EndpointDefinition("https", "Path");

    String id2 = "42";
    String url2 = "https://example.org/example";
    Endpoint endpoint2 = Endpoint.of(id2, url2);
    endpoints.add(new DetectedEndpoint(definition2, endpoint2));

    // Act and Assert
    FirstStep<Endpoints> createResult = StepVerifier.create(probeEndpointsStrategy.convert(endpoints));
    createResult.assertNext(e -> {
      Endpoints endpoints2 = e;
      Iterator<Endpoint> iteratorResult = endpoints2.iterator();
      Endpoint nextResult = iteratorResult.next();
      Endpoint actualNextResult = iteratorResult.next();
      assertFalse(iteratorResult.hasNext());
      assertEquals("Id", nextResult.getId());
      assertSame(endpoint, nextResult);
      assertSame(endpoint2, actualNextResult);
      Stream<Endpoint> streamResult = endpoints2.stream();
      List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
      assertEquals(2, collectResult.size());
      assertSame(endpoint, collectResult.get(0));
      assertSame(endpoint2, collectResult.get(1));
      return;
    }).expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link ProbeEndpointsStrategy#convert(List)} with {@code endpoints}.
   * <ul>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ProbeEndpointsStrategy#convert(List)}
   */
  @Test
  public void testConvertWithEndpoints_whenArrayList_thenCallsBuild() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    ProbeEndpointsStrategy probeEndpointsStrategy = new ProbeEndpointsStrategy(instanceWebClient,
        new String[]{"https://config.us-east-2.amazonaws.com"});

    // Act and Assert
    FirstStep<Endpoints> createResult = StepVerifier.create(probeEndpointsStrategy.convert(new ArrayList<>()));
    createResult.expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link ProbeEndpointsStrategy#convert(InstanceId, EndpointDefinition, URI)} with {@code instanceId}, {@code endpointDefinition}, {@code uri}.
   * <ul>
   *   <li>Then calls {@link ClientResponse#releaseBody()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ProbeEndpointsStrategy#convert(InstanceId, EndpointDefinition, URI)}
   */
  @Test
  public void testConvertWithInstanceIdEndpointDefinitionUri_thenCallsReleaseBody() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    ProbeEndpointsStrategy probeEndpointsStrategy = new ProbeEndpointsStrategy(instanceWebClient,
        new String[]{"https://config.us-east-2.amazonaws.com"});
    InstanceId instanceId = InstanceId.of("42");

    // Act
    Function<ClientResponse, Mono<DetectedEndpoint>> actualConvertResult = probeEndpointsStrategy.convert(instanceId,
        new EndpointDefinition("42", "Path"), PagerdutyNotifier.DEFAULT_URI);
    ClientResponse delegate = mock(ClientResponse.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    when(delegate.releaseBody()).thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
    when(delegate.statusCode()).thenReturn(HttpStatus.OK);
    Mono<DetectedEndpoint> actualPublisher = actualConvertResult.apply(new ClientResponseWrapper(delegate));

    // Assert
    verify(delegate).releaseBody();
    verify(delegate).statusCode();
    verify(builder).build();
    FirstStep<DetectedEndpoint> createResult = StepVerifier.create(actualPublisher);
    createResult.expectError().verify();
  }
}
