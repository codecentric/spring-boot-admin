package de.codecentric.boot.admin.registry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

@SuppressWarnings("rawtypes")
public class StatusUpdaterTest {

	private StatusUpdater updater;
	private SimpleApplicationStore store;
	private RestTemplate template;
	private ApplicationEventPublisher publisher;

	@Before
	public void setup() {
		store = new SimpleApplicationStore();
		template = mock(RestTemplate.class);
		updater = new StatusUpdater(template, store);
		publisher = mock(ApplicationEventPublisher.class);
		updater.setApplicationEventPublisher(publisher);
	}

	@Test
	public void test_update_statusChanged() {
		when(template.getForEntity("health", Map.class)).thenReturn(
				ResponseEntity.ok().<Map> body(
						Collections.singletonMap("status", "UP")));

		updater.updateStatus(Application.create("foo").withId("id")
				.withHealthUrl("health").build());

		Application app = store.find("id");

		assertThat(app.getStatusInfo().getStatus(), is("UP"));
		verify(publisher).publishEvent(
				argThat(isA(ClientApplicationStatusChangedEvent.class)));
	}

	@Test
	public void test_update_statusUnchanged() {
		when(template.getForEntity("health", Map.class))
		.thenReturn(
				ResponseEntity.<Map> ok(Collections.singletonMap("status",
						"UNKNOWN")));

		updater.updateStatus(Application.create("foo").withId("id")
				.withHealthUrl("health").build());

		verify(publisher, never()).publishEvent(
				argThat(isA(ClientApplicationStatusChangedEvent.class)));
	}

	@Test
	public void test_update_noBody() {
		// HTTP 200 - UP
		when(template.getForEntity("health", Map.class)).thenReturn(
				ResponseEntity.<Map> ok(null));

		updater.updateStatus(Application.create("foo").withId("id")
				.withHealthUrl("health").build());

		assertThat(store.find("id").getStatusInfo().getStatus(), is("UP"));

		// HTTP != 200 - DOWN
		when(template.getForEntity("health", Map.class)).thenReturn(
				ResponseEntity.status(503).<Map> body(null));

		updater.updateStatus(Application.create("foo").withId("id")
				.withHealthUrl("health").build());

		assertThat(store.find("id").getStatusInfo().getStatus(), is("DOWN"));
	}

	@Test
	public void test_update_offline() {
		when(template.getForEntity("health", Map.class)).thenThrow(
				new ResourceAccessException("error"));

		updater.updateStatus(Application.create("foo").withId("id")
				.withHealthUrl("health").build());

		assertThat(store.find("id").getStatusInfo().getStatus(), is("OFFLINE"));
	}

	@Test
	public void test_updateStatusForApplications() {
		Application app1 = Application.create("foo").withId("id-1")
				.withHealthUrl("health-1").build();
		store.save(app1);

		Application app2 = Application.create("foo").withId("id-2")
				.withHealthUrl("health-2")
				.withStatusInfo(StatusInfo.valueOf("UP", 0L)).build();
		store.save(app2);

		when(template.getForEntity("health-2", Map.class)).thenReturn(
				ResponseEntity.<Map> ok(null));


		updater.updateStatusForAllApplications();

		verify(template, never()).getForEntity("health-1", Map.class);
	}

}
