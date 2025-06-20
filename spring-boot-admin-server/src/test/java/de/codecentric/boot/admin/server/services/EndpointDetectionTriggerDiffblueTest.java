package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import com.sun.security.auth.UserPrincipal;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.services.endpoints.EndpointDetectionStrategy;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ClientEndpointConfig.Builder;
import jakarta.websocket.ClientEndpointConfig.Configurator;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Extension;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.websocket.AsyncChannelWrapperNonSecure;
import org.apache.tomcat.websocket.EndpointClassHolder;
import org.apache.tomcat.websocket.EndpointHolder;
import org.apache.tomcat.websocket.WsRemoteEndpointImplClient;
import org.apache.tomcat.websocket.WsSession;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.apache.tomcat.websocket.pojo.PojoEndpointServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.adapter.StandardWebSocketSession;
import reactor.core.Scannable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { EndpointDetectionTrigger.class })
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class EndpointDetectionTriggerDiffblueTest {

	@Autowired
	private EndpointDetectionTrigger endpointDetectionTrigger;

	@MockitoBean
	private EndpointDetector endpointDetector;

	@MockitoBean
	private Publisher<InstanceEvent> publisher;

	/**
	 * Test
	 * {@link EndpointDetectionTrigger#EndpointDetectionTrigger(EndpointDetector, Publisher)}.
	 * <p>
	 * Method under test:
	 * {@link EndpointDetectionTrigger#EndpointDetectionTrigger(EndpointDetector, Publisher)}
	 */
	@Test
	public void testNewEndpointDetectionTrigger() {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new EventsourcingInstanceRepository(new InMemoryEventStore(3)), mock(EndpointDetectionStrategy.class));

		IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
		when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

		// Act
		new EndpointDetectionTrigger(endpointDetector, new HazelcastEventStore(Integer.MIN_VALUE, eventLog));

		// Assert
		verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
	}

	/**
	 * Test
	 * {@link EndpointDetectionTrigger#EndpointDetectionTrigger(EndpointDetector, Publisher)}.
	 * <p>
	 * Method under test:
	 * {@link EndpointDetectionTrigger#EndpointDetectionTrigger(EndpointDetector, Publisher)}
	 */
	@Test
	public void testNewEndpointDetectionTrigger2() {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new EventsourcingInstanceRepository(new InMemoryEventStore(Integer.MIN_VALUE)),
				mock(EndpointDetectionStrategy.class));

		IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
		when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

		// Act
		new EndpointDetectionTrigger(endpointDetector, new HazelcastEventStore(1, eventLog));

		// Assert
		verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any()))
			.thenReturn(new ChannelSendOperator<>(source, writeFunction));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult2 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult2);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle2() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Mono<?> source = Mono.just("Data");
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any()))
			.thenReturn(new ChannelSendOperator<>(source, writeFunction));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult2 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult2);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle3() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Publisher<Object> source = mock(Publisher.class);
		doNothing().when(source).subscribe(Mockito.<Subscriber<Object>>any());
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any()))
			.thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(source).subscribe(isA(Subscriber.class));
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle4() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(new ChannelSendOperator<>(source, writeFunction));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult2 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult2);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle5() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		ChannelSendOperator<Object> source2 = new ChannelSendOperator<>(source, writeFunction);

		Function<Publisher<Object>, Publisher<Void>> writeFunction2 = mock(Function.class);
		Flux<Void> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
		when(writeFunction2.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult2);
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(new ChannelSendOperator<>(source2, writeFunction2));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult3 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult3);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(writeFunction2).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle6() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Mono<?> source = Mono.just("Data");
		ChannelSendOperator<Object> source2 = new ChannelSendOperator<>(source, writeFunction);

		Function<Publisher<Object>, Publisher<Void>> writeFunction2 = mock(Function.class);
		Flux<Void> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
		when(writeFunction2.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult2);
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(new ChannelSendOperator<>(source2, writeFunction2));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult3 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult3);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(writeFunction2).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle7() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any())).thenReturn(
				new ChannelSendOperator<>(new ChannelSendOperator<>(new InMemoryEventStore(), mock(Function.class)),
						mock(Function.class)));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle8() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		ChannelSendOperator<Object> source2 = new ChannelSendOperator<>(source, writeFunction);

		Function<Publisher<Object>, Publisher<Void>> writeFunction2 = mock(Function.class);
		Flux<Void> fromIterableResult2 = Flux.fromIterable(new ArrayList<>());
		when(writeFunction2.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult2);
		ChannelSendOperator<Object> source3 = new ChannelSendOperator<>(source2, writeFunction2);

		Function<Publisher<Object>, Publisher<Void>> writeFunction3 = mock(Function.class);
		Flux<Void> fromIterableResult3 = Flux.fromIterable(new ArrayList<>());
		when(writeFunction3.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult3);
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(new ChannelSendOperator<>(source3, writeFunction3));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult4 = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult4);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(writeFunction2).apply(isA(Publisher.class));
		verify(writeFunction3).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle9() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Publisher<Object> source = mock(Publisher.class);
		doNothing().when(source).subscribe(Mockito.<Subscriber<Object>>any());
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(new ChannelSendOperator<>(new ChannelSendOperator<>(
					new ChannelSendOperator<>(new ChannelSendOperator<>(source, mock(Function.class)),
							mock(Function.class)),
					mock(Function.class)), mock(Function.class)));
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(source).subscribe(isA(Subscriber.class));
		verify(publisher2).filter(isA(Predicate.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle10() {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		endpointDetectionTrigger.handle(publisher2);

		// Assert
		verify(publisher2).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link ArrayList#ArrayList()} add {@code 42}.</li>
	 * <li>Then calls {@link Function#apply(Object)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenArrayListAdd42_thenCallsApply() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		ArrayList<Object> it = new ArrayList<>();
		it.add("42");
		Flux<?> source = Flux.fromIterable(it);
		Function<Publisher<Object>, Publisher<Void>> writeFunction = mock(Function.class);
		Flux<Void> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(writeFunction.apply(Mockito.<Publisher<Object>>any())).thenReturn(fromIterableResult);
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any()))
			.thenReturn(new ChannelSendOperator<>(source, writeFunction));

		ArrayList<InstanceEvent> it2 = new ArrayList<>();
		it2.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult2 = Flux.fromIterable(it2);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult2);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(writeFunction).apply(isA(Publisher.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link EndpointDetector}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenEndpointDetector() throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link EndpointDetector}
	 * {@link EndpointDetector#detectEndpoints(InstanceId)} return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenEndpointDetectorDetectEndpointsReturnNull()
			throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(null);

		ArrayList<InstanceEvent> it = new ArrayList<>();
		it.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(it);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(fromIterableResult);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(clientEndpointHolder).getInstance(isNull());
		verify(publisher2).filter(isA(Predicate.class));
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link EndpointDetector}.</li>
	 * <li>When create.</li>
	 * <li>Then create Error is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenEndpointDetector_whenCreate_thenCreateErrorIsNull()
			throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		DirectProcessor<InstanceEvent> publisher2 = DirectProcessor.create();

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointHolder clientEndpointHolder = new EndpointHolder(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertNull(publisher2.getError());
		assertFalse(publisher2.isDisposed());
		assertFalse(publisher2.isTerminated());
		assertFalse(publisher2.hasCompleted());
		assertFalse(publisher2.hasError());
		assertFalse(publisher2.isSerialized());
		Stream<? extends Scannable> actualsResult = publisher2.actuals();
		assertTrue(actualsResult.limit(5).collect(Collectors.toList()).isEmpty());
		Stream<? extends Scannable> parentsResult = publisher2.parents();
		assertTrue(parentsResult.limit(5).collect(Collectors.toList()).isEmpty());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertTrue(publisher2.isScanAvailable());
		assertTrue(publisher2.hasDownstreams());
		assertEquals(Integer.MAX_VALUE, publisher2.getPrefetch());
		assertEquals(Integer.MAX_VALUE, publisher2.getBufferSize());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link EndpointDetector}.</li>
	 * <li>When fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenEndpointDetector_whenFromIterableArrayList() throws AssertionError {
		// Arrange
		Flux<InstanceEvent> publisher2 = Flux.fromIterable(new ArrayList<>());

		// Act
		endpointDetectionTrigger.handle(publisher2);

		// Assert that nothing has changed
		FirstStep<InstanceEvent> createResult = StepVerifier.create(publisher2);
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Given {@link EndpointDetector}.</li>
	 * <li>When fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_givenEndpointDetector_whenFromIterableArrayList2()
			throws DeploymentException, AssertionError, NoSuchAlgorithmException {
		// Arrange
		Flux<InstanceEvent> publisher2 = Flux.fromIterable(new ArrayList<>());

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(clientEndpointHolder).getInstance(isNull());
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertFalse(bufferFactoryResult.isDirect());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
		FirstStep<InstanceEvent> createResult2 = StepVerifier.create(publisher2);
		createResult2.expectComplete().verify();
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>Then return fromIterable {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_thenReturnFromIterableArrayList() {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);
		Flux<InstanceEvent> flux = mock(Flux.class);
		Flux<Object> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(flux.flatMap(Mockito.<Function<InstanceEvent, Publisher<Object>>>any())).thenReturn(fromIterableResult);
		Flux<InstanceEvent> publisher2 = mock(Flux.class);
		when(publisher2.filter(Mockito.<Predicate<InstanceEvent>>any())).thenReturn(flux);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);

		// Assert
		verify(publisher2).filter(isA(Predicate.class));
		verify(flux).flatMap(isA(Function.class));
		assertSame(fromIterableResult, actualHandleResult);
	}

	/**
	 * Test {@link EndpointDetectionTrigger#handle(Flux)}.
	 * <ul>
	 * <li>When create three and {@code true}.</li>
	 * <li>Then create three and {@code true} Pending is zero.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#handle(Flux)}
	 */
	@Test
	public void testHandle_whenCreateThreeAndTrue_thenCreateThreeAndTruePendingIsZero()
			throws DeploymentException, NoSuchAlgorithmException {
		// Arrange
		EmitterProcessor<InstanceEvent> publisher2 = EmitterProcessor.create(3, true);

		// Act
		Publisher<Void> actualHandleResult = endpointDetectionTrigger.handle(publisher2);
		EndpointClassHolder clientEndpointHolder = mock(EndpointClassHolder.class);
		when(clientEndpointHolder.getInstance(Mockito.<InstanceManager>any()))
			.thenReturn(new PojoEndpointServer(new HashMap<>(), "Pojo"));
		WsRemoteEndpointImplClient wsRemoteEndpoint = new WsRemoteEndpointImplClient(
				new AsyncChannelWrapperNonSecure(null));
		WsWebSocketContainer wsWebSocketContainer = new WsWebSocketContainer();
		ArrayList<Extension> negotiatedExtensions = new ArrayList<>();
		HashMap<String, String> pathParameters = new HashMap<>();
		Builder createResult = Builder.create();
		Builder configuratorResult = createResult.configurator(new Configurator());
		Builder decodersResult = configuratorResult.decoders(new ArrayList<>());
		Builder encodersResult = decodersResult.encoders(new ArrayList<>());
		Builder extensionsResult = encodersResult.extensions(new ArrayList<>());
		Builder preferredSubprotocolsResult = extensionsResult.preferredSubprotocols(new ArrayList<>());
		ClientEndpointConfig clientEndpointConfig = preferredSubprotocolsResult.sslContext(SSLContext.getDefault())
			.build();
		WsSession session = new WsSession(clientEndpointHolder, wsRemoteEndpoint, wsWebSocketContainer,
				negotiatedExtensions, "Sub Protocol", pathParameters, true, clientEndpointConfig);

		HttpHeaders headers = new HttpHeaders();
		Mono<Principal> principal = Mono.just(new UserPrincipal("data"));
		HandshakeInfo info = new HandshakeInfo(PagerdutyNotifier.DEFAULT_URI, headers, principal, "Protocol");

		DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
		StandardWebSocketSession standardWebSocketSession = new StandardWebSocketSession(session, info, factory);

		actualHandleResult.subscribe(standardWebSocketSession);

		// Assert that nothing has changed
		verify(clientEndpointHolder).getInstance(isNull());
		DataBufferFactory bufferFactoryResult = standardWebSocketSession.bufferFactory();
		assertTrue(bufferFactoryResult instanceof DefaultDataBufferFactory);
		assertEquals(0, publisher2.getPending());
		assertEquals(3, publisher2.getBufferSize());
		assertEquals(3, publisher2.getPrefetch());
		assertFalse(publisher2.isDisposed());
		assertFalse(publisher2.isTerminated());
		assertFalse(publisher2.hasCompleted());
		assertFalse(publisher2.hasError());
		assertFalse(publisher2.isSerialized());
		Stream<? extends Scannable> actualsResult = publisher2.actuals();
		assertTrue(actualsResult.limit(5).collect(Collectors.toList()).isEmpty());
		Stream<? extends Scannable> parentsResult = publisher2.parents();
		assertTrue(parentsResult.limit(5).collect(Collectors.toList()).isEmpty());
		assertTrue(standardWebSocketSession.getAttributes().isEmpty());
		assertTrue(standardWebSocketSession.isOpen());
		assertTrue(publisher2.isScanAvailable());
		assertSame(factory, bufferFactoryResult);
		assertSame(info, standardWebSocketSession.getHandshakeInfo());
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints() {
		// Arrange
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any()))
			.thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));

		// Act
		endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Assert
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints2() {
		// Arrange
		ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		ChannelSendOperator<Object> channelSendOperator2 = new ChannelSendOperator<>(source, mock(Function.class));

		when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
			.thenReturn(channelSendOperator2);
		when(endpointDetector.detectEndpoints(Mockito.<InstanceId>any())).thenReturn(channelSendOperator);

		// Act
		Mono<Void> actualDetectEndpointsResult = endpointDetectionTrigger
			.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Assert
		verify(endpointDetector).detectEndpoints(isA(InstanceId.class));
		verify(channelSendOperator).onErrorResume(isA(Function.class));
		assertSame(channelSendOperator2, actualDetectEndpointsResult);
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints3() throws AssertionError {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints4() throws AssertionError {
		// Arrange
		EndpointDetector endpointDetector = new EndpointDetector(
				new SnapshottingInstanceRepository(new InMemoryEventStore()), mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link Flux} {@link Flux#collectList()} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Flux#collectList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints_givenFluxCollectListReturnJustArrayList_thenCallsCollectList()
			throws AssertionError {
		// Arrange
		Flux<InstanceEvent> flux = mock(Flux.class);
		Mono<List<InstanceEvent>> justResult = Mono.just(new ArrayList<>());
		when(flux.collectList()).thenReturn(justResult);
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(flux);
		EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
				mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link HazelcastEventStore}
	 * {@link ConcurrentMapEventStore#find(InstanceId)} return fromIterable
	 * {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints_givenHazelcastEventStoreFindReturnFromIterableArrayList() throws AssertionError {
		// Arrange
		HazelcastEventStore eventStore = mock(HazelcastEventStore.class);
		Flux<InstanceEvent> fromIterableResult = Flux.fromIterable(new ArrayList<>());
		when(eventStore.find(Mockito.<InstanceId>any())).thenReturn(fromIterableResult);
		EndpointDetector endpointDetector = new EndpointDetector(new EventsourcingInstanceRepository(eventStore),
				mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#filter(Predicate)} return just
	 * {@link ArrayList#ArrayList()}.</li>
	 * <li>Then calls {@link Mono#filter(Predicate)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
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

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono).filter(isA(Predicate.class));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link Mono} {@link Mono#map(Function)} return just {@code Data}.</li>
	 * <li>Then calls {@link Mono#map(Function)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
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

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
		createResult.expectComplete().verify();
		verify(eventStore).find(isA(InstanceId.class));
		verify(flux).collectList();
		verify(mono2).filter(isA(Predicate.class));
		verify(mono).map(isA(Function.class));
	}

	/**
	 * Test {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}.
	 * <ul>
	 * <li>Then calls
	 * {@link EventsourcingInstanceRepository#computeIfPresent(InstanceId, BiFunction)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link EndpointDetectionTrigger#detectEndpoints(InstanceEvent)}
	 */
	@Test
	public void testDetectEndpoints_thenCallsComputeIfPresent() {
		// Arrange
		Mono<Instance> mono = mock(Mono.class);
		Flux<?> source = Flux.fromIterable(new ArrayList<>());
		when(mono.then()).thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
		EventsourcingInstanceRepository repository = mock(EventsourcingInstanceRepository.class);
		when(repository.computeIfPresent(Mockito.<InstanceId>any(),
				Mockito.<BiFunction<InstanceId, Instance, Mono<Instance>>>any()))
			.thenReturn(mono);
		EndpointDetector endpointDetector = new EndpointDetector(repository, mock(EndpointDetectionStrategy.class));

		Flux<InstanceEvent> publisher = Flux.fromIterable(new ArrayList<>());
		EndpointDetectionTrigger endpointDetectionTrigger = new EndpointDetectionTrigger(endpointDetector, publisher);

		// Act
		endpointDetectionTrigger.detectEndpoints(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Assert
		verify(repository).computeIfPresent(isA(InstanceId.class), isA(BiFunction.class));
		verify(mono).then();
	}

}
