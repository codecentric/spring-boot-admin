package de.codecentric.boot.admin.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.ApiMediaTypeHandler;
import de.codecentric.boot.admin.server.services.EndpointDetector;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InfoUpdater;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.services.StatusUpdater;
import de.codecentric.boot.admin.server.services.endpoints.EndpointDetectionStrategy;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@RunWith(MockitoJUnitRunner.class)
public class AdminServerAutoConfigurationDiffblueTest {
  @InjectMocks
  private AdminServerAutoConfiguration adminServerAutoConfiguration;

  @InjectMocks
  private AdminServerProperties adminServerProperties;

  /**
   * Test {@link AdminServerAutoConfiguration#instanceRegistry(InstanceRepository, InstanceIdGenerator, InstanceFilter)}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#instanceRegistry(InstanceRepository, InstanceIdGenerator, InstanceFilter)}
   */
  @Test
  public void testInstanceRegistry() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Instance> createResult = StepVerifier.create(
        adminServerAutoConfiguration
            .instanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
                mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
            .getInstances());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#applicationRegistry(InstanceRegistry, InstanceEventPublisher)}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#applicationRegistry(InstanceRegistry, InstanceEventPublisher)}
   */
  @Test
  public void testApplicationRegistry() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Application> createResult = StepVerifier
        .create(
            adminServerAutoConfiguration
                .applicationRegistry(
                    new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
                        mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
                    mock(InstanceEventPublisher.class))
                .getApplications());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#instanceIdGenerator()}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#instanceIdGenerator()}
   */
  @Test
  public void testInstanceIdGenerator() {
    // Arrange and Act
    InstanceIdGenerator actualInstanceIdGeneratorResult = adminServerAutoConfiguration.instanceIdGenerator();
    Registration registration = Registration.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .source("Source")
        .build();
    InstanceId actualGenerateIdResult = actualInstanceIdGeneratorResult.generateId(registration);

    // Assert
    assertTrue(actualInstanceIdGeneratorResult instanceof HashingInstanceUrlIdGenerator);
    assertEquals("504149e8a3fa", actualGenerateIdResult.getValue());
    assertEquals("504149e8a3fa", actualGenerateIdResult.toString());
  }

