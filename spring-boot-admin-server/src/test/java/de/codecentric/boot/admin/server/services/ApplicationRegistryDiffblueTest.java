package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { ApplicationRegistry.class })
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationRegistryDiffblueTest {

	@Autowired
	private ApplicationRegistry applicationRegistry;

	@MockitoBean
	private InstanceEventPublisher instanceEventPublisher;

	@MockitoBean
	private InstanceRegistry instanceRegistry;

	/**
	 * Test
	 * {@link ApplicationRegistry#ApplicationRegistry(InstanceRegistry, InstanceEventPublisher)}.
	 * <p>
	 * Method under test:
	 * {@link ApplicationRegistry#ApplicationRegistry(InstanceRegistry, InstanceEventPublisher)}
	 */
	@Test
	public void testNewApplicationRegistry() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.getApplications());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.getApplications());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.getApplications());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRegistry.getInstances()).thenReturn(flux);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplications());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxFlatMapReturnFlux_thenCallsFlatMap() throws AssertionError {
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
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplications());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).filter(isA(Predicate.class));
		verify(flux2).flatMap(isA(Function.class));
		verify(flux3).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function, int)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function, int)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap()
			throws AssertionError {
		// Arrange
		Flux<GroupedFlux<Object, Instance>> flux = mock(Flux.class);
		Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, Instance>, Publisher<Object>>>any(), anyInt()))
			.thenReturn(fromIterableResult);
		Flux<Instance> flux2 = mock(Flux.class);
		when(flux2.groupBy(Mockito.<Function<Instance, Object>>any())).thenReturn(flux);
		Flux<Instance> flux3 = mock(Flux.class);
		when(flux3.filter(Mockito.<Predicate<Instance>>any())).thenReturn(flux2);
		when(instanceRegistry.getInstances()).thenReturn(flux3);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplications());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
		verify(flux3).filter(isA(Predicate.class));
		verify(flux).flatMap(isA(Function.class), eq(2147483647));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap2()
			throws AssertionError {
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
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplications());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxGroupByReturnFromIterableArrayList_thenCallsFilter()
			throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, Instance>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<Instance, Object>>any())).thenReturn(fromIterableResult);
		Flux<Instance> flux2 = mock(Flux.class);
		when(flux2.filter(Mockito.<Predicate<Instance>>any())).thenReturn(flux);
		when(instanceRegistry.getInstances()).thenReturn(flux2);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplications());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
		verify(flux2).filter(isA(Predicate.class));
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link ConcurrentMapEventStore#findAll()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenFluxGroupByReturnFromIterableArrayList_thenCallsFindAll()
			throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplications());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplications());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances()} return
	 * create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenInstanceRegistryGetInstancesReturnCreate() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRegistry.getInstances()).thenReturn(createResult);

		// Act
		applicationRegistry.getApplications();

		// Assert
		verify(instanceRegistry).getInstances();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplications()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances()} return
	 * fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplications()}
	 */
	@Test
	public void testGetApplications_givenInstanceRegistryGetInstancesReturnFromIterableArrayList()
			throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRegistry.getInstances()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplications());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.getApplication("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.getApplication("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Mono<List<Instance>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		Flux<Instance> flux2 = mock(Flux.class);
		when(flux2.filter(Mockito.<Predicate<Instance>>any())).thenReturn(flux);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux2);

		// Act
		applicationRegistry.getApplication("Name");

		// Assert
		verify(instanceRegistry).getInstances(eq("Name"));
		verify(flux).collectList();
		verify(flux2).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxFilterReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplication("Name"));
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances(eq("Name"));
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxFlatMapReturnFlux_thenCallsFlatMap() throws AssertionError {
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
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplication("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).filter(isA(Predicate.class));
		verify(flux2).flatMap(isA(Function.class));
		verify(flux3).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxFlatMapReturnFlux_thenCallsFlatMap2() throws AssertionError {
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
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplication("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux2).filter(isA(Predicate.class));
		verify(flux).filter(isA(Predicate.class));
		verify(flux3).flatMap(isA(Function.class));
		verify(flux4).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap()
			throws AssertionError {
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
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplication("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#groupBy(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act
		new ApplicationRegistry(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class)), mock(InstanceEventPublisher.class))
			.getApplication("Name");

		// Assert
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.getApplication("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances(String)}
	 * return create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenInstanceRegistryGetInstancesReturnCreate() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(createResult);

		// Act
		applicationRegistry.getApplication("Name");

		// Assert
		verify(instanceRegistry).getInstances(eq("Name"));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances(String)}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenInstanceRegistryGetInstancesReturnFromIterableArrayList() {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);

		// Act
		applicationRegistry.getApplication("Name");

		// Assert
		verify(instanceRegistry).getInstances(eq("Name"));
	}

	/**
	 * Test {@link ApplicationRegistry#getApplication(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
	 * <li>Then calls {@link Mono#map(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getApplication(String)}
	 */
	@Test
	public void testGetApplication_givenMonoMapReturnJustData_thenCallsMap() throws AssertionError {
		// Arrange
		Mono<List<Instance>> mono = mock(Mono.class);
		Mono<Object> justResult = Mono.just("Data");
		when(mono.map(Mockito.<Function<List<Instance>, Object>>any())).thenReturn(justResult);
		Flux<Instance> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		Flux<Instance> flux2 = mock(Flux.class);
		when(flux2.filter(Mockito.<Predicate<Instance>>any())).thenReturn(flux);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux2);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.getApplication("Name"));
		createResult.expectError().verify();
		verify(instanceRegistry).getInstances(eq("Name"));
		verify(flux).collectList();
		verify(flux2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.deregister("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(new ApplicationRegistry(
				new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
						mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
				mock(InstanceEventPublisher.class))
			.deregister("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return {@link Flux}.</li>
	 * <li>When {@code Name}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenFluxFilterReturnFlux_whenName_thenCallsFilter() throws AssertionError {
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
		FirstStep<InstanceId> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.deregister("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux2).filter(isA(Predicate.class));
		verify(flux).filter(isA(Predicate.class));
		verify(flux3).flatMap(isA(Function.class));
		verify(flux4).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
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
		FirstStep<InstanceId> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.deregister("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).filter(isA(Predicate.class));
		verify(flux2).flatMap(isA(Function.class));
		verify(flux3).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return create.</li>
	 * <li>When empty string.</li>
	 * <li>Then return create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenFluxFlatMapReturnCreate_whenEmptyString_thenReturnCreate() {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		UnicastProcessor<Object> createResult = UnicastProcessor.create();
		when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(createResult);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);

		// Act
		Flux<InstanceId> actualDeregisterResult = applicationRegistry.deregister("");

		// Assert
		verify(instanceRegistry).getInstances(eq(""));
		verify(flux).flatMap(isA(Function.class));
		assertSame(createResult, actualDeregisterResult);
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#groupBy(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenFluxFlatMapReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
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
		FirstStep<InstanceId> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.deregister("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#groupBy(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.deregister("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier
			.create(new ApplicationRegistry(
					new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
							mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
					mock(InstanceEventPublisher.class))
				.deregister("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances(String)}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_givenInstanceRegistryGetInstancesReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(applicationRegistry.deregister("Name"));
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances(eq("Name"));
	}

	/**
	 * Test {@link ApplicationRegistry#deregister(String)}.
	 * <ul>
	 * <li>Then return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#deregister(String)}
	 */
	@Test
	public void testDeregister_thenReturnFromIterableArrayList() {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.flatMap(Mockito.<Function<Instance, Publisher<Object>>>any())).thenReturn(fromIterableResult);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);

		// Act
		Flux<InstanceId> actualDeregisterResult = applicationRegistry.deregister("Name");

		// Assert
		verify(instanceRegistry).getInstances(eq("Name"));
		verify(flux).flatMap(isA(Function.class));
		assertSame(fromIterableResult, actualDeregisterResult);
	}

	/**
	 * Test {@link ApplicationRegistry#toApplication(String, Flux)}.
	 * <ul>
	 * <li>Given just {@link ArrayList#ArrayList()}.</li>
	 * <li>When {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#toApplication(String, Flux)}
	 */
	@Test
	public void testToApplication_givenJustArrayList_whenFluxCollectListReturnJustArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> instances = mock(Flux.class);
		Mono<List<Instance>> justResult = Mono.just(new ArrayList<>());
		when(instances.collectList()).thenReturn(justResult);

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.toApplication("Name", instances));
		createResult.assertNext(a -> {
			Application application = a;
			assertNull(application.getBuildVersion());
			assertTrue(application.getInstances().isEmpty());
			assertEquals("Name", application.getName());
			assertEquals("UNKNOWN", application.getStatus());
			Instant statusTimestamp = application.getStatusTimestamp();
			assertEquals(0L, statusTimestamp.getEpochSecond());
			assertEquals(0, statusTimestamp.getNano());
			return;
		}).expectComplete().verify();
		verify(instances).collectList();
	}

	/**
	 * Test {@link ApplicationRegistry#toApplication(String, Flux)}.
	 * <ul>
	 * <li>When fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#toApplication(String, Flux)}
	 */
	@Test
	public void testToApplication_whenFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> instances = Flux.fromIterable(new ArrayList<>());

		// Act and Assert
		FirstStep<Application> createResult = StepVerifier.create(applicationRegistry.toApplication("Name", instances));
		createResult.assertNext(a -> {
			Application application = a;
			assertNull(application.getBuildVersion());
			assertTrue(application.getInstances().isEmpty());
			assertEquals("Name", application.getName());
			assertEquals("UNKNOWN", application.getStatus());
			Instant statusTimestamp = application.getStatusTimestamp();
			assertEquals(0L, statusTimestamp.getEpochSecond());
			assertEquals(0, statusTimestamp.getNano());
			return;
		}).expectComplete().verify();
	}

	/**
	 * Test {@link ApplicationRegistry#getBuildVersion(List)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getBuildVersion(List)}
	 */
	@Test
	public void testGetBuildVersion() {
		// Arrange, Act and Assert
		assertNull(applicationRegistry.getBuildVersion(new ArrayList<>()));
	}

	/**
	 * Test {@link ApplicationRegistry#getStatus(List)}.
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getStatus(List)}
	 */
	@Test
	public void testGetStatus() {
		// Arrange, Act and Assert
		List<Object> toListResult = applicationRegistry.getStatus(new ArrayList<>()).toList();
		assertEquals(2, toListResult.size());
		Object getResult = toListResult.get(1);
		assertTrue(getResult instanceof Instant);
		assertEquals("UNKNOWN", toListResult.get(0));
		assertEquals(0, ((Instant) getResult).getNano());
		assertEquals(0L, ((Instant) getResult).getEpochSecond());
	}

	/**
	 * Test {@link ApplicationRegistry#getMax(Instant, Instant)}.
	 * <ul>
	 * <li>When {@link LocalDate} with {@code 1970} and one and one atStartOfDay atZone
	 * {@link ZoneOffset#UTC} toInstant.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getMax(Instant, Instant)}
	 */
	@Test
	public void testGetMax_whenLocalDateWith1970AndOneAndOneAtStartOfDayAtZoneUtcToInstant() {
		// Arrange
		Instant t1 = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

		// Act
		Instant actualMax = applicationRegistry.getMax(t1,
				LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		// Assert
		assertSame(actualMax.EPOCH, actualMax);
	}

	/**
	 * Test {@link ApplicationRegistry#getMax(Instant, Instant)}.
	 * <ul>
	 * <li>When ofYearDay one and one atStartOfDay atZone {@link ZoneOffset#UTC}
	 * toInstant.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApplicationRegistry#getMax(Instant, Instant)}
	 */
	@Test
	public void testGetMax_whenOfYearDayOneAndOneAtStartOfDayAtZoneUtcToInstant() {
		// Arrange
		Instant t1 = LocalDate.ofYearDay(1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();

		// Act
		Instant actualMax = applicationRegistry.getMax(t1,
				LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

		// Assert
		assertSame(actualMax.EPOCH, actualMax);
	}

}
