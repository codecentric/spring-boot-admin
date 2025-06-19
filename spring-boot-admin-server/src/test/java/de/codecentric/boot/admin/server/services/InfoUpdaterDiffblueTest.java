package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper.HeadersWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {InfoUpdater.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class InfoUpdaterDiffblueTest {
  @MockitoBean
  private ApiMediaTypeHandler apiMediaTypeHandler;

  @Autowired
  private InfoUpdater infoUpdater;

  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private InstanceWebClient instanceWebClient;

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo() {
    // Arrange
    Mono<Instance> mono = mock(Mono.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateInfoResult = infoUpdater.updateInfo(InstanceId.of("42"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualUpdateInfoResult);
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo2() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo3() throws AssertionError {
    // Arrange
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    SnapshottingInstanceRepository repository = new SnapshottingInstanceRepository(new InMemoryEventStore());
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(builder).build();
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code 42}.</li>
   *   <li>When {@link InstanceId} with value is {@code 4242}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenArrayListAdd42_whenInstanceIdWithValueIs4242() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<>();
    objectList.addAll(new ArrayList<>());
    objectList.add("42");

    ArrayList<Object> it = new ArrayList<>();
    it.addAll(objectList);
    Flux<?> source = Flux.fromIterable(it);
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    Mono<Instance> mono = mock(Mono.class);
    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateInfoResult = infoUpdater.updateInfo(InstanceId.of("4242"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualUpdateInfoResult);
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} addAll {@link ArrayList#ArrayList()}.</li>
   *   <li>When {@link InstanceId} with value is {@code 4242}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenArrayListAddAllArrayList_whenInstanceIdWithValueIs4242() {
    // Arrange
    ArrayList<Object> it = new ArrayList<>();
    it.addAll(new ArrayList<>());
    Flux<?> source = Flux.fromIterable(it);
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    Mono<Instance> mono = mock(Mono.class);
    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateInfoResult = infoUpdater.updateInfo(InstanceId.of("4242"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualUpdateInfoResult);
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
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
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
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
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectComplete().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
    verify(mono).filter(isA(Predicate.class));
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#map(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_givenMonoMapReturnJustData_thenCallsMap() throws AssertionError {
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
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(infoUpdater.updateInfo(InstanceId.of("42")));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(builder).build();
    verify(flux).collectList();
    verify(mono2).filter(isA(Predicate.class));
    verify(mono).map(isA(Function.class));
  }

  /**
   * Test {@link InfoUpdater#updateInfo(InstanceId)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 4242}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#updateInfo(InstanceId)}
   */
  @Test
  public void testUpdateInfo_whenInstanceIdWithValueIs4242() {
    // Arrange
    Mono<Instance> mono = mock(Mono.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    ChannelSendOperator<Object> channelSendOperator = new ChannelSendOperator<>(source, mock(Function.class));

    when(mono.then()).thenReturn(channelSendOperator);
    when(instanceRepository.computeIfPresent(Mockito.<InstanceId>any(),
        Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any())).thenReturn(mono);

    // Act
    Mono<Void> actualUpdateInfoResult = infoUpdater.updateInfo(InstanceId.of("4242"));

    // Assert
    verify(instanceRepository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
    verify(mono).then();
    assertSame(channelSendOperator, actualUpdateInfoResult);
  }

  /**
   * Test {@link InfoUpdater#convertInfo(Instance, Throwable)} with {@code instance}, {@code ex}.
   * <p>
   * Method under test: {@link InfoUpdater#convertInfo(Instance, Throwable)}
   */
  @Test
  public void testConvertInfoWithInstanceEx() {
    // Arrange, Act and Assert
    assertTrue(infoUpdater.convertInfo(null, new Throwable()).getValues().isEmpty());
  }

  /**
   * Test {@link InfoUpdater#convertInfo(Instance, ClientResponse)} with {@code instance}, {@code response}.
   * <ul>
   *   <li>Given just {@link HashMap#HashMap()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoUpdater#convertInfo(Instance, ClientResponse)}
   */
  @Test
  public void testConvertInfoWithInstanceResponse_givenJustHashMap() throws AssertionError {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
    when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());
    new ChannelSendOperator<>(new HazelcastEventStore(3, eventLog), mock(Function.class));

    MediaType mediaType = mock(MediaType.class);
    when(mediaType.isCompatibleWith(Mockito.<MediaType>any())).thenReturn(true);
    Optional<MediaType> ofResult = Optional.of(mediaType);
    HeadersWrapper headersWrapper = mock(HeadersWrapper.class);
    when(headersWrapper.contentType()).thenReturn(ofResult);
    ClientResponseWrapper delegate = mock(ClientResponseWrapper.class);
    Mono<Map<String, Object>> justResult = Mono.just(new HashMap<>());
    when(delegate.bodyToMono(Mockito.<ParameterizedTypeReference<Map<String, Object>>>any())).thenReturn(justResult);
    when(delegate.headers()).thenReturn(headersWrapper);
    when(delegate.statusCode()).thenReturn(HttpStatus.OK);

    // Act and Assert
    FirstStep<Info> createResult = StepVerifier
        .create(infoUpdater.convertInfo(null, new ClientResponseWrapper(delegate)));
    createResult.assertNext(i -> {
      assertTrue(i.getValues().isEmpty());
      return;
    }).expectComplete().verify();
    verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
    verify(mediaType).isCompatibleWith(isA(MediaType.class));
    verify(delegate).bodyToMono(isA(ParameterizedTypeReference.class));
    verify(delegate).headers();
    verify(delegate).statusCode();
    verify(headersWrapper).contentType();
  }
}