  /**
   * Test {@link AdminServerAutoConfiguration#statusUpdater(InstanceRepository, Builder)}.
   * <ul>
   *   <li>Given {@link Builder} {@link Builder#build()} return {@link WebClient}.</li>
   *   <li>Then calls {@link InstanceWebClient.Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#statusUpdater(InstanceRepository, InstanceWebClient.Builder)}
   */
  @Test
  public void testStatusUpdater_givenBuilderBuildReturnWebClient_thenCallsBuild() {
    // Arrange
    EventsourcingInstanceRepository instanceRepository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
    InstanceWebClient.Builder instanceWebClientBulder = mock(InstanceWebClient.Builder.class);
    when(instanceWebClientBulder.build()).thenReturn(buildResult);

    // Act
    adminServerAutoConfiguration.statusUpdater(instanceRepository, instanceWebClientBulder);

    // Assert
    verify(instanceWebClientBulder).build();
    verify(builder).build();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#statusUpdateTrigger(StatusUpdater, Publisher)}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#statusUpdateTrigger(StatusUpdater, Publisher)}
   */
  @Test
  public void testStatusUpdateTrigger() {
    // Arrange
    AdminServerAutoConfiguration adminServerAutoConfiguration = new AdminServerAutoConfiguration(
        new AdminServerProperties());
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    StatusUpdater statusUpdater = new StatusUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    Flux<InstanceEvent> events = Flux.fromIterable(new ArrayList<>());

    // Act
    adminServerAutoConfiguration.statusUpdateTrigger(statusUpdater, events);

    // Assert
    verify(builder).build();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#endpointDetector(InstanceRepository, Builder)}.
   * <ul>
   *   <li>Given {@link Builder} {@link Builder#build()} return {@link WebClient}.</li>
   *   <li>Then calls {@link InstanceWebClient.Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#endpointDetector(InstanceRepository, InstanceWebClient.Builder)}
   */
  @Test
  public void testEndpointDetector_givenBuilderBuildReturnWebClient_thenCallsBuild() {
    // Arrange
    AdminServerAutoConfiguration adminServerAutoConfiguration = new AdminServerAutoConfiguration(
        new AdminServerProperties());
    EventsourcingInstanceRepository instanceRepository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
    InstanceWebClient.Builder instanceWebClientBuilder = mock(InstanceWebClient.Builder.class);
    when(instanceWebClientBuilder.build()).thenReturn(buildResult);

    // Act
    adminServerAutoConfiguration.endpointDetector(instanceRepository, instanceWebClientBuilder);

    // Assert
    verify(instanceWebClientBuilder).build();
    verify(builder).build();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#endpointDetectionTrigger(EndpointDetector, Publisher)}.
   * <ul>
   *   <li>Given randomUUID.</li>
   *   <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#endpointDetectionTrigger(EndpointDetector, Publisher)}
   */
  @Test
  public void testEndpointDetectionTrigger_givenRandomUUID_thenCallsAddEntryListener() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());
    EndpointDetector endpointDetector = new EndpointDetector(
        new EventsourcingInstanceRepository(new HazelcastEventStore(eventLogs)), mock(EndpointDetectionStrategy.class));

    Flux<InstanceEvent> events = Flux.fromIterable(new ArrayList<>());

    // Act
    adminServerAutoConfiguration.endpointDetectionTrigger(endpointDetector, events);

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
  }

  /**
   * Test {@link AdminServerAutoConfiguration#infoUpdater(InstanceRepository, Builder)}.
   * <ul>
   *   <li>Given {@link Builder} {@link Builder#build()} return {@link WebClient}.</li>
   *   <li>Then calls {@link InstanceWebClient.Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#infoUpdater(InstanceRepository, InstanceWebClient.Builder)}
   */
  @Test
  public void testInfoUpdater_givenBuilderBuildReturnWebClient_thenCallsBuild() {
    // Arrange
    EventsourcingInstanceRepository instanceRepository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
    InstanceWebClient.Builder instanceWebClientBuilder = mock(InstanceWebClient.Builder.class);
    when(instanceWebClientBuilder.build()).thenReturn(buildResult);

    // Act
    adminServerAutoConfiguration.infoUpdater(instanceRepository, instanceWebClientBuilder);

    // Assert
    verify(instanceWebClientBuilder).build();
    verify(builder).build();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#infoUpdateTrigger(InfoUpdater, Publisher)}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#infoUpdateTrigger(InfoUpdater, Publisher)}
   */
  @Test
  public void testInfoUpdateTrigger() {
    // Arrange
    AdminServerAutoConfiguration adminServerAutoConfiguration = new AdminServerAutoConfiguration(
        new AdminServerProperties());
    Builder builder = mock(Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient instanceWebClient = InstanceWebClient.builder().webClient(builder).build();
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    InfoUpdater infoUpdater = new InfoUpdater(repository, instanceWebClient, new ApiMediaTypeHandler());

    Flux<InstanceEvent> events = Flux.fromIterable(new ArrayList<>());

    // Act
    adminServerAutoConfiguration.infoUpdateTrigger(infoUpdater, events);

    // Assert
    verify(builder).build();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#eventStore()}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#eventStore()}
   */
  @Test
  public void testEventStore() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<InstanceEvent> createResult = StepVerifier.create(adminServerAutoConfiguration.eventStore().findAll());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link AdminServerAutoConfiguration#instanceRepository(InstanceEventStore)}.
   * <p>
   * Method under test: {@link AdminServerAutoConfiguration#instanceRepository(InstanceEventStore)}
   */
  @Test
  public void testInstanceRepository() throws AssertionError {
    //   Diffblue Cover was unable to create a Spring-specific test for this Spring method.
    //   Run dcover create --keep-partial-tests to gain insights into why
    //   a non-Spring test was created.

    // Arrange
    AdminServerAutoConfiguration adminServerAutoConfiguration = new AdminServerAutoConfiguration(
        new AdminServerProperties());

    // Act and Assert
    FirstStep<Instance> createResult = StepVerifier
        .create(adminServerAutoConfiguration.instanceRepository(new InMemoryEventStore()).findAll());
    createResult.expectComplete().verify();
  }
}
