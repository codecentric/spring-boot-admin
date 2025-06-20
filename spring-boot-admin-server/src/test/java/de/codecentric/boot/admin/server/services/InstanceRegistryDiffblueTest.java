package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
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
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
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
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { InstanceRegistry.class })
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class InstanceRegistryDiffblueTest {

	@MockitoBean
	private InstanceFilter instanceFilter;

	@MockitoBean
	private InstanceIdGenerator instanceIdGenerator;

	@Autowired
	private InstanceRegistry instanceRegistry;

	@MockitoBean
	private InstanceRepository instanceRepository;

	/**
	 * Test
	 * {@link InstanceRegistry#InstanceRegistry(InstanceRepository, InstanceIdGenerator, InstanceFilter)}.
	 * <p>
	 * Method under test:
	 * {@link InstanceRegistry#InstanceRegistry(InstanceRepository, InstanceIdGenerator, InstanceFilter)}
	 */
	@Test
	public void testNewInstanceRegistry() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister() throws AssertionError {
		// Arrange
		InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
		String value = "42";
		InstanceId ofResult = InstanceId.of(value);
		when(generator.generateId(Mockito.<Registration>any())).thenReturn(ofResult);
		InstanceRegistry instanceRegistry = new InstanceRegistry(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.assertNext(i -> {
			assertSame(ofResult, i);
			return;
		}).expectComplete().verify();
		verify(generator).generateId(isA(Registration.class));
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		InstanceEventStore eventStore = mock(InstanceEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
		InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
		when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
		InstanceRegistry instanceRegistry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(generator).generateId(isA(Registration.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <ul>
	 * <li>Given {@link InstanceEventStore} {@link InstanceEventStore#find(InstanceId)}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister_givenInstanceEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		InstanceEventStore eventStore = mock(InstanceEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(eventStore);
		InstanceIdGenerator generator = mock(InstanceIdGenerator.class);
		when(generator.generateId(Mockito.<Registration>any())).thenReturn(InstanceId.of("42"));
		InstanceRegistry instanceRegistry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(generator).generateId(isA(Registration.class));
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry instanceRegistry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(generator).generateId(isA(Registration.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return {@link Mono}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister_givenMonoFilterReturnMono_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry instanceRegistry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(generator).generateId(isA(Registration.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#register(Registration)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#flatMap(Function)} return just
	 * {@code Data}.</li>
	 * <li>Then calls {@link Mono#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#register(Registration)}
	 */
	@Test
	public void testRegister_givenMonoFlatMapReturnJustData_thenCallsFlatMap() throws AssertionError {
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
		InstanceRegistry instanceRegistry = new InstanceRegistry(repository, generator, mock(InstanceFilter.class));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.register(registration));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(generator).generateId(isA(Registration.class));
		verify(flux).collectList();
		verify(mono3).filter(isA(Predicate.class));
		verify(mono).flatMap(isA(Function.class));
		verify(mono2).map(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString() throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRepository.findByName(Mockito.<String>any())).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstances("Name"));
		createResult.expectComplete().verify();
		verify(instanceRepository).findByName(eq("Name"));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString3() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString4() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new SnapshottingInstanceRepository(new InMemoryEventStore()),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances("Name"));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString_givenFluxFilterReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRepository.findByName(Mockito.<String>any())).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstances("Name"));
		createResult.expectComplete().verify();
		verify(instanceRepository).findByName(eq("Name"));
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString_givenFluxGroupByReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <ul>
	 * <li>Given {@link InstanceRepository} {@link InstanceRepository#findByName(String)}
	 * return create.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString_givenInstanceRepositoryFindByNameReturnCreate() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRepository.findByName(Mockito.<String>any())).thenReturn(createResult);

		// Act
		instanceRegistry.getInstances("Name");

		// Assert
		verify(instanceRepository).findByName(eq("Name"));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <ul>
	 * <li>Then calls {@link Flux#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString_thenCallsFlatMap() throws AssertionError {
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
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances("Name"));
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).flatMap(isA(Function.class));
		verify(flux2).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances(String)} with {@code String}.
	 * <ul>
	 * <li>Then return create zero and {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances(String)}
	 */
	@Test
	public void testGetInstancesWithString_thenReturnCreateZeroAndTrue() {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		ReplayProcessor<Instance> createResult = ReplayProcessor.create(0, true);
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(createResult);
		when(instanceRepository.findByName(Mockito.<String>any())).thenReturn(flux);

		// Act
		Flux<Instance> actualInstances = instanceRegistry.getInstances("Name");

		// Assert
		verify(instanceRepository).findByName(eq("Name"));
		verify(flux).filter(isA(Predicate.class));
		assertSame(createResult, actualInstances);
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#filter(Predicate)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances_givenFluxFilterReturnFromIterableArrayList_thenCallsFilter() throws AssertionError {
		// Arrange
		Flux<Instance> flux = mock(Flux.class);
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.filter(Mockito.<Predicate<Instance>>any())).thenReturn(fromIterableResult);
		when(instanceRepository.findAll()).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstances());
		createResult.expectComplete().verify();
		verify(instanceRepository).findAll();
		verify(flux).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#groupBy(Function)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#groupBy(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances_givenFluxGroupByReturnFromIterableArrayList_thenCallsGroupBy() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<GroupedFlux<Object, InstanceEvent>> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.groupBy(Mockito.<Function<InstanceEvent, Object>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
		verify(flux).groupBy(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore} {@link ConcurrentMapEventStore#findAll()}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances_givenHazelcastEventStoreFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
					mock(InstanceIdGenerator.class), mock(InstanceFilter.class))
				.getInstances());
		createResult.expectComplete().verify();
		verify(eventStore).findAll();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <ul>
	 * <li>Given {@link InstanceRepository} {@link InstanceRepository#findAll()} return
	 * create.</li>
	 * <li>Then calls {@link InstanceRepository#findAll()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances_givenInstanceRepositoryFindAllReturnCreate_thenCallsFindAll() {
		// Arrange
		DirectProcessor<Instance> createResult = DirectProcessor.create();
		when(instanceRepository.findAll()).thenReturn(createResult);

		// Act
		instanceRegistry.getInstances();

		// Assert
		verify(instanceRepository).findAll();
	}

	/**
	 * Test {@link InstanceRegistry#getInstances()}.
	 * <ul>
	 * <li>Given {@link InstanceRepository} {@link InstanceRepository#findAll()} return
	 * fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstances()}
	 */
	@Test
	public void testGetInstances_givenInstanceRepositoryFindAllReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<Instance> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceRepository.findAll()).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstances());
		createResult.expectComplete().verify();
		verify(instanceRepository).findAll();
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance() throws AssertionError {
		// Arrange
		InstanceRegistry instanceRegistry = new InstanceRegistry(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
				mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstance(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance2() throws AssertionError {
		// Arrange
		InstanceRegistry instanceRegistry = new InstanceRegistry(
				new SnapshottingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
				mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstance(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstance(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore}
	 * {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstance(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance_givenMonoFilterReturnJustArrayList_thenCallsCollectList() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(instanceRegistry.getInstance(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstanceRegistry#getInstance(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return {@code null}.</li>
	 * <li>Then return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#getInstance(InstanceId)}
	 */
	@Test
	public void testGetInstance_givenMonoFilterReturnNull_thenReturnNull() {
		// Arrange
		Mono<Instance> mono = mock(Mono.class);
		when(mono.filter(Mockito.<Predicate<Instance>>any())).thenReturn(null);
		when(instanceRepository.find(Mockito.<InstanceId>any())).thenReturn(mono);

		// Act
		Mono<Instance> actualInstance = instanceRegistry.getInstance(InstanceId.of("42"));

		// Assert
		verify(instanceRepository).find(isA(InstanceId.class));
		verify(mono).filter(isA(Predicate.class));
		assertNull(actualInstance);
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister() throws AssertionError {
		// Arrange
		InstanceRegistry instanceRegistry = new InstanceRegistry(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
				mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister2() throws AssertionError {
		// Arrange
		InstanceRegistry instanceRegistry = new InstanceRegistry(
				new SnapshottingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
				mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister_givenFluxCollectListReturnJustArrayList_thenCallsCollectList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore}
	 * {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#flatMap(Function)} return just
	 * {@code Data}.</li>
	 * <li>Then calls {@link Mono#flatMap(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister_givenMonoFlatMapReturnJustData_thenCallsFlatMap() throws AssertionError {
		// Arrange
		Mono<Object> mono = mock(Mono.class);
		Mono<Object> justResult = Mono.just("Data");
		when(mono.flatMap(Mockito.<Function<Object, Mono<Object>>>any())).thenReturn(justResult);
		Mono<Object> mono2 = mock(Mono.class);
		when(mono2.flatMap(Mockito.<Function<Object, Mono<Object>>>any())).thenReturn(mono);
		Mono<List<InstanceEvent>> mono3 = mock(Mono.class);
		when(mono3.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(mono2);
		Mono<List<InstanceEvent>> mono4 = mock(Mono.class);
		when(mono4.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono3);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono4);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono4).filter(isA(Predicate.class));
		verify(mono2).flatMap(isA(Function.class));
		verify(mono).flatMap(isA(Function.class));
		verify(mono3).map(isA(Function.class));
	}

	/**
	 * Test {@link InstanceRegistry#deregister(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceRegistry#deregister(InstanceId)}
	 */
	@Test
	public void testDeregister_givenMonoMapReturnJustData_thenCallsFilter() throws AssertionError {
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
		InstanceRegistry instanceRegistry = new InstanceRegistry(new EventsourcingInstanceRepository(eventStore),
				mock(InstanceIdGenerator.class), mock(InstanceFilter.class));

		// Act and Assert
		FirstStep<InstanceId> createResult = StepVerifier.create(instanceRegistry.deregister(InstanceId.of("42")));
		createResult.expectError().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

}
