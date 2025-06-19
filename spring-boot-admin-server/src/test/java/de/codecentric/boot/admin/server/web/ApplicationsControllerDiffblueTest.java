package de.codecentric.boot.admin.server.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.Application.Builder;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {ApplicationsController.class})
@DisabledInAotMode
@RunWith(MockitoJUnitRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationsControllerDiffblueTest {
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @MockitoBean
  private ApplicationRegistry applicationRegistry;

  @Autowired
  private ApplicationsController applicationsController;

  /**
   * Test {@link ApplicationsController#ApplicationsController(ApplicationRegistry, ApplicationEventPublisher)}.
   * <p>
   * Method under test: {@link ApplicationsController#ApplicationsController(ApplicationRegistry, ApplicationEventPublisher)}
   */
  @Test
  public void testNewApplicationsController() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications2() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<GroupedFlux<Object, InstanceEvent>> flux2 = mock(Flux.class);
    when(flux2.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux);
    Flux<InstanceEvent> flux3 = mock(Flux.class);
    when(flux3.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux2);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux3);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).filter(isA(Predicate.class));
    verify(flux2).flatMap(isA(Function.class));
    verify(flux3).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter2() throws AssertionError {
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
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux4);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux2).filter(isA(Predicate.class));
    verify(flux).filter(isA(Predicate.class));
    verify(flux3).flatMap(isA(Function.class));
    verify(flux4).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#flatMap(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap() throws AssertionError {
    // Arrange
    Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(fromIterableResult);
    Flux<InstanceEvent> flux2 = mock(Flux.class);
    when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux2);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).flatMap(isA(Function.class));
    verify(flux2).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#flatMap(Function, int)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#flatMap(Function, int)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap2() throws AssertionError {
    // Arrange
    Flux<GroupedFlux<Object, Object>> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, Object>, Publisher<Object>>>any(), anyInt()))
        .thenReturn(fromIterableResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.groupBy(Mockito.<Function<Object, Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<Object> flux4 = mock(Flux.class);
    when(flux4.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux3);
    Flux<GroupedFlux<Object, InstanceEvent>> flux5 = mock(Flux.class);
    when(flux5.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux4);
    Flux<InstanceEvent> flux6 = mock(Flux.class);
    when(flux6.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux5);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux6);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux4).filter(isA(Predicate.class));
    verify(flux3).filter(isA(Predicate.class));
    verify(flux5).flatMap(isA(Function.class));
    verify(flux).flatMap(isA(Function.class), eq(2147483647));
    verify(flux6).groupBy(isA(Function.class));
    verify(flux2).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxGroupByReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, Object>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<Object, Object>>any())).thenReturn(fromIterableResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<GroupedFlux<Object, InstanceEvent>> flux4 = mock(Flux.class);
    when(flux4.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux3);
    Flux<InstanceEvent> flux5 = mock(Flux.class);
    when(flux5.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux4);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux5);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux4).flatMap(isA(Function.class));
    verify(flux5).groupBy(isA(Function.class));
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(eventStore).findAll();
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Then calls {@link ApplicationRegistry#getApplications()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_thenCallsGetApplications() throws AssertionError {
    // Arrange
    Flux<Application> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(applicationRegistry.getApplications()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(applicationsController.applications());
    createResult.expectComplete().verify();
    verify(applicationRegistry).getApplications();
  }

  /**
   * Test {@link ApplicationsController#applications()}.
   * <ul>
   *   <li>Then calls {@link InstanceRegistry#getInstances()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applications()}
   */
  @Test
  public void testApplications_thenCallsGetInstances() throws AssertionError {
    // Arrange
    InstanceRegistry instanceRegistry = mock(InstanceRegistry.class);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRegistry.getInstances()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(
        new ApplicationsController(new ApplicationRegistry(instanceRegistry, mock(InstanceEventPublisher.class)),
            mock(ApplicationEventPublisher.class)).applications());
    createResult.expectComplete().verify();
    verify(instanceRegistry).getInstances();
  }

  /**
   * Test {@link ApplicationsController#refreshApplications()}.
   * <p>
   * Method under test: {@link ApplicationsController#refreshApplications()}
   */
  @Test
  public void testRefreshApplications() {
    // Arrange
    doNothing().when(applicationEventPublisher).publishEvent(Mockito.<ApplicationEvent>any());

    // Act
    applicationsController.refreshApplications();

    // Assert
    verify(applicationEventPublisher).publishEvent(isA(ApplicationEvent.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication() throws AssertionError {
    // Arrange
    Builder builderResult = Application.builder();
    String s = "foo";
    BuildVersion buildVersion = BuildVersion.valueOf(s);
    Builder buildVersionResult = builderResult.buildVersion(buildVersion);
    Builder statusResult = buildVersionResult.instances(new ArrayList<>()).name("Name").status("Status");
    Application buildResult = statusResult
        .statusTimestamp(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant())
        .build();
    Mono<Application> justResult = Mono.just(buildResult);
    when(applicationRegistry.getApplication(Mockito.<String>any())).thenReturn(justResult);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier
        .create(applicationsController.application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      Application body = responseEntity.getBody();
      assertSame(buildVersion, body.getBuildVersion());
      assertTrue(body.getInstances().isEmpty());
      assertEquals("Name", body.getName());
      assertEquals("Status", body.getStatus());
      Instant statusTimestamp = body.getStatusTimestamp();
      assertEquals(0L, statusTimestamp.getEpochSecond());
      assertEquals(0, statusTimestamp.getNano());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.OK, statusCode);
      assertEquals(200, responseEntity.getStatusCodeValue());
      assertTrue(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(applicationRegistry).getApplication(eq("Name"));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication2() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication3() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Mono<List<Object>> justResult = Mono.just(new ArrayList<>());
    when(flux.collectList()).thenReturn(justResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<Object> flux4 = mock(Flux.class);
    when(flux4.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux3);
    Flux<GroupedFlux<Object, InstanceEvent>> flux5 = mock(Flux.class);
    when(flux5.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux4);
    Flux<InstanceEvent> flux6 = mock(Flux.class);
    when(flux6.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux5);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux6);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).collectList();
    verify(flux4).filter(isA(Predicate.class));
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux5).flatMap(isA(Function.class));
    verify(flux6).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<GroupedFlux<Object, InstanceEvent>> flux2 = mock(Flux.class);
    when(flux2.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux);
    Flux<InstanceEvent> flux3 = mock(Flux.class);
    when(flux3.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux2);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux3);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).filter(isA(Predicate.class));
    verify(flux2).flatMap(isA(Function.class));
    verify(flux3).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter2() throws AssertionError {
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
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux4);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux2).filter(isA(Predicate.class));
    verify(flux).filter(isA(Predicate.class));
    verify(flux3).flatMap(isA(Function.class));
    verify(flux4).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter3() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<GroupedFlux<Object, InstanceEvent>> flux4 = mock(Flux.class);
    when(flux4.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux3);
    Flux<InstanceEvent> flux5 = mock(Flux.class);
    when(flux5.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux4);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux5);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux).filter(isA(Predicate.class));
    verify(flux4).flatMap(isA(Function.class));
    verify(flux5).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#flatMap(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap() throws AssertionError {
    // Arrange
    Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(fromIterableResult);
    Flux<InstanceEvent> flux2 = mock(Flux.class);
    when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux2);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).flatMap(isA(Function.class));
    verify(flux2).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just {@code Data}.</li>
   *   <li>Then calls {@link Mono#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenMonoFilterReturnJustData_thenCallsFilter() throws AssertionError {
    // Arrange
    Mono<Object> mono = mock(Mono.class);
    Mono<Object> justResult = Mono.just("Data");
    when(mono.filter(Mockito.<Predicate<Object>>any())).thenReturn(justResult);
    Mono<List<Object>> mono2 = mock(Mono.class);
    when(mono2.map(Mockito.<Function<List<Object>, Object>>any())).thenReturn(mono);
    Flux<Object> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono2);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<Object> flux4 = mock(Flux.class);
    when(flux4.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux3);
    Flux<GroupedFlux<Object, InstanceEvent>> flux5 = mock(Flux.class);
    when(flux5.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux4);
    Flux<InstanceEvent> flux6 = mock(Flux.class);
    when(flux6.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux5);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux6);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.expectError().verify();
    verify(eventStore).findAll();
    verify(flux).collectList();
    verify(flux4).filter(isA(Predicate.class));
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux5).flatMap(isA(Function.class));
    verify(flux6).groupBy(isA(Function.class));
    verify(mono).filter(isA(Predicate.class));
    verify(mono2).map(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_givenMonoMapReturnJustData_thenCallsCollectList() throws AssertionError {
    // Arrange
    Mono<List<Object>> mono = mock(Mono.class);
    Mono<Object> justResult = Mono.just("Data");
    when(mono.map(Mockito.<Function<List<Object>, Object>>any())).thenReturn(justResult);
    Flux<Object> flux = mock(Flux.class);
    when(flux.collectList()).thenReturn(mono);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<Object> flux4 = mock(Flux.class);
    when(flux4.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux3);
    Flux<GroupedFlux<Object, InstanceEvent>> flux5 = mock(Flux.class);
    when(flux5.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux4);
    Flux<InstanceEvent> flux6 = mock(Flux.class);
    when(flux6.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux5);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux6);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.expectError().verify();
    verify(eventStore).findAll();
    verify(flux).collectList();
    verify(flux4).filter(isA(Predicate.class));
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux5).flatMap(isA(Function.class));
    verify(flux6).groupBy(isA(Function.class));
    verify(mono).map(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#application(String)}.
   * <ul>
   *   <li>Then calls {@link InstanceRegistry#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#application(String)}
   */
  @Test
  public void testApplication_thenCallsGetInstances() throws AssertionError {
    // Arrange
    InstanceRegistry instanceRegistry = mock(InstanceRegistry.class);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<ResponseEntity<Application>> createResult = StepVerifier.create(
        new ApplicationsController(new ApplicationRegistry(instanceRegistry, mock(InstanceEventPublisher.class)),
            mock(ApplicationEventPublisher.class)).application("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Application> responseEntity = r;
      assertNull(responseEntity.getBody());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Name"));
  }

  /**
   * Test {@link ApplicationsController#applicationsStream()}.
   * <ul>
   *   <li>Then calls {@link Flux#mergeWith(Publisher)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#applicationsStream()}
   */
  @Test
  public void testApplicationsStream_thenCallsMergeWith() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.mergeWith(Mockito.<Publisher<?>>any())).thenReturn(fromIterableResult);
    Flux<Application> flux2 = mock(Flux.class);
    when(flux2.map(Mockito.<Function<Application, Object>>any())).thenReturn(flux);
    when(applicationRegistry.getApplicationStream()).thenReturn(flux2);

    // Act and Assert
    FirstStep<ServerSentEvent<Application>> createResult = StepVerifier
        .create(applicationsController.applicationsStream());
    createResult.expectComplete().verify();
    verify(applicationRegistry).getApplicationStream();
    verify(flux2).map(isA(Function.class));
    verify(flux).mergeWith(isA(Publisher.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister2() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link ApplicationRegistry} {@link ApplicationRegistry#deregister(String)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenApplicationRegistryDeregisterReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    Flux<InstanceId> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(applicationRegistry.deregister(Mockito.<String>any())).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(applicationsController.unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(applicationRegistry).deregister(eq("Name"));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#collectList()} return just {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#collectList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
    // Arrange
    Flux<InstanceId> flux = mock(Flux.class);
    Mono<List<InstanceId>> justResult = Mono.just(new ArrayList<>());
    when(flux.collectList()).thenReturn(justResult);
    when(applicationRegistry.deregister(Mockito.<String>any())).thenReturn(flux);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(applicationsController.unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(applicationRegistry).deregister(eq("Name"));
    verify(flux).collectList();
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.filter(Mockito.<Predicate<Object>>any())).thenReturn(fromIterableResult);
    Flux<GroupedFlux<Object, InstanceEvent>> flux2 = mock(Flux.class);
    when(flux2.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux);
    Flux<InstanceEvent> flux3 = mock(Flux.class);
    when(flux3.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux2);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux3);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).filter(isA(Predicate.class));
    verify(flux2).flatMap(isA(Function.class));
    verify(flux3).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter2() throws AssertionError {
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
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux4);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux2).filter(isA(Predicate.class));
    verify(flux).filter(isA(Predicate.class));
    verify(flux3).flatMap(isA(Function.class));
    verify(flux4).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#filter(Predicate)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
    // Arrange
    Flux<Object> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<Object, Publisher<Object>>>any())).thenReturn(fromIterableResult);
    Flux<Object> flux2 = mock(Flux.class);
    when(flux2.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux);
    Flux<Object> flux3 = mock(Flux.class);
    when(flux3.filter(Mockito.<Predicate<Object>>any())).thenReturn(flux2);
    Flux<GroupedFlux<Object, InstanceEvent>> flux4 = mock(Flux.class);
    when(flux4.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(flux3);
    Flux<InstanceEvent> flux5 = mock(Flux.class);
    when(flux5.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux4);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux5);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux3).filter(isA(Predicate.class));
    verify(flux2).filter(isA(Predicate.class));
    verify(flux4).flatMap(isA(Function.class));
    verify(flux).flatMap(isA(Function.class));
    verify(flux5).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#flatMap(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap() throws AssertionError {
    // Arrange
    Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
    Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
        .thenReturn(fromIterableResult);
    Flux<InstanceEvent> flux2 = mock(Flux.class);
    when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux2);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).flatMap(isA(Function.class));
    verify(flux2).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Flux#groupBy(Function)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
    // Arrange
    Flux<InstanceEvent> flux = mock(Flux.class);
    Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    when(eventStore.findAll()).thenReturn(flux);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
    verify(flux).groupBy(isA(Function.class));
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()} return fromIterable {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
    // Arrange
    HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
    Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(eventStore.findAll()).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(new ApplicationsController(
        new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
            mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class)),
        mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(eventStore).findAll();
  }

  /**
   * Test {@link ApplicationsController#unregister(String)}.
   * <ul>
   *   <li>Then calls {@link InstanceRegistry#getInstances(String)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ApplicationsController#unregister(String)}
   */
  @Test
  public void testUnregister_thenCallsGetInstances() throws AssertionError {
    // Arrange
    InstanceRegistry instanceRegistry = mock(InstanceRegistry.class);
    Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
    when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);

    // Act and Assert
    FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(
        new ApplicationsController(new ApplicationRegistry(instanceRegistry, mock(InstanceEventPublisher.class)),
            mock(ApplicationEventPublisher.class)).unregister("Name"));
    createResult.assertNext(r -> {
      ResponseEntity<Void> responseEntity = r;
      assertNull(responseEntity.getBody());
      assertTrue(responseEntity.getHeaders().isEmpty());
      HttpStatusCode statusCode = responseEntity.getStatusCode();
      assertTrue(statusCode instanceof HttpStatus);
      assertEquals(HttpStatus.NOT_FOUND, statusCode);
      assertEquals(404, responseEntity.getStatusCodeValue());
      assertFalse(responseEntity.hasBody());
      return;
    }).expectComplete().verify();
    verify(instanceRegistry).getInstances(eq("Name"));
  }
}
