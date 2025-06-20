package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import de.codecentric.boot.admin.server.notify.FeiShuNotifier.Card;
import de.codecentric.boot.admin.server.notify.FeiShuNotifier.MessageType;
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

@ContextConfiguration(classes = { FeiShuNotifier.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class FeiShuNotifierDiffblueTest {

	@Autowired
	private FeiShuNotifier feiShuNotifier;

	@MockitoBean
	private InstanceRepository instanceRepository;

	@MockitoBean
	private RestTemplate restTemplate;

	/**
	 * Test Card getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link Card#Card(FeiShuNotifier)}
	 * <li>{@link Card#setThemeColor(String)}
	 * <li>{@link Card#setTitle(String)}
	 * <li>{@link Card#getThemeColor()}
	 * <li>{@link Card#getTitle()}
	 * </ul>
	 */
	@Test
	public void testCardGettersAndSetters() {
		// Arrange and Act
		Card actualCard = new FeiShuNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(RestTemplate.class)).new Card();
		actualCard.setThemeColor("Theme Color");
		actualCard.setTitle("Dr");
		String actualThemeColor = actualCard.getThemeColor();

		// Assert
		assertEquals("Dr", actualCard.getTitle());
		assertEquals("Theme Color", actualThemeColor);
	}

	/**
	 * Test {@link FeiShuNotifier#FeiShuNotifier(InstanceRepository, RestTemplate)}.
	 * <p>
	 * Method under test:
	 * {@link FeiShuNotifier#FeiShuNotifier(InstanceRepository, RestTemplate)}
	 */
	@Test
	public void testNewFeiShuNotifier() {
		// Arrange and Act
		FeiShuNotifier actualFeiShuNotifier = new FeiShuNotifier(instanceRepository, mock(RestTemplate.class));

		// Assert
		Card card = actualFeiShuNotifier.getCard();
		assertEquals("Codecentric's Spring Boot Admin notice", card.getTitle());
		assertEquals(
				"ServiceName: #{instance.registration.name}(#{instance.id}) \n"
						+ "ServiceUrl: #{instance.registration.serviceUrl} \n"
						+ "Status: changed status from [#{lastStatus}] to [#{event.statusInfo.status}]",
				actualFeiShuNotifier.getMessage());
		assertEquals("red", card.getThemeColor());
		assertNull(actualFeiShuNotifier.getSecret());
		assertNull(actualFeiShuNotifier.getWebhookUrl());
		assertEquals(MessageType.interactive, actualFeiShuNotifier.getMessageType());
		assertTrue(actualFeiShuNotifier.isEnabled());
		assertTrue(actualFeiShuNotifier.isAtAll());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualFeiShuNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link FeiShuNotifier#doNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Given {@link FeiShuNotifier} WebhookUrl is
	 * {@link PagerdutyNotifier#DEFAULT_URI}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link FeiShuNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify_givenFeiShuNotifierWebhookUrlIsDefault_uri() throws AssertionError {
		// Arrange
		feiShuNotifier.setWebhookUrl(PagerdutyNotifier.DEFAULT_URI);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(feiShuNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

	/**
	 * Test {@link FeiShuNotifier#doNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>Given {@link FeiShuNotifier} WebhookUrl is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link FeiShuNotifier#doNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testDoNotify_givenFeiShuNotifierWebhookUrlIsNull() throws AssertionError {
		// Arrange
		feiShuNotifier.setWebhookUrl(null);

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier
			.create(feiShuNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
		createResult.expectError().verify();
	}

	/**
	 * Test {@link FeiShuNotifier#getMessage()}.
	 * <p>
	 * Method under test: {@link FeiShuNotifier#getMessage()}
	 */
	@Test
	public void testGetMessage() {
		// Arrange, Act and Assert
		assertEquals(
				"ServiceName: #{instance.registration.name}(#{instance.id}) \n"
						+ "ServiceUrl: #{instance.registration.serviceUrl} \n"
						+ "Status: changed status from [#{lastStatus}] to [#{event.statusInfo.status}]",
				feiShuNotifier.getMessage());
	}

	/**
	 * Test {@link FeiShuNotifier#setMessage(String)}.
	 * <p>
	 * Method under test: {@link FeiShuNotifier#setMessage(String)}
	 */
	@Test
	public void testSetMessage() {
		// Arrange and Act
		feiShuNotifier.setMessage("Not all who wander are lost");

		// Assert
		assertEquals("Not all who wander are lost", feiShuNotifier.getMessage());
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link FeiShuNotifier#setAtAll(boolean)}
	 * <li>{@link FeiShuNotifier#setCard(Card)}
	 * <li>{@link FeiShuNotifier#setMessageType(MessageType)}
	 * <li>{@link FeiShuNotifier#setRestTemplate(RestTemplate)}
	 * <li>{@link FeiShuNotifier#setSecret(String)}
	 * <li>{@link FeiShuNotifier#setWebhookUrl(URI)}
	 * <li>{@link FeiShuNotifier#getCard()}
	 * <li>{@link FeiShuNotifier#getMessageType()}
	 * <li>{@link FeiShuNotifier#getSecret()}
	 * <li>{@link FeiShuNotifier#getWebhookUrl()}
	 * <li>{@link FeiShuNotifier#isAtAll()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		FeiShuNotifier feiShuNotifier = new FeiShuNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

		// Act
		feiShuNotifier.setAtAll(true);
		Card card = new FeiShuNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
				mock(RestTemplate.class)).new Card();
		feiShuNotifier.setCard(card);
		feiShuNotifier.setMessageType(MessageType.text);
		feiShuNotifier.setRestTemplate(mock(RestTemplate.class));
		feiShuNotifier.setSecret("Secret");
		URI webhookUrl = PagerdutyNotifier.DEFAULT_URI;
		feiShuNotifier.setWebhookUrl(webhookUrl);
		Card actualCard = feiShuNotifier.getCard();
		MessageType actualMessageType = feiShuNotifier.getMessageType();
		String actualSecret = feiShuNotifier.getSecret();
		URI actualWebhookUrl = feiShuNotifier.getWebhookUrl();
		boolean actualIsAtAllResult = feiShuNotifier.isAtAll();

		// Assert
		assertEquals("Secret", actualSecret);
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualWebhookUrl.toString());
		assertEquals(MessageType.text, actualMessageType);
		assertTrue(actualIsAtAllResult);
		assertSame(card, actualCard);
		assertSame(webhookUrl, actualWebhookUrl);
	}

}
