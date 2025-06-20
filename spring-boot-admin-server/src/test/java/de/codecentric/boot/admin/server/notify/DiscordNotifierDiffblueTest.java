package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { DiscordNotifier.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class DiscordNotifierDiffblueTest {

	@Autowired
	private DiscordNotifier discordNotifier;

	@MockitoBean
	private InstanceRepository instanceRepository;

	@MockitoBean
	private RestTemplate restTemplate;

	/**
	 * Test {@link DiscordNotifier#DiscordNotifier(InstanceRepository, RestTemplate)}.
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifier#DiscordNotifier(InstanceRepository, RestTemplate)}
	 */
	@Test
	public void testNewDiscordNotifier() {
		// Arrange and Act
		DiscordNotifier actualDiscordNotifier = new DiscordNotifier(instanceRepository, mock(RestTemplate.class));

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualDiscordNotifier.getMessage());
		assertNull(actualDiscordNotifier.getAvatarUrl());
		assertNull(actualDiscordNotifier.getUsername());
		assertNull(actualDiscordNotifier.getWebhookUrl());
		assertFalse(actualDiscordNotifier.isTts());
		assertTrue(actualDiscordNotifier.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualDiscordNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link DiscordNotifier#doNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Given {@link DiscordNotifier} WebhookUrl is
	 * {@link PagerdutyNotifier#DEFAULT_URI}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DiscordNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify_givenDiscordNotifierWebhookUrlIsDefault_uri() throws AssertionError {
		// Arrange
		discordNotifier.setWebhookUrl(PagerdutyNotifier.DEFAULT_URI);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(discordNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

	/**
	 * Test {@link DiscordNotifier#doNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Given {@link DiscordNotifier} WebhookUrl is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DiscordNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify_givenDiscordNotifierWebhookUrlIsNull() throws AssertionError {
		// Arrange
		discordNotifier.setWebhookUrl(null);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(discordNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

	/**
	 * Test {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then return Body {@code avatar_url} is
	 * {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}
	 */
	@Test
	public void testCreateDiscordNotification_thenReturnBodyAvatarUrlIsHttpsExampleOrgExample() {
		// Arrange
		DiscordNotifier discordNotifier = new DiscordNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		discordNotifier.setMessage("Not all who wander are lost");
		discordNotifier.setAvatarUrl("https://example.org/example");
		discordNotifier.setUsername(null);

		// Act
		Object actualCreateDiscordNotificationResult = discordNotifier
			.createDiscordNotification(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

		// Assert
		Object body = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getBody();
		assertTrue(body instanceof Map);
		assertTrue(actualCreateDiscordNotificationResult instanceof HttpEntity);
		assertEquals(3, ((Map<String, Object>) body).size());
		assertEquals("Not all who wander are lost", ((Map<String, Object>) body).get("content"));
		HttpHeaders headers = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getHeaders();
		assertEquals(2, headers.size());
		List<String> getResult = headers.get(HttpHeaders.USER_AGENT);
		assertEquals(1, getResult.size());
		assertEquals("RestTemplate", getResult.get(0));
		List<String> getResult2 = headers.get(HttpHeaders.CONTENT_TYPE);
		assertEquals(1, getResult2.size());
		assertEquals("application/json", getResult2.get(0));
		assertEquals("https://example.org/example", ((Map<String, Object>) body).get("avatar_url"));
		assertFalse((Boolean) ((Map<String, Object>) body).get("tts"));
		assertTrue(((HttpEntity<Object>) actualCreateDiscordNotificationResult).hasBody());
	}

	/**
	 * Test {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then return Body size is two.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}
	 */
	@Test
	public void testCreateDiscordNotification_thenReturnBodySizeIsTwo() {
		// Arrange
		DiscordNotifier discordNotifier = new DiscordNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		discordNotifier.setMessage("Not all who wander are lost");
		discordNotifier.setAvatarUrl(null);
		discordNotifier.setUsername(null);

		// Act
		Object actualCreateDiscordNotificationResult = discordNotifier
			.createDiscordNotification(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

		// Assert
		Object body = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getBody();
		assertTrue(body instanceof Map);
		assertTrue(actualCreateDiscordNotificationResult instanceof HttpEntity);
		assertEquals(2, ((Map<String, Object>) body).size());
		assertEquals("Not all who wander are lost", ((Map<String, Object>) body).get("content"));
		HttpHeaders headers = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getHeaders();
		assertEquals(2, headers.size());
		List<String> getResult = headers.get(HttpHeaders.USER_AGENT);
		assertEquals(1, getResult.size());
		assertEquals("RestTemplate", getResult.get(0));
		List<String> getResult2 = headers.get(HttpHeaders.CONTENT_TYPE);
		assertEquals(1, getResult2.size());
		assertEquals("application/json", getResult2.get(0));
		assertFalse((Boolean) ((Map<String, Object>) body).get("tts"));
		assertTrue(((HttpEntity<Object>) actualCreateDiscordNotificationResult).hasBody());
	}

	/**
	 * Test {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then return Body {@code username} is {@code janedoe}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifier#createDiscordNotification(InstanceEvent, Instance)}
	 */
	@Test
	public void testCreateDiscordNotification_thenReturnBodyUsernameIsJanedoe() {
		// Arrange
		DiscordNotifier discordNotifier = new DiscordNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		discordNotifier.setMessage("Not all who wander are lost");
		discordNotifier.setAvatarUrl(null);
		discordNotifier.setUsername("janedoe");

		// Act
		Object actualCreateDiscordNotificationResult = discordNotifier
			.createDiscordNotification(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

		// Assert
		Object body = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getBody();
		assertTrue(body instanceof Map);
		assertTrue(actualCreateDiscordNotificationResult instanceof HttpEntity);
		assertEquals(3, ((Map<String, Object>) body).size());
		assertEquals("Not all who wander are lost", ((Map<String, Object>) body).get("content"));
		HttpHeaders headers = ((HttpEntity<Object>) actualCreateDiscordNotificationResult).getHeaders();
		assertEquals(2, headers.size());
		List<String> getResult = headers.get(HttpHeaders.USER_AGENT);
		assertEquals(1, getResult.size());
		assertEquals("RestTemplate", getResult.get(0));
		List<String> getResult2 = headers.get(HttpHeaders.CONTENT_TYPE);
		assertEquals(1, getResult2.size());
		assertEquals("application/json", getResult2.get(0));
		assertEquals("janedoe", ((Map<String, Object>) body).get("username"));
		assertFalse((Boolean) ((Map<String, Object>) body).get("tts"));
		assertTrue(((HttpEntity<Object>) actualCreateDiscordNotificationResult).hasBody());
	}

	/**
	 * Test {@link DiscordNotifier#createContent(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Then return {@code Not all who wander are lost}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DiscordNotifier#createContent(InstanceEvent, Instance)}
	 */
	@Test
	public void testCreateContent_thenReturnNotAllWhoWanderAreLost() {
		// Arrange
		DiscordNotifier discordNotifier = new DiscordNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
		discordNotifier.setMessage("Not all who wander are lost");

		// Act and Assert
		assertEquals("Not all who wander are lost",
				discordNotifier.createContent(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link DiscordNotifier#setAvatarUrl(String)}
	 * <li>{@link DiscordNotifier#setRestTemplate(RestTemplate)}
	 * <li>{@link DiscordNotifier#setTts(boolean)}
	 * <li>{@link DiscordNotifier#setUsername(String)}
	 * <li>{@link DiscordNotifier#setWebhookUrl(URI)}
	 * <li>{@link DiscordNotifier#getAvatarUrl()}
	 * <li>{@link DiscordNotifier#getUsername()}
	 * <li>{@link DiscordNotifier#getWebhookUrl()}
	 * <li>{@link DiscordNotifier#isTts()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		DiscordNotifier discordNotifier = new DiscordNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

		// Act
		discordNotifier.setAvatarUrl("https://example.org/example");
		discordNotifier.setRestTemplate(mock(RestTemplate.class));
		discordNotifier.setTts(true);
		discordNotifier.setUsername("janedoe");
		URI webhookUrl = PagerdutyNotifier.DEFAULT_URI;
		discordNotifier.setWebhookUrl(webhookUrl);
		String actualAvatarUrl = discordNotifier.getAvatarUrl();
		String actualUsername = discordNotifier.getUsername();
		URI actualWebhookUrl = discordNotifier.getWebhookUrl();
		boolean actualIsTtsResult = discordNotifier.isTts();

		// Assert
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualWebhookUrl.toString());
		assertEquals("https://example.org/example", actualAvatarUrl);
		assertEquals("janedoe", actualUsername);
		assertTrue(actualIsTtsResult);
		assertSame(webhookUrl, actualWebhookUrl);
	}

	/**
	 * Test {@link DiscordNotifier#getMessage()}.
	 * <p>
	 * Method under test: {@link DiscordNotifier#getMessage()}
	 */
	@Test
	public void testGetMessage() {
		// Arrange, Act and Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				discordNotifier.getMessage());
	}

	/**
	 * Test {@link DiscordNotifier#setMessage(String)}.
	 * <p>
	 * Method under test: {@link DiscordNotifier#setMessage(String)}
	 */
	@Test
	public void testSetMessage() {
		// Arrange and Act
		discordNotifier.setMessage("Not all who wander are lost");

		// Assert
		assertEquals("Not all who wander are lost", discordNotifier.getMessage());
	}

}
