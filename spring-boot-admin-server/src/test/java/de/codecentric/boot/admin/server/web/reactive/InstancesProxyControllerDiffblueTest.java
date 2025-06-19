package de.codecentric.boot.admin.server.web.reactive;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.InstanceWebProxy;
import de.codecentric.boot.admin.server.web.InstanceWebProxy.InstanceResponse;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {InstancesProxyController.class, String.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class InstancesProxyControllerDiffblueTest {
  @MockitoBean
  private InstanceRegistry instanceRegistry;

  @MockitoBean
  private InstanceWebClient instanceWebClient;

  @Autowired
  private InstancesProxyController instancesProxyController;

  @Autowired
  private Set<String> set;

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest() throws AssertionError {
    // Arrange
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest2() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("https://example.org/example", "https://example.org/example");
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(httpHeaders);
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux).flatMap(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest3() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    Flux<DataBuffer> flux2 = mock(Flux.class);
    Flux<Object> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(flux2.map(Mockito.<Function<DataBuffer, Object>>any())).thenReturn(fromIterableResult2);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    when(delegate.getBody()).thenReturn(flux2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux).flatMap(isA(Function.class));
    verify(flux2).map(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest4() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    HashSet<String> ignoredHeaders = new HashSet<>();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        ignoredHeaders, new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest5() throws AssertionError {
    // Arrange
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        new HashSet<>(), registry, instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest6() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        new HashSet<>(), registry, instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest7() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    HashSet<String> ignoredHeaders = new HashSet<>();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        ignoredHeaders, new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Given create.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_givenCreate() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    DirectProcessor<DataBuffer> createResult = DirectProcessor.create();
    when(delegate.getBody()).thenReturn(createResult);

    // Act and Assert
    FirstStep<InstanceResponse> createResult2 = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult2.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux).flatMap(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return {@link Flux}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_givenFluxFilterReturnFlux() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<GroupedFlux<Object, InstanceEvent>> flux3 = mock(Flux.class);
    when(flux3.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux2);
    Flux<InstanceEvent> flux4 = mock(Flux.class);
    when(flux4.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux3);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.findAll()).thenReturn(flux4);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        new HashSet<>(), registry, instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
    verify(flux2).filter(isA(Predicate.class));
    verify(flux).filter(isA(Predicate.class));
    verify(flux3).flatMap(isA(Function.class));
    verify(flux4).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return {@link Flux}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_givenFluxGroupByReturnFlux() throws AssertionError {
    // Arrange
    Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(fromIterableResult);
    Flux<InstanceEvent> flux2 = mock(Flux.class);
    when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.findAll()).thenReturn(flux2);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        new HashSet<>(), registry, instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
    verify(flux).flatMap(isA(Function.class));
    verify(flux2).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Given {@link HttpHeaders#HttpHeaders()} add {@code Header Name} and {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_givenHttpHeadersAddHeaderNameAnd42() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Header Name", "42");
    httpHeaders.add("https://example.org/example", "https://example.org/example");
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(httpHeaders);
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux).flatMap(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Then calls {@link Flux#cache()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_thenCallsCache() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    Flux<Object> flux2 = mock(Flux.class);
    Flux<Object> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(flux2.cache()).thenReturn(fromIterableResult2);
    Flux<DataBuffer> flux3 = mock(Flux.class);
    when(flux3.map(Mockito.<Function<DataBuffer, Object>>any())).thenReturn(flux2);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    when(delegate.getBody()).thenReturn(flux3);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux2).cache();
    verify(flux).flatMap(isA(Function.class));
    verify(flux3).map(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<GroupedFlux<Object, InstanceEvent>> flux2 = mock(Flux.class);
    when(flux2.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux);
    Flux<InstanceEvent> flux3 = mock(Flux.class);
    when(flux3.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux2);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.findAll()).thenReturn(flux3);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InstancesProxyController instancesProxyController = new InstancesProxyController("Admin Context Path",
        new HashSet<>(), registry, instanceWebClient);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(builder).build();
    verify(flux).filter(isA(Predicate.class));
    verify(flux2).flatMap(isA(Function.class));
    verify(flux3).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)} with {@code applicationName}, {@code request}.
   * <ul>
   *   <li>Then calls {@link InstanceRegistry#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstancesProxyController#endpointProxy(String, ServerHttpRequest)}
   */
  @Test
  public void testEndpointProxyWithApplicationNameRequest_thenCallsGetInstances() throws AssertionError {
    // Arrange
    Flux<Instance> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);
    PathContainer pathContainer = mock(PathContainer.class);
    when(pathContainer.value()).thenReturn("https://example.org/example");
    RequestPath requestPath = mock(RequestPath.class);
    when(requestPath.pathWithinApplication()).thenReturn(pathContainer);
    ServerHttpRequestDecorator delegate = mock(ServerHttpRequestDecorator.class);
    when(delegate.getHeaders()).thenReturn(new HttpHeaders());
    when(delegate.getMethod()).thenReturn(HttpMethod.valueOf("https://example.org/example"));
    when(delegate.getURI()).thenReturn(PagerdutyNotifier.DEFAULT_URI);
    when(delegate.getPath()).thenReturn(requestPath);
    Flux<DataBuffer> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
    when(delegate.getBody()).thenReturn(fromIterableResult2);

    // Act and Assert
    FirstStep<InstanceResponse> createResult = StepVerifier
        .create(instancesProxyController.endpointProxy("Application Name", new ServerHttpRequestDecorator(delegate)));
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Application Name"));
    verify(pathContainer).value();
    verify(requestPath).pathWithinApplication();
    verify(delegate).getBody();
    verify(delegate).getHeaders();
    verify(delegate).getMethod();
    verify(delegate).getPath();
    verify(delegate).getURI();
    verify(flux).flatMap(isA(Function.class));
  }
}
