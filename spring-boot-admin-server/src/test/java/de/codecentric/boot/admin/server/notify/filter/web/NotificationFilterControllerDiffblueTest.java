package de.codecentric.boot.admin.server.notify.filter.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.filter.ApplicationNameNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.InstanceIdNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.NotificationFilter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = { NotificationFilterController.class })
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationFilterControllerDiffblueTest {

	@MockitoBean
	private FilteringNotifier filteringNotifier;

	@Autowired
	private NotificationFilterController notificationFilterController;

	/**
	 * Test
	 * {@link NotificationFilterController#NotificationFilterController(FilteringNotifier)}.
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#NotificationFilterController(FilteringNotifier)}
	 */
	@Test
	public void testNewNotificationFilterController() {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act and Assert
		assertTrue(new NotificationFilterController(
				new FilteringNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore())))
			.getFilters()
			.isEmpty());
	}

	/**
	 * Test {@link NotificationFilterController#getFilters()}.
	 * <p>
	 * Method under test: {@link NotificationFilterController#getFilters()}
	 */
	@Test
	public void testGetFilters() {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act and Assert
		assertTrue(new NotificationFilterController(
				new FilteringNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore())))
			.getFilters()
			.isEmpty());
	}

	/**
	 * Test {@link NotificationFilterController#getFilters()}.
	 * <ul>
	 * <li>Then calls {@link FilteringNotifier#getNotificationFilters()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link NotificationFilterController#getFilters()}
	 */
	@Test
	public void testGetFilters_thenCallsGetNotificationFilters() {
		// Arrange
		when(filteringNotifier.getNotificationFilters()).thenReturn(new HashMap<>());

		// Act
		Collection<NotificationFilter> actualFilters = notificationFilterController.getFilters();

		// Assert
		verify(filteringNotifier).getNotificationFilters();
		assertTrue(actualFilters.isEmpty());
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter() {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act and Assert
		Object body = new NotificationFilterController(
				new FilteringNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore())))
			.addFilter("42", "Name", 1L)
			.getBody();
		assertTrue(body instanceof InstanceIdNotificationFilter);
		InstanceId instanceId = ((InstanceIdNotificationFilter) body).getInstanceId();
		assertEquals("42", instanceId.getValue());
		assertEquals("42", instanceId.toString());
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <ul>
	 * <li>Given {@link FilteringNotifier}.</li>
	 * <li>When {@code null}.</li>
	 * <li>Then StatusCode return {@link HttpStatus}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter_givenFilteringNotifier_whenNull_thenStatusCodeReturnHttpStatus() {
		// Arrange and Act
		ResponseEntity<?> actualAddFilterResult = notificationFilterController.addFilter(null, null, null);

		// Assert
		HttpStatusCode statusCode = actualAddFilterResult.getStatusCode();
		assertTrue(statusCode instanceof HttpStatus);
		assertEquals("Either 'instanceId' or 'applicationName' must be set", actualAddFilterResult.getBody());
		assertEquals(400, actualAddFilterResult.getStatusCodeValue());
		assertEquals(HttpStatus.BAD_REQUEST, statusCode);
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <ul>
	 * <li>When {@code 42}.</li>
	 * <li>Then return Body InstanceId Value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter_when42_thenReturnBodyInstanceIdValueIs42() {
		// Arrange
		doNothing().when(filteringNotifier).addFilter(Mockito.<NotificationFilter>any());

		// Act
		ResponseEntity<?> actualAddFilterResult = notificationFilterController.addFilter("42", "Name", 1L);

		// Assert
		verify(filteringNotifier).addFilter(isA(NotificationFilter.class));
		Object body = actualAddFilterResult.getBody();
		assertTrue(body instanceof InstanceIdNotificationFilter);
		InstanceId instanceId = ((InstanceIdNotificationFilter) body).getInstanceId();
		assertEquals("42", instanceId.getValue());
		assertEquals("42", instanceId.toString());
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <ul>
	 * <li>When minus one.</li>
	 * <li>Then return Body Expiry is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter_whenMinusOne_thenReturnBodyExpiryIsNull() {
		// Arrange
		doNothing().when(filteringNotifier).addFilter(Mockito.<NotificationFilter>any());

		// Act
		ResponseEntity<?> actualAddFilterResult = notificationFilterController.addFilter("42", "Name", -1L);

		// Assert
		verify(filteringNotifier).addFilter(isA(NotificationFilter.class));
		Object body = actualAddFilterResult.getBody();
		assertTrue(body instanceof InstanceIdNotificationFilter);
		InstanceId instanceId = ((InstanceIdNotificationFilter) body).getInstanceId();
		assertEquals("42", instanceId.getValue());
		assertEquals("42", instanceId.toString());
		assertNull(((InstanceIdNotificationFilter) body).getExpiry());
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <ul>
	 * <li>When {@code not blank}.</li>
	 * <li>Then return Body InstanceId Value is {@code not blank}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter_whenNotBlank_thenReturnBodyInstanceIdValueIsNotBlank() {
		// Arrange
		doNothing().when(filteringNotifier).addFilter(Mockito.<NotificationFilter>any());

		// Act
		ResponseEntity<?> actualAddFilterResult = notificationFilterController.addFilter("not blank", "not blank",
				null);

		// Assert
		verify(filteringNotifier).addFilter(isA(NotificationFilter.class));
		Object body = actualAddFilterResult.getBody();
		assertTrue(body instanceof InstanceIdNotificationFilter);
		InstanceId instanceId = ((InstanceIdNotificationFilter) body).getInstanceId();
		assertEquals("not blank", instanceId.getValue());
		assertEquals("not blank", instanceId.toString());
		assertNull(((InstanceIdNotificationFilter) body).getExpiry());
	}

	/**
	 * Test {@link NotificationFilterController#addFilter(String, String, Long)}.
	 * <ul>
	 * <li>When space.</li>
	 * <li>Then Body return {@link ApplicationNameNotificationFilter}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link NotificationFilterController#addFilter(String, String, Long)}
	 */
	@Test
	public void testAddFilter_whenSpace_thenBodyReturnApplicationNameNotificationFilter() {
		// Arrange
		doNothing().when(filteringNotifier).addFilter(Mockito.<NotificationFilter>any());

		// Act
		ResponseEntity<?> actualAddFilterResult = notificationFilterController.addFilter(" ", "Name", 1L);

		// Assert
		verify(filteringNotifier).addFilter(isA(NotificationFilter.class));
		Object body = actualAddFilterResult.getBody();
		assertTrue(body instanceof ApplicationNameNotificationFilter);
		assertEquals("Name", ((ApplicationNameNotificationFilter) body).getApplicationName());
	}

	/**
	 * Test {@link NotificationFilterController#deleteFilter(String)}.
	 * <p>
	 * Method under test: {@link NotificationFilterController#deleteFilter(String)}
	 */
	@Test
	public void testDeleteFilter() {
		// Arrange
		Notifier delegate = mock(Notifier.class);

		// Act
		ResponseEntity<Void> actualDeleteFilterResult = new NotificationFilterController(
				new FilteringNotifier(delegate, new EventsourcingInstanceRepository(new InMemoryEventStore())))
			.deleteFilter("42");

		// Assert
		HttpStatusCode statusCode = actualDeleteFilterResult.getStatusCode();
		assertTrue(statusCode instanceof HttpStatus);
		assertNull(actualDeleteFilterResult.getBody());
		assertEquals(404, actualDeleteFilterResult.getStatusCodeValue());
		assertEquals(HttpStatus.NOT_FOUND, statusCode);
		assertFalse(actualDeleteFilterResult.hasBody());
		assertTrue(actualDeleteFilterResult.getHeaders().isEmpty());
	}

	/**
	 * Test {@link NotificationFilterController#deleteFilter(String)}.
	 * <ul>
	 * <li>Given {@link FilteringNotifier} {@link FilteringNotifier#removeFilter(String)}
	 * return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link NotificationFilterController#deleteFilter(String)}
	 */
	@Test
	public void testDeleteFilter_givenFilteringNotifierRemoveFilterReturnNull() {
		// Arrange
		when(filteringNotifier.removeFilter(Mockito.<String>any())).thenReturn(null);

		// Act
		ResponseEntity<Void> actualDeleteFilterResult = notificationFilterController.deleteFilter("42");

		// Assert
		verify(filteringNotifier).removeFilter(eq("42"));
		HttpStatusCode statusCode = actualDeleteFilterResult.getStatusCode();
		assertTrue(statusCode instanceof HttpStatus);
		assertNull(actualDeleteFilterResult.getBody());
		assertEquals(404, actualDeleteFilterResult.getStatusCodeValue());
		assertEquals(HttpStatus.NOT_FOUND, statusCode);
		assertFalse(actualDeleteFilterResult.hasBody());
		assertTrue(actualDeleteFilterResult.getHeaders().isEmpty());
	}

	/**
	 * Test {@link NotificationFilterController#deleteFilter(String)}.
	 * <ul>
	 * <li>Then return StatusCodeValue is two hundred.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link NotificationFilterController#deleteFilter(String)}
	 */
	@Test
	public void testDeleteFilter_thenReturnStatusCodeValueIsTwoHundred() {
		// Arrange
		when(filteringNotifier.removeFilter(Mockito.<String>any())).thenReturn(new ApplicationNameNotificationFilter(
				"Application Name", LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

		// Act
		ResponseEntity<Void> actualDeleteFilterResult = notificationFilterController.deleteFilter("42");

		// Assert
		verify(filteringNotifier).removeFilter(eq("42"));
		HttpStatusCode statusCode = actualDeleteFilterResult.getStatusCode();
		assertTrue(statusCode instanceof HttpStatus);
		assertNull(actualDeleteFilterResult.getBody());
		assertEquals(200, actualDeleteFilterResult.getStatusCodeValue());
		assertEquals(HttpStatus.OK, statusCode);
		assertFalse(actualDeleteFilterResult.hasBody());
		assertTrue(actualDeleteFilterResult.getHeaders().isEmpty());
	}

}
