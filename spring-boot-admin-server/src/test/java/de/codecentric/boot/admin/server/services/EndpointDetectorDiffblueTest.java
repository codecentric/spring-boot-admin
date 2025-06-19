package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.services.endpoints.EndpointDetectionStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {EndpointDetector.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class EndpointDetectorDiffblueTest {
  @MockitoBean
  private EndpointDetectionStrategy endpointDetectionStrategy;

  @Autowired
  private EndpointDetector endpointDetector;

  @MockitoBean
  private InstanceRepository instanceRepository;

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints() {
    // Arrange
    Mono<Instance> mono = mock(Mono.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualDetectEndpointsResult = endpointDetector.detectEndpoints(InstanceId.of("42"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualDetectEndpointsResult);
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints2() throws AssertionError {
    // Arrange
    EndpointDetector endpointDetector = new EndpointDetector(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints3() throws AssertionError {
    // Arrange
    EndpointDetector endpointDetector = new EndpointDetector(
        new SnapshottingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(flux.collectList()).thenReturn(justResult);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
        mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(flux).collectList();
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
    EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
        mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Mono<List<InstanceEvent>> mono = mock(Mono.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
        mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(flux).collectList();
    verify(mono).filter(isA(Predicate.class));
  }

  /**
   * Test {@link EndpointDetector#detectEndpoints(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#map(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link EndpointDetector#detectEndpoints(InstanceId)}
   */
  @Test
  public void testDetectEndpoints_givenMonoMapReturnJustData_thenCallsMap() throws AssertionError {
    // Arrange
    Mono<List<InstanceEvent>> mono = mock(Mono.class);
    Mono<Object> justResult = Mono.just("Data");
    when(mono.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(justResult);
    Mono<List<InstanceEvent>> mono2 = mock(Mono.class);
    when(mono2.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono2);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
        mock(EndpointDetectionStrategy.class));

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(endpointDetector.detectEndpoints(InstanceId.of("42")));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(flux).collectList();
    verify(mono2).filter(isA(Predicate.class));
    verify(mono).map(isA(Function.class));
  }
}
