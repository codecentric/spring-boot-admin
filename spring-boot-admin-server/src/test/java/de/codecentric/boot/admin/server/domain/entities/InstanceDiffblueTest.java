package de.codecentric.boot.admin.server.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class InstanceDiffblueTest {

	/**
	 * Test {@link Instance#create(InstanceId)}.
	 * <ul>
	 * <li>When {@link InstanceId} with value is {@code 42}.</li>
	 * <li>Then return StatusInfo Status is {@code UNKNOWN}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#create(InstanceId)}
	 */
	@Test
	public void testCreate_whenInstanceIdWithValueIs42_thenReturnStatusInfoStatusIsUnknown() {
		// Arrange
		InstanceId id = InstanceId.of("42");

		// Act
		Instance actualCreateResult = Instance.create(id);

		// Assert
		StatusInfo statusInfo = actualCreateResult.getStatusInfo();
		assertEquals("UNKNOWN", statusInfo.getStatus());
		assertNull(actualCreateResult.getBuildVersion());
		assertEquals(-1L, actualCreateResult.getVersion());
		Instant statusTimestamp = actualCreateResult.getStatusTimestamp();
		assertEquals(0, statusTimestamp.getNano());
		assertEquals(0L, statusTimestamp.getEpochSecond());
		assertFalse(actualCreateResult.isRegistered());
		assertFalse(statusInfo.isDown());
		assertFalse(statusInfo.isOffline());
		assertFalse(statusInfo.isUp());
		assertTrue(statusInfo.isUnknown());
		assertTrue(actualCreateResult.getUnsavedEvents().isEmpty());
		Map<String, Object> values = actualCreateResult.getInfo().getValues();
		assertTrue(values.isEmpty());
		assertTrue(statusInfo.getDetails().isEmpty());
		assertSame(values, actualCreateResult.getTags().getValues());
		assertSame(id, actualCreateResult.getId());
	}

	/**
	 * Test {@link Instance#deregister()}.
	 * <p>
	 * Method under test: {@link Instance#deregister()}
	 */
	@Test
	public void testDeregister() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		// Act and Assert
		assertSame(createResult, createResult.deregister());
	}

	/**
	 * Test {@link Instance#withInfo(Info)}.
	 * <p>
	 * Method under test: {@link Instance#withInfo(Info)}
	 */
	@Test
	public void testWithInfo() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		// Act and Assert
		assertSame(createResult, createResult.withInfo(Info.empty()));
	}

	/**
	 * Test {@link Instance#withEndpoints(Endpoints)}.
	 * <ul>
	 * <li>Then return UnsavedEvents size is one.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#withEndpoints(Endpoints)}
	 */
	@Test
	public void testWithEndpoints_thenReturnUnsavedEventsSizeIsOne() {
		// Arrange
		InstanceId id = InstanceId.of("42");
		Instance createResult = Instance.create(id);
		Endpoints endpoints = Endpoints.single("42", "https://example.org/example");

		// Act
		Instance actualWithEndpointsResult = createResult.withEndpoints(endpoints);

		// Assert
		List<InstanceEvent> unsavedEvents = actualWithEndpointsResult.getUnsavedEvents();
		assertEquals(1, unsavedEvents.size());
		InstanceEvent getResult = unsavedEvents.get(0);
		assertTrue(getResult instanceof InstanceEndpointsDetectedEvent);
		assertEquals("ENDPOINTS_DETECTED", getResult.getType());
		assertEquals(0L, actualWithEndpointsResult.getVersion());
		assertEquals(0L, getResult.getVersion());
		assertSame(endpoints, actualWithEndpointsResult.getEndpoints());
		assertSame(endpoints, ((InstanceEndpointsDetectedEvent) getResult).getEndpoints());
		assertSame(id, getResult.getInstance());
	}

	/**
	 * Test {@link Instance#withEndpoints(Endpoints)}.
	 * <ul>
	 * <li>When empty.</li>
	 * <li>Then return create {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#withEndpoints(Endpoints)}
	 */
	@Test
	public void testWithEndpoints_whenEmpty_thenReturnCreateInstanceIdWithValueIs42() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		// Act and Assert
		assertSame(createResult, createResult.withEndpoints(Endpoints.empty()));
	}

	/**
	 * Test {@link Instance#isRegistered()}.
	 * <p>
	 * Method under test: {@link Instance#isRegistered()}
	 */
	@Test
	public void testIsRegistered() {
		// Arrange, Act and Assert
		assertFalse(Instance.create(InstanceId.of("42")).isRegistered());
	}

	/**
	 * Test {@link Instance#getRegistration()}.
	 * <p>
	 * Method under test: {@link Instance#getRegistration()}
	 */
	@Test
	public void testGetRegistration() {
		// Arrange, Act and Assert
		assertThrows(IllegalStateException.class, () -> Instance.create(InstanceId.of("42")).getRegistration());
	}

	/**
	 * Test {@link Instance#getUnsavedEvents()}.
	 * <p>
	 * Method under test: {@link Instance#getUnsavedEvents()}
	 */
	@Test
	public void testGetUnsavedEvents() {
		// Arrange, Act and Assert
		assertTrue(Instance.create(InstanceId.of("42")).getUnsavedEvents().isEmpty());
	}

	/**
	 * Test {@link Instance#clearUnsavedEvents()}.
	 * <p>
	 * Method under test: {@link Instance#clearUnsavedEvents()}
	 */
	@Test
	public void testClearUnsavedEvents() {
		// Arrange
		InstanceId id = InstanceId.of("42");

		// Act
		Instance actualClearUnsavedEventsResult = Instance.create(id).clearUnsavedEvents();

		// Assert
		StatusInfo statusInfo = actualClearUnsavedEventsResult.getStatusInfo();
		assertEquals("UNKNOWN", statusInfo.getStatus());
		assertNull(actualClearUnsavedEventsResult.getBuildVersion());
		assertEquals(-1L, actualClearUnsavedEventsResult.getVersion());
		Instant statusTimestamp = actualClearUnsavedEventsResult.getStatusTimestamp();
		assertEquals(0, statusTimestamp.getNano());
		assertEquals(0L, statusTimestamp.getEpochSecond());
		assertFalse(actualClearUnsavedEventsResult.isRegistered());
		assertFalse(statusInfo.isDown());
		assertFalse(statusInfo.isOffline());
		assertFalse(statusInfo.isUp());
		assertTrue(statusInfo.isUnknown());
		assertTrue(actualClearUnsavedEventsResult.getUnsavedEvents().isEmpty());
		Map<String, Object> values = actualClearUnsavedEventsResult.getInfo().getValues();
		assertTrue(values.isEmpty());
		assertTrue(statusInfo.getDetails().isEmpty());
		assertSame(values, actualClearUnsavedEventsResult.getTags().getValues());
		assertSame(id, actualClearUnsavedEventsResult.getId());
	}

	/**
	 * Test {@link Instance#apply(InstanceEvent)} with {@code event}.
	 * <p>
	 * Method under test: {@link Instance#apply(InstanceEvent)}
	 */
	@Test
	public void testApplyWithEvent() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));
		InstanceId instance = InstanceId.of("42");
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		Registration registration2 = createResult
			.apply(new InstanceRegistrationUpdatedEvent(instance, 1L, registration))
			.getRegistration();
		assertEquals("Name", registration2.getName());
		assertEquals("Source", registration2.getSource());
		assertEquals("https://example.org/example", registration2.getHealthUrl());
		assertEquals("https://example.org/example", registration2.getManagementUrl());
		assertEquals("https://example.org/example", registration2.getServiceUrl());
		assertTrue(registration2.getMetadata().isEmpty());
	}

	/**
	 * Test {@link Instance#apply(InstanceEvent)} with {@code event}.
	 * <ul>
	 * <li>Then return Endpoints is empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(InstanceEvent)}
	 */
	@Test
	public void testApplyWithEvent_thenReturnEndpointsIsEmpty() {
		// Arrange
		InstanceId id = InstanceId.of("42");
		Instance createResult = Instance.create(id);
		InstanceId instance = InstanceId.of("42");
		Endpoints endpoints = Endpoints.empty();

		// Act
		Instance actualApplyResult = createResult.apply(new InstanceEndpointsDetectedEvent(instance, 1L, endpoints));

		// Assert
		assertNull(actualApplyResult.getBuildVersion());
		assertEquals(1L, actualApplyResult.getVersion());
		assertFalse(actualApplyResult.isRegistered());
		assertTrue(actualApplyResult.getUnsavedEvents().isEmpty());
		assertSame(endpoints, actualApplyResult.getEndpoints());
		assertSame(id, actualApplyResult.getId());
	}

	/**
	 * Test {@link Instance#apply(InstanceEvent)} with {@code event}.
	 * <ul>
	 * <li>Then return Id Value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(InstanceEvent)}
	 */
	@Test
	public void testApplyWithEvent_thenReturnIdValueIs42() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		// Act
		Instance actualApplyResult = createResult.apply(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Assert
		InstanceId id = actualApplyResult.getId();
		assertEquals("42", id.getValue());
		assertEquals("42", id.toString());
		StatusInfo statusInfo = actualApplyResult.getStatusInfo();
		assertEquals("UNKNOWN", statusInfo.getStatus());
		assertFalse(statusInfo.isDown());
		assertFalse(statusInfo.isOffline());
		assertFalse(statusInfo.isUp());
		assertTrue(statusInfo.isUnknown());
		Map<String, Object> values = actualApplyResult.getInfo().getValues();
		assertTrue(values.isEmpty());
		assertTrue(statusInfo.getDetails().isEmpty());
		assertSame(values, actualApplyResult.getTags().getValues());
	}

	/**
	 * Test {@link Instance#apply(InstanceEvent)} with {@code event}.
	 * <ul>
	 * <li>Then return Info is empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(InstanceEvent)}
	 */
	@Test
	public void testApplyWithEvent_thenReturnInfoIsEmpty() {
		// Arrange
		InstanceId id = InstanceId.of("42");
		Instance createResult = Instance.create(id);
		InstanceId instance = InstanceId.of("42");
		Info info = Info.empty();

		// Act
		Instance actualApplyResult = createResult.apply(new InstanceInfoChangedEvent(instance, 1L, info));

		// Assert
		assertNull(actualApplyResult.getBuildVersion());
		assertEquals(1L, actualApplyResult.getVersion());
		assertFalse(actualApplyResult.isRegistered());
		assertTrue(actualApplyResult.getUnsavedEvents().isEmpty());
		assertSame(info, actualApplyResult.getInfo());
		assertSame(id, actualApplyResult.getId());
	}

	/**
	 * Test {@link Instance#apply(InstanceEvent)} with {@code event}.
	 * <ul>
	 * <li>Then return Registered.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(InstanceEvent)}
	 */
	@Test
	public void testApplyWithEvent_thenReturnRegistered() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));
		InstanceId instance = InstanceId.of("42");
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act
		Instance actualApplyResult = createResult.apply(new InstanceRegisteredEvent(instance, 1L, registration));

		// Assert
		Registration registration2 = actualApplyResult.getRegistration();
		assertEquals("Name", registration2.getName());
		assertEquals("Source", registration2.getSource());
		assertEquals("https://example.org/example", registration2.getHealthUrl());
		assertEquals("https://example.org/example", registration2.getManagementUrl());
		assertEquals("https://example.org/example", registration2.getServiceUrl());
		assertTrue(actualApplyResult.isRegistered());
		assertTrue(registration2.getMetadata().isEmpty());
	}

	/**
	 * Test {@link Instance#apply(Collection)} with {@code events}.
	 * <ul>
	 * <li>Then return Id Value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(Collection)}
	 */
	@Test
	public void testApplyWithEvents_thenReturnIdValueIs42() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		ArrayList<InstanceEvent> events = new ArrayList<>();
		events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Act
		Instance actualApplyResult = createResult.apply(events);

		// Assert
		InstanceId id = actualApplyResult.getId();
		assertEquals("42", id.getValue());
		assertEquals("42", id.toString());
		StatusInfo statusInfo = actualApplyResult.getStatusInfo();
		assertEquals("UNKNOWN", statusInfo.getStatus());
		assertEquals(1L, actualApplyResult.getVersion());
		assertFalse(statusInfo.isDown());
		assertFalse(statusInfo.isOffline());
		assertFalse(statusInfo.isUp());
		assertTrue(statusInfo.isUnknown());
		Map<String, Object> values = actualApplyResult.getInfo().getValues();
		assertTrue(values.isEmpty());
		assertTrue(statusInfo.getDetails().isEmpty());
		assertSame(values, actualApplyResult.getTags().getValues());
	}

	/**
	 * Test {@link Instance#apply(Collection)} with {@code events}.
	 * <ul>
	 * <li>When {@link ArrayList#ArrayList()}.</li>
	 * <li>Then return create {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Instance#apply(Collection)}
	 */
	@Test
	public void testApplyWithEvents_whenArrayList_thenReturnCreateInstanceIdWithValueIs42() {
		// Arrange
		Instance createResult = Instance.create(InstanceId.of("42"));

		// Act and Assert
		assertSame(createResult, createResult.apply(new ArrayList<>()));
	}

}
