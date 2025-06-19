package de.codecentric.boot.admin.server.cloud.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.client.RefreshInstancesEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Subscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.cloud.kubernetes.commons.discovery.DefaultKubernetesServiceInstance;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
class InstanceDiscoveryListenerDiffblueTest {
  @Mock
  private DiscoveryClient discoveryClient;

  @InjectMocks
  private InstanceDiscoveryListener instanceDiscoveryListener;

  @Mock
  private InstanceRegistry instanceRegistry;

  @Mock
  private InstanceRepository instanceRepository;

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady3() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady4() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 59L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given ArrayList() add DefaultServiceInstance(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenArrayListAddDefaultServiceInstance_thenCallsFindAll() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given InMemoryEventStore findAll() return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenInMemoryEventStoreFindAllReturnFromIterableArrayList() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRegistry#register(Registration)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given InstanceRegistry register(Registration) return 'null'; then calls register(Registration)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenInstanceRegistryRegisterReturnNull_thenCallsRegister() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}.
   * <ul>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationReady(ApplicationReadyEvent)}
   */
  @Test
  @DisplayName("Test onApplicationReady(ApplicationReadyEvent); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationReady_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);
    Class<Object> forNameResult = Object.class;
    SpringApplication application = new SpringApplication(forNameResult);

    // Act
    instanceDiscoveryListener.onApplicationReady(new ApplicationReadyEvent(application, new String[]{"Args"},
        new AnnotationConfigReactiveWebApplicationContext(), null));

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered3() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered4() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered5() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 59L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given ArrayList() add DefaultServiceInstance()")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenArrayListAddDefaultServiceInstance() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given InstanceRegistry register(Registration) return 'null'")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenInstanceRegistryRegisterReturnNull() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}.
   * <ul>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onInstanceRegistered(InstanceRegisteredEvent)}
   */
  @Test
  @DisplayName("Test onInstanceRegistered(InstanceRegisteredEvent); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testOnInstanceRegistered_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onInstanceRegistered(new InstanceRegisteredEvent<>("Source", "Config"));

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances3() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances4() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 59L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given ArrayList() add DefaultServiceInstance(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenArrayListAddDefaultServiceInstance_thenCallsFindAll() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given InMemoryEventStore findAll() return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenInMemoryEventStoreFindAllReturnFromIterableArrayList() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRegistry#register(Registration)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given InstanceRegistry register(Registration) return 'null'; then calls register(Registration)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenInstanceRegistryRegisterReturnNull_thenCallsRegister() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}.
   * <ul>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onRefreshInstances(RefreshInstancesEvent)}
   */
  @Test
  @DisplayName("Test onRefreshInstances(RefreshInstancesEvent); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testOnRefreshInstances_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onRefreshInstances(new RefreshInstancesEvent("Source"));

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat3() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given ArrayList() add DefaultServiceInstance(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenArrayListAddDefaultServiceInstance_thenCallsFindAll() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given InMemoryEventStore findAll() return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenInMemoryEventStoreFindAllReturnFromIterableArrayList() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenInstanceIdWithValueIs42_thenCallsFindAll2() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRegistry#register(Registration)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given InstanceRegistry register(Registration) return 'null'; then calls register(Registration)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenInstanceRegistryRegisterReturnNull_thenCallsRegister() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onParentHeartbeat(ParentHeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onParentHeartbeat(ParentHeartbeatEvent); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testOnParentHeartbeat_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onParentHeartbeat(new ParentHeartbeatEvent("Source", "Value"));

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent3() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given ArrayList() add DefaultServiceInstance(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenArrayListAddDefaultServiceInstance_thenCallsFindAll() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given InMemoryEventStore findAll() return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenInMemoryEventStoreFindAllReturnFromIterableArrayList() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenInstanceIdWithValueIs42_thenCallsFindAll2() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRegistry#register(Registration)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given InstanceRegistry register(Registration) return 'null'; then calls register(Registration)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenInstanceRegistryRegisterReturnNull_thenCallsRegister() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}.
   * <ul>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#onApplicationEvent(HeartbeatEvent)}
   */
  @Test
  @DisplayName("Test onApplicationEvent(HeartbeatEvent); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testOnApplicationEvent_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository);

    // Act
    instanceDiscoveryListener.onApplicationEvent(new HeartbeatEvent("Source", "State"));

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover()")
  @Tag("MaintainedByDiffblue")
  void testDiscover() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover()")
  @Tag("MaintainedByDiffblue")
  void testDiscover2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList
        .add(new DefaultServiceInstance("42", "42", "Discovering new instances from DiscoveryClient", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover()")
  @Tag("MaintainedByDiffblue")
  void testDiscover3() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 59L));
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    // Act
    new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository).discover();

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given ArrayList() add DefaultServiceInstance(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenArrayListAddDefaultServiceInstance_thenCallsFindAll() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance());
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link DiscoveryClient#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given ArrayList() add 'null'; then calls getInstances(String)")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenArrayListAddNull_thenCallsGetInstances() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@code null}.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given ArrayList() add 'null'; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenArrayListAddNull_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    serviceInstanceList.add(null);
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given Flux groupBy(Function) return fromIterable ArrayList(); then calls groupBy(Function)")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    // Act
    new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository).discover();

    // Assert
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return create three and {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given InMemoryEventStore findAll() return create three and 'true'")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenInMemoryEventStoreFindAllReturnCreateThreeAndTrue() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
    when(eventStore.findAll()).thenReturn(createResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    // Act
    new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository).discover();

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given InMemoryEventStore findAll() return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenInMemoryEventStoreFindAllReturnFromIterableArrayList() {
    // Arrange
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    // Act
    new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository).discover();

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given InstanceId with value is '42'; then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenInstanceIdWithValueIs42_thenCallsFindAll() {
    // Arrange
    ArrayList<InstanceEvent> it = new ArrayList<>();
    it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
    InMemoryEventStore eventStore = mock(InMemoryEventStore.class);
    when(eventStore.findAll()).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    // Act
    new InstanceDiscoveryListener(discoveryClient,
        new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
        repository).discover();

    // Assert
    verify(eventStore).findAll();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return just {@link InstanceId} with value is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given InstanceRegistry register(Registration) return just InstanceId with value is '42'")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenInstanceRegistryRegisterReturnJustInstanceIdWithValueIs42() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link InstanceRegistry} {@link InstanceRegistry#register(Registration)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRegistry#register(Registration)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given InstanceRegistry register(Registration) return 'null'; then calls register(Registration)")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenInstanceRegistryRegisterReturnNull_thenCallsRegister() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(null);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#subscribe(Subscriber)} does nothing.</li>
   *   <li>Then calls {@link Mono#subscribe(Subscriber)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); given Mono subscribe(Subscriber) does nothing; then calls subscribe(Subscriber)")
  @Tag("MaintainedByDiffblue")
  void testDiscover_givenMonoSubscribeDoesNothing_thenCallsSubscribe() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");

    ArrayList<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(new DefaultServiceInstance("42", "42", "localhost", 8080, true));
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(serviceInstanceList);
    when(discoveryClient.getServices()).thenReturn(stringList);
    Mono<InstanceId> mono = mock(Mono.class);
    doNothing().when(mono).subscribe(Mockito.<Subscriber<InstanceId>>any());
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(mono);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
    verify(mono).subscribe(isA(Subscriber.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testDiscover_thenCallsFindAll() {
    // Arrange
    when(discoveryClient.getServices()).thenReturn(new ArrayList<>());
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#discover()}.
   * <ul>
   *   <li>Then calls {@link InstanceRepository#findAll()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#discover()}
   */
  @Test
  @DisplayName("Test discover(); then calls findAll()")
  @Tag("MaintainedByDiffblue")
  void testDiscover_thenCallsFindAll2() {
    // Arrange
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("Discovering new instances from DiscoveryClient");
    when(discoveryClient.getInstances(Mockito.<String>any())).thenReturn(new ArrayList<>());
    when(discoveryClient.getServices()).thenReturn(stringList);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRepository.findAll()).thenReturn(fromIterableResult);

    // Act
    instanceDiscoveryListener.discover();

    // Assert
    verify(instanceRepository).findAll();
    verify(discoveryClient).getInstances(eq("Discovering new instances from DiscoveryClient"));
    verify(discoveryClient).getServices();
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "", "localhost", 8080, true)));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance2() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier
        .create(instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42",
            "Converting service '{}' running at '{}' with metadata {}", 8080, true)));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance3() throws AssertionError {
    // Arrange
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    String value = "42";
    InstanceId ofResult = InstanceId.of(value);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(ofResult);
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
        generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.assertNext(i -> {
      assertSame(ofResult, i);
      return;
    }).expectComplete().verify();
    verify(generator).generateId(isA(Registration.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link EventsourcingInstanceRepository#EventsourcingInstanceRepository(InstanceEventStore)} with eventStore is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given EventsourcingInstanceRepository(InstanceEventStore) with eventStore is 'null'")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenEventsourcingInstanceRepositoryWithEventStoreIsNull() throws AssertionError {
    // Arrange
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(null), generator,
        mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectComplete().verify();
    verify(generator).generateId(isA(Registration.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given Flux collectList() return just ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenFluxCollectListReturnJustArrayList() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(flux.collectList()).thenReturn(justResult);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(generator).generateId(isA(Registration.class));
    verify(flux).collectList();
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link InstanceDiscoveryListener}.</li>
   *   <li>When {@link DefaultServiceInstance#DefaultServiceInstance()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given InstanceDiscoveryListener; when DefaultServiceInstance()")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenInstanceDiscoveryListener_whenDefaultServiceInstance() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier
        .create(instanceDiscoveryListener.registerInstance(new DefaultServiceInstance()));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link InstanceEventStore} {@link InstanceEventStore#find(InstanceId)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given InstanceEventStore find(InstanceId) return fromIterable ArrayList()")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenInstanceEventStoreFindReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(generator).generateId(isA(Registration.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link InstanceIdGenerator} {@link InstanceIdGenerator#generateId(Registration)} return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given InstanceIdGenerator generateId(Registration) return 'null'")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenInstanceIdGeneratorGenerateIdReturnNull() throws AssertionError {
    // Arrange
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(null);
    InstanceRegistry registry = new InstanceRegistry(mock(InstanceRepository.class), generator,
        mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectComplete().verify();
    verify(generator).generateId(isA(Registration.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link InstanceRepository} {@link InstanceRepository#compute(InstanceId, BiFunction)} return {@code null}.</li>
   *   <li>Then calls {@link InstanceRepository#compute(InstanceId, BiFunction)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given InstanceRepository compute(InstanceId, BiFunction) return 'null'; then calls compute(InstanceId, BiFunction)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenInstanceRepositoryComputeReturnNull_thenCallsCompute() throws AssertionError {
    // Arrange
    InstanceRepository repository = mock(InstanceRepository.class);
    when(repository.compute(Mockito.<InstanceId>any(), Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any()))
        .thenReturn(null);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectComplete().verify();
    verify(repository).compute(isA(InstanceId.class), isA(BiFunction.class));
    verify(generator).generateId(isA(Registration.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given Mono filter(Predicate) return just ArrayList(); then calls filter(Predicate)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Mono<List<InstanceEvent>> mono = mock(Mono.class);
    Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
    when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(generator).generateId(isA(Registration.class));
    verify(flux).collectList();
    verify(mono).filter(isA(Predicate.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#flatMap(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#flatMap(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given Mono flatMap(Function) return just 'Data'; then calls flatMap(Function)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenMonoFlatMapReturnJustData_thenCallsFlatMap() throws AssertionError {
    // Arrange
    Mono<Object> mono = mock(Mono.class);
    Mono<Object> justResult = Mono.just("Data");
    when(mono.flatMap(Mockito.<Function<Object, Mono<Object>>>any())).thenReturn(justResult);
    Mono<List<InstanceEvent>> mono2 = mock(Mono.class);
    when(mono2.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(mono);
    Mono<List<InstanceEvent>> mono3 = mock(Mono.class);
    when(mono3.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono2);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono3);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(generator).generateId(isA(Registration.class));
    verify(flux).collectList();
    verify(mono3).filter(isA(Predicate.class));
    verify(mono).flatMap(isA(Function.class));
    verify(mono2).map(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); given Mono map(Function) return just 'Data'; then calls filter(Predicate)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_givenMonoMapReturnJustData_thenCallsFilter() throws AssertionError {
    // Arrange
    Mono<List<InstanceEvent>> mono = mock(Mono.class);
    Mono<Object> justResult = Mono.just("Data");
    when(mono.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(justResult);
    Mono<List<InstanceEvent>> mono2 = mock(Mono.class);
    when(mono2.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono);
    Flux<InstanceEvent> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono2);
    InstanceEventStore eventStore = mock(InstanceEventStore.class);
    when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
    InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
    when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
    InstanceRegistry registry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));

    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    FirstStep<InstanceId> createResult = StepVerifier.create(
        instanceDiscoveryListener.registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
    createResult.expectError().verify();
    verify(eventStore).find(isA(InstanceId.class));
    verify(generator).generateId(isA(Registration.class));
    verify(flux).collectList();
    verify(mono2).filter(isA(Predicate.class));
    verify(mono).map(isA(Function.class));
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Then calls {@link ServiceInstanceConverter#convert(ServiceInstance)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); then calls convert(ServiceInstance)")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_thenCallsConvert() {
    // Arrange
    InstanceRegistry registry = mock(InstanceRegistry.class);
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(registry.register(Mockito.<Registration>any())).thenReturn(justResult);
    ServiceInstanceConverter converter = mock(ServiceInstanceConverter.class);
    Registration buildResult = Registration.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .source("Source")
        .build();
    when(converter.convert(Mockito.<ServiceInstance>any())).thenReturn(buildResult);
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());

    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));
    instanceDiscoveryListener.setConverter(converter);

    // Act
    Mono<InstanceId> actualRegisterInstanceResult = instanceDiscoveryListener
        .registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true));

    // Assert
    verify(converter).convert(isA(ServiceInstance.class));
    verify(registry).register(isA(Registration.class));
    assertSame(justResult, actualRegisterInstanceResult);
  }

  /**
   * Test {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}.
   * <ul>
   *   <li>Then return just {@link InstanceId} with value is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#registerInstance(ServiceInstance)}
   */
  @Test
  @DisplayName("Test registerInstance(ServiceInstance); then return just InstanceId with value is '42'")
  @Tag("MaintainedByDiffblue")
  void testRegisterInstance_thenReturnJustInstanceIdWithValueIs42() {
    // Arrange
    Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
    when(instanceRegistry.register(Mockito.<Registration>any())).thenReturn(justResult);

    // Act
    Mono<InstanceId> actualRegisterInstanceResult = instanceDiscoveryListener
        .registerInstance(new DefaultServiceInstance("42", "42", "localhost", 8080, true));

    // Assert
    verify(instanceRegistry).register(isA(Registration.class));
    assertSame(justResult, actualRegisterInstanceResult);
  }

  /**
   * Test {@link InstanceDiscoveryListener#toString(ServiceInstance)} with {@code ServiceInstance}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#toString(ServiceInstance)}
   */
  @Test
  @DisplayName("Test toString(ServiceInstance) with 'ServiceInstance'")
  @Tag("MaintainedByDiffblue")
  void testToStringWithServiceInstance() {
    //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
    //   Run dcover create --keep-partial-tests to gain insights into why
    //   a non-Spring test was created.

    // Arrange
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act and Assert
    assertEquals("serviceId=42, instanceId=42, url= https://localhost:8080",
        instanceDiscoveryListener.toString(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
  }

  /**
   * Test {@link InstanceDiscoveryListener#toString(ServiceInstance)} with {@code ServiceInstance}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#toString(ServiceInstance)}
   */
  @Test
  @DisplayName("Test toString(ServiceInstance) with 'ServiceInstance'")
  @Tag("MaintainedByDiffblue")
  void testToStringWithServiceInstance2() {
    //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
    //   Run dcover create --keep-partial-tests to gain insights into why
    //   a non-Spring test was created.

    // Arrange
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    DefaultServiceInstance instance = new DefaultServiceInstance("42", "42", "localhost", 8080, true);
    instance.setSecure(false);

    // Act and Assert
    assertEquals("serviceId=42, instanceId=42, url= http://localhost:8080",
        instanceDiscoveryListener.toString(instance));
  }

  /**
   * Test {@link InstanceDiscoveryListener#toString(ServiceInstance)} with {@code ServiceInstance}.
   * <p>
   * Method under test: {@link InstanceDiscoveryListener#toString(ServiceInstance)}
   */
  @Test
  @DisplayName("Test toString(ServiceInstance) with 'ServiceInstance'")
  @Tag("MaintainedByDiffblue")
  void testToStringWithServiceInstance3() {
    //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
    //   Run dcover create --keep-partial-tests to gain insights into why
    //   a non-Spring test was created.

    // Arrange
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));
    HashMap<String, String> metadata = new HashMap<>();

    // Act and Assert
    assertEquals("serviceId=42, instanceId=42, url= https://localhost:8080",
        instanceDiscoveryListener.toString(new DefaultKubernetesServiceInstance("42", "42", "localhost", 8080, metadata,
            true, "https", "https", new HashMap<>())));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link InstanceDiscoveryListener#setConverter(ServiceInstanceConverter)}
   *   <li>{@link InstanceDiscoveryListener#setIgnoredInstancesMetadata(Map)}
   *   <li>{@link InstanceDiscoveryListener#setIgnoredServices(Set)}
   *   <li>{@link InstanceDiscoveryListener#setInstancesMetadata(Map)}
   *   <li>{@link InstanceDiscoveryListener#setServices(Set)}
   *   <li>{@link InstanceDiscoveryListener#getIgnoredInstancesMetadata()}
   *   <li>{@link InstanceDiscoveryListener#getIgnoredServices()}
   *   <li>{@link InstanceDiscoveryListener#getInstancesMetadata()}
   *   <li>{@link InstanceDiscoveryListener#getServices()}
   * </ul>
   */
  @Test
  @DisplayName("Test getters and setters")
  @Tag("MaintainedByDiffblue")
  void testGettersAndSetters() {
    // Arrange
    CompositeDiscoveryClient discoveryClient = new CompositeDiscoveryClient(new ArrayList<>());
    InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore(3)),
        mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

    InstanceDiscoveryListener instanceDiscoveryListener = new InstanceDiscoveryListener(discoveryClient, registry,
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)));

    // Act
    instanceDiscoveryListener.setConverter(mock(ServiceInstanceConverter.class));
    HashMap<String, String> ignoredInstancesMetadata = new HashMap<>();
    instanceDiscoveryListener.setIgnoredInstancesMetadata(ignoredInstancesMetadata);
    HashSet<String> ignoredServices = new HashSet<>();
    instanceDiscoveryListener.setIgnoredServices(ignoredServices);
    HashMap<String, String> instancesMetadata = new HashMap<>();
    instanceDiscoveryListener.setInstancesMetadata(instancesMetadata);
    HashSet<String> services = new HashSet<>();
    instanceDiscoveryListener.setServices(services);
    Map<String, String> actualIgnoredInstancesMetadata = instanceDiscoveryListener.getIgnoredInstancesMetadata();
    Set<String> actualIgnoredServices = instanceDiscoveryListener.getIgnoredServices();
    Map<String, String> actualInstancesMetadata = instanceDiscoveryListener.getInstancesMetadata();
    Set<String> actualServices = instanceDiscoveryListener.getServices();

    // Assert
    assertTrue(actualIgnoredInstancesMetadata.isEmpty());
    assertTrue(actualInstancesMetadata.isEmpty());
    assertTrue(actualIgnoredServices.isEmpty());
    assertTrue(actualServices.isEmpty());
    assertSame(ignoredInstancesMetadata, actualIgnoredInstancesMetadata);
    assertSame(instancesMetadata, actualInstancesMetadata);
    assertSame(ignoredServices, actualIgnoredServices);
    assertSame(services, actualServices);
  }
}
