package de.codecentric.boot.admin.server.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { InstancesController.class })
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class InstancesControllerDiffblueTest {

	@MockitoBean
	private InstanceEventStore instanceEventStore;

	@MockitoBean
	private InstanceRegistry instanceRegistry;

	@Autowired
	private InstancesController instancesController;

	/**
	 * Test
	 * {@link InstancesController#InstancesController(InstanceRegistry, InstanceEventStore)}.
	 * <p>
	 * Method under test:
	 * {@link InstancesController#InstancesController(InstanceRegistry, InstanceEventStore)}
	 */
	@Test
	public void testNewInstancesController() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).events());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances2() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString() throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instancesController.instances("Name"));
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances(eq("Name"));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString2() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString3() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString4() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenFluxFilterReturnFlux_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux2).filter(isA(Predicate.class));
		verify(flux).filter(isA(Predicate.class));
		verify(flux3).flatMap(isA(Function.class));
		verify(flux4).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenFluxFlatMapReturnFlux_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).filter(isA(Predicate.class));
		verify(flux2).flatMap(isA(Function.class));
		verify(flux3).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenFluxFlatMapReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
		Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
			.thenReturn(fromIterableResult);
		Flux<InstanceEvent> flux2 = mock(Flux.class);
		when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux2);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenFluxGroupByReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances(String)}
	 * return create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenInstanceRegistryGetInstancesReturnCreate() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(createResult);

		// Act
		instancesController.instances("Name");

		// Assert
		verify(instanceRegistry).getInstances(eq("Name"));
	}

	/**
	 * Test {@link InstancesController#instances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances(String)}
	 * return {@link Flux}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances(String)}
	 */
	@Test
	public void testInstancesWithString_givenInstanceRegistryGetInstancesReturnFlux() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRegistry.getInstances(Mockito.<String>any())).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instancesController.instances("Name"));
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances(eq("Name"));
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return {@link Flux}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenFluxFlatMapReturnFlux_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).filter(isA(Predicate.class));
		verify(flux2).flatMap(isA(Function.class));
		verify(flux3).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#flatMap(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenFluxFlatMapReturnFromIterableArrayList_thenCallsFlatMap() throws AssertionError {
		// Arrange
		Flux<GroupedFlux<Object, InstanceEvent>> flux = mock(Flux.class);
		Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.flatMap(Mockito.<Function<GroupedFlux<Object, InstanceEvent>, Publisher<Object>>>any()))
			.thenReturn(fromIterableResult);
		Flux<InstanceEvent> flux2 = mock(Flux.class);
		when(flux2.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(flux);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux2);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#groupBy(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances()} return
	 * create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenInstanceRegistryGetInstancesReturnCreate() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRegistry.getInstances()).thenReturn(createResult);

		// Act
		instancesController.instances();

		// Assert
		verify(instanceRegistry).getInstances();
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances()} return
	 * {@link Flux}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenInstanceRegistryGetInstancesReturnFlux_thenCallsFilter() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRegistry.getInstances()).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instancesController.instances());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstancesController#instances()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#getInstances()} return
	 * fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instances()}
	 */
	@Test
	public void testInstances_givenInstanceRegistryGetInstancesReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRegistry.getInstances()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instancesController.instances());
		createResult.expectComplete().verify();
		verify(instanceRegistry).getInstances();
	}

	/**
	 * Test {@link InstancesController#instance(String)}.
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Instance> responseEntity = r;
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
	 * Test {@link InstancesController#instance(String)}.
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance2() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Instance> responseEntity = r;
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
	 * Test {@link InstancesController#instance(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance_givenFluxCollectListReturnJustArrayList_when42_thenCallsCollectList()
			throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Instance> responseEntity = r;
			assertNull(responseEntity.getBody());
			assertTrue(responseEntity.getHeaders().isEmpty());
			HttpStatusCode statusCode = responseEntity.getStatusCode();
			assertTrue(statusCode instanceof HttpStatus);
			assertEquals(HttpStatus.NOT_FOUND, statusCode);
			assertEquals(404, responseEntity.getStatusCodeValue());
			assertFalse(responseEntity.hasBody());
			return;
		}).expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link InstancesController#instance(String)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore}
	 * {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Instance> responseEntity = r;
			assertNull(responseEntity.getBody());
			assertTrue(responseEntity.getHeaders().isEmpty());
			HttpStatusCode statusCode = responseEntity.getStatusCode();
			assertTrue(statusCode instanceof HttpStatus);
			assertEquals(HttpStatus.NOT_FOUND, statusCode);
			assertEquals(404, responseEntity.getStatusCodeValue());
			assertFalse(responseEntity.hasBody());
			return;
		}).expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link InstancesController#instance(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance_givenMonoFilterReturnJustArrayList_when42_thenCallsCollectList() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Instance> responseEntity = r;
			assertNull(responseEntity.getBody());
			assertTrue(responseEntity.getHeaders().isEmpty());
			HttpStatusCode statusCode = responseEntity.getStatusCode();
			assertTrue(statusCode instanceof HttpStatus);
			assertEquals(HttpStatus.NOT_FOUND, statusCode);
			assertEquals(404, responseEntity.getStatusCodeValue());
			assertFalse(responseEntity.hasBody());
			return;
		}).expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstancesController#instance(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@code Data}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance_givenMonoFilterReturnJustData_when42_thenCallsCollectList() throws AssertionError {
		// Arrange
		Mono<Object> mono = mock(Mono.class);
		Mono<Object> justResult = Mono.just("Data");
		when(mono.filter(Mockito.<Predicate<Object>>any())).thenReturn(justResult);
		Mono<List<InstanceEvent>> mono2 = mock(Mono.class);
		when(mono2.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(mono);
		Mono<List<InstanceEvent>> mono3 = mock(Mono.class);
		when(mono3.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono2);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono3);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono3).filter(isA(Predicate.class));
		verify(mono).filter(isA(Predicate.class));
		verify(mono2).map(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#instance(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#instance(String)}
	 */
	@Test
	public void testInstance_givenMonoMapReturnJustData_when42_thenCallsCollectList() throws AssertionError {
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
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Instance>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).instance("42"));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
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
	 * Test {@link InstancesController#unregister(String)}.
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister2() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
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
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
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
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore}
	 * {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
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
		verify(eventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry} {@link InstanceRegistry#deregister(InstanceId)}
	 * return just {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_givenInstanceRegistryDeregisterReturnJustInstanceIdWithValueIs42()
			throws AssertionError {
		// Arrange
		Mono<InstanceId> justResult = Mono.just(InstanceId.of("42"));
		when(instanceRegistry.deregister(Mockito.<InstanceId>any())).thenReturn(justResult);

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier.create(instancesController.unregister("42"));
		createResult.assertNext(r -> {
			ResponseEntity<Void> responseEntity = r;
			assertNull(responseEntity.getBody());
			assertTrue(responseEntity.getHeaders().isEmpty());
			HttpStatusCode statusCode = responseEntity.getStatusCode();
			assertTrue(statusCode instanceof HttpStatus);
			assertEquals(HttpStatus.NO_CONTENT, statusCode);
			assertEquals(204, responseEntity.getStatusCodeValue());
			assertFalse(responseEntity.hasBody());
			return;
		}).expectComplete().verify();
		verify(instanceRegistry).deregister(isA(InstanceId.class));
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_givenMonoFilterReturnJustArrayList_when42_thenCallsFilter() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return {@link Mono}.</li>
	 * <li>When {@code 42}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_givenMonoFilterReturnMono_when42_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#unregister(String)}.
	 * <ul>
	 * <li>Then calls
	 * {@link EventsourcingInstanceRepository#computeIfPresent(InstanceId, BiFunction)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#unregister(String)}
	 */
	@Test
	public void testUnregister_thenCallsComputeIfPresent() throws AssertionError {
		// Arrange
		Mono<Instance> mono = mock(Mono.class);
		Mono<Object> justResult = Mono.just("Data");
		when(mono.map(Mockito.<Function<Instance, Object>>any())).thenReturn(justResult);
		EventsourcingInstanceRepository repository = mock(EventsourcingInstanceRepository.class);
		when(repository.computeIfPresent(Mockito.<InstanceId>any(),
				Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any()))
			.thenReturn(mono);
		InstanceRegistry registry = new InstanceRegistry(repository, mock(InstanceIdGenerator.class),
				mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<ResponseEntity<Void>> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).unregister("42"));
		createResult.expectError().verify();
		verify(repository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link InstancesController#events()}.
	 * <p>
	 * Method under test: {@link InstancesController#events()}
	 */
	@Test
	public void testEvents() throws AssertionError {
		// Arrange
		InstanceRegistry registry = new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier
			.create(new InstancesController(registry, new InMemoryEventStore()).events());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstancesController#events()}.
	 * <ul>
	 * <li>Given {@link InstanceRegistry}.</li>
	 * <li>Then calls {@link InstanceEventStore#findAll()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstancesController#events()}
	 */
	@Test
	public void testEvents_givenInstanceRegistry_thenCallsFindAll() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceEventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(instancesController.events());
		createResult.expectComplete().verify();
		verify(instanceEventStore).findAll();
	}

}
