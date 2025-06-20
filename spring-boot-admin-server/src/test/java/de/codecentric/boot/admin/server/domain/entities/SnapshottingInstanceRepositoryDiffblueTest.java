package de.codecentric.boot.admin.server.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { SnapshottingInstanceRepository.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class SnapshottingInstanceRepositoryDiffblueTest {

	@MockitoBean
	private InstanceEventStore instanceEventStore;

	@Autowired
	private SnapshottingInstanceRepository snapshottingInstanceRepository;

	/**
	 * Test
	 * {@link SnapshottingInstanceRepository#SnapshottingInstanceRepository(InstanceEventStore)}.
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#SnapshottingInstanceRepository(InstanceEventStore)}
	 */
	@Test
	public void testNewSnapshottingInstanceRepository() throws AssertionError {
		// Diffblue Cover was unable to create a Spring-specific test for this Spring
		// method.
		// Run dcover create --keep-partial-tests to gain insights into why
		// a non-Spring test was created.

		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(new SnapshottingInstanceRepository(new InMemoryEventStore()).findAll());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#findAll()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#findAll()}
	 */
	@Test
	public void testFindAll() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(snapshottingInstanceRepository.findAll());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#find(InstanceId)}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#find(InstanceId)}
	 */
	@Test
	public void testFind() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.find(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#save(Instance)}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#save(Instance)}
	 */
	@Test
	public void testSave() throws AssertionError {
		// Arrange
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		when(instanceEventStore.append(Mockito.<List<InstanceEvent>>any()))
			.thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.save(Instance.create(InstanceId.of("42"))));
		createResult.expectError().verify();
		verify(instanceEventStore).append(isA(List.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#save(Instance)}.
	 * <ul>
	 * <li>Given
	 * {@link SnapshottingInstanceRepository#SnapshottingInstanceRepository(InstanceEventStore)}
	 * with eventStore is {@link InMemoryEventStore#InMemoryEventStore()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#save(Instance)}
	 */
	@Test
	public void testSave_givenSnapshottingInstanceRepositoryWithEventStoreIsInMemoryEventStore() throws AssertionError {
		// Arrange
		SnapshottingInstanceRepository snapshottingInstanceRepository = new SnapshottingInstanceRepository(
				new InMemoryEventStore());
		String value = "42";
		InstanceId id = InstanceId.of(value);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.save(Instance.create(id)));
		createResult.assertNext(i -> {
			Instance instance = i;
			assertNull(instance.getBuildVersion());
			assertSame(id, instance.getId());
			assertTrue(instance.getUnsavedEvents().isEmpty());
			assertEquals(-1L, instance.getVersion());
			assertFalse(instance.isRegistered());
			return;
		}).expectComplete().verify();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart() {
		// Arrange
		doThrow(new OptimisticLockingException("An error occurred")).when(instanceEventStore)
			.subscribe(Mockito.<Subscriber<InstanceEvent>>any());
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceEventStore.findAll()).thenReturn(fromIterableResult);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(instanceEventStore).subscribe(isA(Subscriber.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart2() {
		// Arrange
		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		doNothing().when(instanceEventStore).subscribe(Mockito.<Subscriber<InstanceEvent>>any());
		when(instanceEventStore.findAll()).thenReturn(fromIterableResult);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(instanceEventStore).subscribe(isA(Subscriber.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart3() {
		// Arrange
		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 59L));
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		doNothing().when(instanceEventStore).subscribe(Mockito.<Subscriber<InstanceEvent>>any());
		when(instanceEventStore.findAll()).thenReturn(fromIterableResult);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(instanceEventStore).subscribe(isA(Subscriber.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart4() {
		// Arrange
		ArrayList<InstanceEvent> it = new ArrayList<>();
		InstanceId instance = InstanceId.of("42");
		it.add(new InstanceEndpointsDetectedEvent(instance, 1L, Endpoints.empty()));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act
		new SnapshottingInstanceRepository(eventStore).start();

		// Assert
		verify(eventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart5() {
		// Arrange
		ArrayList<InstanceEvent> it = new ArrayList<>();
		InstanceId instance = InstanceId.of("42");
		it.add(new InstanceInfoChangedEvent(instance, 1L, Info.empty()));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act
		new SnapshottingInstanceRepository(eventStore).start();

		// Assert
		verify(eventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart6() {
		// Arrange
		InstanceDeregisteredEvent instanceDeregisteredEvent = mock(InstanceDeregisteredEvent.class);
		when(instanceDeregisteredEvent.getVersion()).thenThrow(new OptimisticLockingException("An error occurred"));
		when(instanceDeregisteredEvent.getInstance()).thenReturn(InstanceId.of("42"));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		InstanceId instance = InstanceId.of("42");
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();
		it.add(new InstanceRegisteredEvent(instance, 1L, registration));
		it.add(instanceDeregisteredEvent);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act
		new SnapshottingInstanceRepository(eventStore).start();

		// Assert
		verify(instanceDeregisteredEvent).getInstance();
		verify(instanceDeregisteredEvent).getVersion();
		verify(eventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart7() {
		// Arrange
		InstanceDeregisteredEvent instanceDeregisteredEvent = mock(InstanceDeregisteredEvent.class);
		when(instanceDeregisteredEvent.getVersion()).thenThrow(new OptimisticLockingException("An error occurred"));
		when(instanceDeregisteredEvent.getInstance()).thenReturn(InstanceId.of("42"));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		InstanceId instance = InstanceId.of("42");
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();
		it.add(new InstanceRegistrationUpdatedEvent(instance, 1L, registration));
		it.add(instanceDeregisteredEvent);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.findAll()).thenReturn(flux);

		// Act
		new SnapshottingInstanceRepository(eventStore).start();

		// Assert
		verify(instanceDeregisteredEvent).getInstance();
		verify(instanceDeregisteredEvent).getVersion();
		verify(eventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#subscribe(Consumer)} return
	 * {@link Disposable}.</li>
	 * <li>Then calls {@link Flux#subscribe(Consumer)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_givenFluxSubscribeReturnDisposable_thenCallsSubscribe() {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.subscribe(Mockito.<Consumer<InstanceEvent>>any())).thenReturn(mock(Disposable.class));
		Flux<InstanceEvent> flux2 = mock(Flux.class);
		when(flux2.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(flux);
		when(instanceEventStore.findAll()).thenReturn(flux2);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(flux2).concatWith(isA(Publisher.class));
		verify(flux).subscribe(isA(Consumer.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Given {@link InstanceDeregisteredEvent} {@link InstanceEvent#getInstance()}
	 * return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_givenInstanceDeregisteredEventGetInstanceReturnNull() {
		// Arrange
		InstanceDeregisteredEvent instanceDeregisteredEvent = mock(InstanceDeregisteredEvent.class);
		when(instanceDeregisteredEvent.getInstance()).thenReturn(null);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(instanceDeregisteredEvent);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		when(instanceEventStore.findAll()).thenReturn(flux);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceDeregisteredEvent).getInstance();
		verify(instanceEventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Given {@link InstanceEventStore} {@link InstanceEventStore#findAll()} return
	 * create three and {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_givenInstanceEventStoreFindAllReturnCreateThreeAndTrue() {
		// Arrange
		EmitterProcessor<InstanceEvent> createResult = EmitterProcessor.create(3, true);
		when(instanceEventStore.findAll()).thenReturn(createResult);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Given {@link InstanceEventStore} {@link InstanceEventStore#findAll()} return
	 * {@link Flux}.</li>
	 * <li>Then calls {@link Flux#concatWith(Publisher)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_givenInstanceEventStoreFindAllReturnFlux_thenCallsConcatWith() {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		when(instanceEventStore.findAll()).thenReturn(flux);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Then calls {@link InstanceEvent#getVersion()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_thenCallsGetVersion() {
		// Arrange
		InstanceDeregisteredEvent instanceDeregisteredEvent = mock(InstanceDeregisteredEvent.class);
		when(instanceDeregisteredEvent.getVersion()).thenThrow(new OptimisticLockingException("An error occurred"));
		when(instanceDeregisteredEvent.getInstance()).thenReturn(InstanceId.of("42"));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(instanceDeregisteredEvent);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.concatWith(Mockito.<Publisher<InstanceEvent>>any())).thenReturn(fromIterableResult);
		when(instanceEventStore.findAll()).thenReturn(flux);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceDeregisteredEvent).getInstance();
		verify(instanceDeregisteredEvent).getVersion();
		verify(instanceEventStore).findAll();
		verify(flux).concatWith(isA(Publisher.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#start()}.
	 * <ul>
	 * <li>Then calls {@link Publisher#subscribe(Subscriber)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link SnapshottingInstanceRepository#start()}
	 */
	@Test
	public void testStart_thenCallsSubscribe() {
		// Arrange
		doNothing().when(instanceEventStore).subscribe(Mockito.<Subscriber<InstanceEvent>>any());
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceEventStore.findAll()).thenReturn(fromIterableResult);

		// Act
		snapshottingInstanceRepository.start();

		// Assert
		verify(instanceEventStore).findAll();
		verify(instanceEventStore).subscribe(isA(Subscriber.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}.
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}
	 */
	@Test
	public void testRehydrateSnapshot() throws AssertionError {
		// Arrange
		SnapshottingInstanceRepository snapshottingInstanceRepository = new SnapshottingInstanceRepository(
				new InMemoryEventStore());

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.rehydrateSnapshot(InstanceId.of("42")));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}
	 */
	@Test
	public void testRehydrateSnapshot_givenFluxCollectListReturnJustArrayList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		when(instanceEventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.rehydrateSnapshot(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(instanceEventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}.
	 * <ul>
	 * <li>Given {@link InstanceEventStore} {@link InstanceEventStore#find(InstanceId)}
	 * return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}
	 */
	@Test
	public void testRehydrateSnapshot_givenInstanceEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(instanceEventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.rehydrateSnapshot(InstanceId.of("42")));
		createResult.expectComplete().verify();
		verify(instanceEventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}
	 */
	@Test
	public void testRehydrateSnapshot_givenMonoFilterReturnJustArrayList_thenCallsFilter() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(mono.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(justResult);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono);
		when(instanceEventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		String value = "42";
		InstanceId id = InstanceId.of(value);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier.create(snapshottingInstanceRepository.rehydrateSnapshot(id));
		createResult.assertNext(i -> {
			Instance instance = i;
			assertNull(instance.getBuildVersion());
			assertSame(id, instance.getId());
			Map<String, Object> values = instance.getInfo().getValues();
			assertTrue(values.isEmpty());
			StatusInfo statusInfo = instance.getStatusInfo();
			assertTrue(statusInfo.getDetails().isEmpty());
			assertEquals("UNKNOWN", statusInfo.getStatus());
			assertFalse(statusInfo.isDown());
			assertFalse(statusInfo.isOffline());
			assertTrue(statusInfo.isUnknown());
			assertFalse(statusInfo.isUp());
			Instant statusTimestamp = instance.getStatusTimestamp();
			assertEquals(0L, statusTimestamp.getEpochSecond());
			assertEquals(0, statusTimestamp.getNano());
			assertSame(values, instance.getTags().getValues());
			assertTrue(instance.getUnsavedEvents().isEmpty());
			assertEquals(-1L, instance.getVersion());
			assertFalse(instance.isRegistered());
			return;
		}).expectComplete().verify();
		verify(instanceEventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
	 * <li>Then calls {@link Mono#map(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#rehydrateSnapshot(InstanceId)}
	 */
	@Test
	public void testRehydrateSnapshot_givenMonoMapReturnJustData_thenCallsMap() throws AssertionError {
		// Arrange
		Mono<List<InstanceEvent>> mono = mock(Mono.class);
		Mono<Object> justResult = Mono.just("Data");
		when(mono.map(Mockito.<Function<List<InstanceEvent>, Object>>any())).thenReturn(justResult);
		Mono<List<InstanceEvent>> mono2 = mock(Mono.class);
		when(mono2.filter(Mockito.<Predicate<List<InstanceEvent>>>any())).thenReturn(mono);
		Flux<InstanceEvent> flux = mock(Flux.class);
		when(flux.collectList()).thenReturn(mono2);
		when(instanceEventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);

		// Act and Assert
		FirstStep<Instance> createResult = StepVerifier
			.create(snapshottingInstanceRepository.rehydrateSnapshot(InstanceId.of("42")));
		createResult.expectError().verify();
		verify(instanceEventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@code null}.</li>
	 * <li>When {@link InstanceRegisteredEvent} {@link InstanceEvent#getInstance()} return
	 * {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}
	 */
	@Test
	public void testUpdateSnapshot_givenNull_whenInstanceRegisteredEventGetInstanceReturnNull() {
		// Arrange
		InstanceRegisteredEvent event = mock(InstanceRegisteredEvent.class);
		when(event.getInstance()).thenReturn(null);

		// Act
		snapshottingInstanceRepository.updateSnapshot(event);

		// Assert
		verify(event).getInstance();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link OptimisticLockingException#OptimisticLockingException(String)}
	 * with message is {@code An error occurred}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}
	 */
	@Test
	public void testUpdateSnapshot_givenOptimisticLockingExceptionWithMessageIsAnErrorOccurred() {
		// Arrange
		InstanceRegisteredEvent event = mock(InstanceRegisteredEvent.class);
		when(event.getVersion()).thenThrow(new OptimisticLockingException("An error occurred"));
		when(event.getInstance()).thenReturn(InstanceId.of("42"));

		// Act
		snapshottingInstanceRepository.updateSnapshot(event);

		// Assert
		verify(event).getInstance();
		verify(event).getVersion();
	}

	/**
	 * Test {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}.
	 * <ul>
	 * <li>Then calls {@link InstanceEvent#getTimestamp()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SnapshottingInstanceRepository#updateSnapshot(InstanceEvent)}
	 */
	@Test
	public void testUpdateSnapshot_thenCallsGetTimestamp() {
		// Arrange
		SnapshottingInstanceRepository snapshottingInstanceRepository = new SnapshottingInstanceRepository(
				new InMemoryEventStore());
		InstanceRegisteredEvent event = mock(InstanceRegisteredEvent.class);
		Registration buildResult = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();
		when(event.getRegistration()).thenReturn(buildResult);
		when(event.getTimestamp())
			.thenReturn(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
		when(event.getVersion()).thenReturn(1L);
		when(event.getInstance()).thenReturn(InstanceId.of("42"));

		// Act
		snapshottingInstanceRepository.updateSnapshot(event);

		// Assert
		verify(event, atLeast(1)).getInstance();
		verify(event).getTimestamp();
		verify(event, atLeast(1)).getVersion();
		verify(event).getRegistration();
	}

}
