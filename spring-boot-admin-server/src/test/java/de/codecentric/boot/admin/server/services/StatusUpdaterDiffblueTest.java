package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {StatusUpdater.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class StatusUpdaterDiffblueTest {
  @MockitoBean
  private ApiMediaTypeHandler apiMediaTypeHandler;

  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private InstanceWebClient instanceWebClient;

  @Autowired
  private StatusUpdater statusUpdater;

  /**
   * Test {@link StatusUpdater#timeout(Duration)}.
   * <p>
   * Method under test: {@link StatusUpdater#timeout(Duration)}
   */
  @Test
  public void testTimeout() {
    // Arrange
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(mock(Builder.class)).build();
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    assertSame(statusUpdater, statusUpdater.timeout(null));
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus() {
    // Arrange
    Mono<Instance> mono = mock(Mono.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateStatusResult = statusUpdater.updateStatus(InstanceId.of("42"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualUpdateStatusResult);
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus2() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus3() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    SnapshottingInstanceRepository repository = new SnapshottingInstanceRepository(new InMemoryEventStore());
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(flux.collectList()).thenReturn(justResult);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Mono<List<InstanceEvent>> mono = mock(Mono.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
    verify(mono).filter(isA(Predicate.class));
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#map(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus_givenMonoMapReturnJustData_thenCallsMap() throws AssertionError {
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
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(statusUpdater.updateStatus(InstanceId.of("42")));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
    verify(mono2).filter(isA(Predicate.class));
    verify(mono).map(isA(Function.class));
  }

  /**
   * Test {@link StatusUpdater#updateStatus(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#then()} return {@code null}.</li>
   *   <li>Then return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#updateStatus(InstanceId)}
   */
  @Test
  public void testUpdateStatus_givenMonoThenReturnNull_thenReturnNull() {
    // Arrange
    Mono<Instance> mono = mock(Mono.class);
    when(mono.then()).thenReturn(null);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateStatusResult = statusUpdater.updateStatus(InstanceId.of("4242"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertNull(actualUpdateStatusResult);
  }

  /**
   * Test {@link StatusUpdater#handleError(Throwable)}.
   * <ul>
   *   <li>Given {@link Throwable#Throwable()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#handleError(Throwable)}
   */
  @Test
  public void testHandleError_givenThrowable() throws AssertionError {
    // Arrange
    AbstractMethodError abstractMethodError = new AbstractMethodError("message");
    abstractMethodError.addSuppressed(new Throwable());

    // Act and Assert
    FirstStep<StatusInfo> createResult = StepVerifier
        .create(statusUpdater.handleError(new Throwable("message", abstractMethodError)));
    createResult.assertNext(s -> {
      StatusInfo statusInfo = s;
      Map<String, Object> details = statusInfo.getDetails();
      assertEquals(2, details.size());
      Object getResult = details.get("exception");
      assertEquals("java.lang.Throwable", getResult);
      assertEquals("message", details.get("message"));
      assertEquals("OFFLINE", statusInfo.getStatus());
      assertFalse(statusInfo.isDown());
      assertTrue(statusInfo.isOffline());
      assertFalse(statusInfo.isUnknown());
      assertFalse(statusInfo.isUp());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link StatusUpdater#handleError(Throwable)}.
   * <ul>
   *   <li>When {@link Throwable#Throwable()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#handleError(Throwable)}
   */
  @Test
  public void testHandleError_whenThrowable() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<StatusInfo> createResult = StepVerifier.create(statusUpdater.handleError(new Throwable()));
    createResult.assertNext(s -> {
      StatusInfo statusInfo = s;
      Map<String, Object> details = statusInfo.getDetails();
      assertEquals(2, details.size());
      Object getResult = details.get("exception");
      assertEquals("java.lang.Throwable", getResult);
      assertNull(details.get("message"));
      assertEquals("OFFLINE", statusInfo.getStatus());
      assertFalse(statusInfo.isDown());
      assertTrue(statusInfo.isOffline());
      assertFalse(statusInfo.isUnknown());
      assertFalse(statusInfo.isUp());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link StatusUpdater#handleError(Throwable)}.
   * <ul>
   *   <li>When {@link Throwable#Throwable(String, Throwable)} with {@code message} and {@link Throwable#Throwable()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusUpdater#handleError(Throwable)}
   */
  @Test
  public void testHandleError_whenThrowableWithMessageAndThrowable() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<StatusInfo> createResult = StepVerifier
        .create(statusUpdater.handleError(new Throwable("message", new Throwable())));
    createResult.assertNext(s -> {
      StatusInfo statusInfo = s;
      Map<String, Object> details = statusInfo.getDetails();
      assertEquals(2, details.size());
      Object getResult = details.get("exception");
      assertEquals("java.lang.Throwable", getResult);
      assertEquals("message", details.get("message"));
      assertEquals("OFFLINE", statusInfo.getStatus());
      assertFalse(statusInfo.isDown());
      assertTrue(statusInfo.isOffline());
      assertFalse(statusInfo.isUnknown());
      assertFalse(statusInfo.isUp());
      return;
    }).expectComplete().verify();
  }
}
