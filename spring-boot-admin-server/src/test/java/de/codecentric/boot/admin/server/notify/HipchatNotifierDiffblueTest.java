package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.net.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { HipchatNotifier.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class HipchatNotifierDiffblueTest {

	@Autowired
	private HipchatNotifier hipchatNotifier;

	@MockitoBean
	private InstanceRepository instanceRepository;

	@MockitoBean
	private RestTemplate restTemplate;

	/**
	 * Test {@link HipchatNotifier#HipchatNotifier(InstanceRepository, RestTemplate)}.
	 * <p>
	 * Method under test:
	 * {@link HipchatNotifier#HipchatNotifier(InstanceRepository, RestTemplate)}
	 */
	@Test
	public void testNewHipchatNotifier() {
		// Arrange and Act
		HipchatNotifier actualHipchatNotifier = new HipchatNotifier(instanceRepository, mock(RestTemplate.class));

		// Assert
		assertEquals(
				"<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status"
						+ "}</strong>",
				actualHipchatNotifier.getDescription());
		assertNull(actualHipchatNotifier.getAuthToken());
		assertNull(actualHipchatNotifier.getRoomId());
		assertNull(actualHipchatNotifier.getUrl());
		assertFalse(actualHipchatNotifier.getNotify());
		assertFalse(actualHipchatNotifier.isNotify());
		assertTrue(actualHipchatNotifier.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualHipchatNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link HipchatNotifier#doNotify(InstanceEvent, Instance)}.
	 * <p>
	 * Method under test: {@link HipchatNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(hipchatNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

	/**
	 * Test {@link HipchatNotifier#buildUrl()}.
	 * <ul>
	 * <li>Given {@link HipchatNotifier} Url is
	 * {@link PagerdutyNotifier#DEFAULT_URI}.</li>
	 * <li>Then return a string.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HipchatNotifier#buildUrl()}
	 */
	@Test
	public void testBuildUrl_givenHipchatNotifierUrlIsDefault_uri_thenReturnAString() {
		// Arrange
		hipchatNotifier.setUrl(PagerdutyNotifier.DEFAULT_URI);

		// Act and Assert
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json/room/null/notification?auth"
				+ "_token=null", hipchatNotifier.buildUrl());
	}

	/**
	 * Test {@link HipchatNotifier#buildUrl()}.
	 * <ul>
	 * <li>Given {@link HipchatNotifier}.</li>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HipchatNotifier#buildUrl()}
	 */
	@Test
	public void testBuildUrl_givenHipchatNotifier_thenThrowIllegalStateException() {
		// Arrange, Act and Assert
		assertThrows(IllegalStateException.class, () -> hipchatNotifier.buildUrl());
	}

	/**
	 * Test {@link HipchatNotifier#getMessage(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then return {@code The characteristics of someone or something}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HipchatNotifier#getMessage(InstanceEvent, Instance)}
	 */
	@Test
	public void testGetMessage_thenReturnTheCharacteristicsOfSomeoneOrSomething() {
		// Arrange
		HipchatNotifier hipchatNotifier = new HipchatNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		hipchatNotifier.setDescription("The characteristics of someone or something");

		// Act and Assert
		assertEquals("The characteristics of someone or something",
				hipchatNotifier.getMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
	}

	/**
	 * Test {@link HipchatNotifier#getColor(InstanceEvent)}.
	 * <ul>
	 * <li>Given {@link IllegalStateException#IllegalStateException(String)} with
	 * {@code UP}.</li>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HipchatNotifier#getColor(InstanceEvent)}
	 */
	@Test
	public void testGetColor_givenIllegalStateExceptionWithUp_thenThrowIllegalStateException() {
		// Arrange
		InstanceStatusChangedEvent event = mock(InstanceStatusChangedEvent.class);
		when(event.getStatusInfo()).thenThrow(new IllegalStateException("UP"));

		// Act and Assert
		assertThrows(IllegalStateException.class, () -> hipchatNotifier.getColor(event));
		verify(event).getStatusInfo();
	}

	/**
	 * Test {@link HipchatNotifier#getColor(InstanceEvent)}.
	 * <ul>
	 * <li>When {@link InstanceId} with value is {@code 42}.</li>
	 * <li>Then return {@code gray}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HipchatNotifier#getColor(InstanceEvent)}
	 */
	@Test
	public void testGetColor_whenInstanceIdWithValueIs42_thenReturnGray() {
		// Arrange, Act and Assert
		assertEquals("gray", hipchatNotifier.getColor(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link HipchatNotifier#setAuthToken(String)}
	 * <li>{@link HipchatNotifier#setNotify(boolean)}
	 * <li>{@link HipchatNotifier#setRestTemplate(RestTemplate)}
	 * <li>{@link HipchatNotifier#setRoomId(String)}
	 * <li>{@link HipchatNotifier#setUrl(URI)}
	 * <li>{@link HipchatNotifier#getAuthToken()}
	 * <li>{@link HipchatNotifier#getNotify()}
	 * <li>{@link HipchatNotifier#getRoomId()}
	 * <li>{@link HipchatNotifier#getUrl()}
	 * <li>{@link HipchatNotifier#isNotify()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		HipchatNotifier hipchatNotifier = new HipchatNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

		// Act
		hipchatNotifier.setAuthToken("ABC123");
		hipchatNotifier.setNotify(true);
		hipchatNotifier.setRestTemplate(mock(RestTemplate.class));
		hipchatNotifier.setRoomId("42");
		URI url = PagerdutyNotifier.DEFAULT_URI;
		hipchatNotifier.setUrl(url);
		String actualAuthToken = hipchatNotifier.getAuthToken();
		boolean actualNotify = hipchatNotifier.getNotify();
		String actualRoomId = hipchatNotifier.getRoomId();
		URI actualUrl = hipchatNotifier.getUrl();
		boolean actualIsNotifyResult = hipchatNotifier.isNotify();

		// Assert
		assertEquals("42", actualRoomId);
		assertEquals("ABC123", actualAuthToken);
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualUrl.toString());
		assertTrue(actualNotify);
		assertTrue(actualIsNotifyResult);
		assertSame(url, actualUrl);
	}

	/**
	 * Test {@link HipchatNotifier#getDescription()}.
	 * <p>
	 * Method under test: {@link HipchatNotifier#getDescription()}
	 */
	@Test
	public void testGetDescription() {
		// Arrange, Act and Assert
		assertEquals(
				"<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status"
						+ "}</strong>",
				hipchatNotifier.getDescription());
	}

	/**
	 * Test {@link HipchatNotifier#setDescription(String)}.
	 * <p>
	 * Method under test: {@link HipchatNotifier#setDescription(String)}
	 */
	@Test
	public void testSetDescription() {
		// Arrange and Act
		hipchatNotifier.setDescription("The characteristics of someone or something");

		// Assert
		assertEquals("The characteristics of someone or something", hipchatNotifier.getDescription());
	}

}
