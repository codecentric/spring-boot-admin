package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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

@ContextConfiguration(classes = {SlackNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class SlackNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private RestTemplate restTemplate;

  @Autowired
  private SlackNotifier slackNotifier;

  /**
   * Test {@link SlackNotifier#SlackNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link SlackNotifier#SlackNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewSlackNotifier() {
    // Arrange and Act
    SlackNotifier actualSlackNotifier = new SlackNotifier(instanceRepository, mock(RestTemplate.class));

    // Assert
    assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
        actualSlackNotifier.getMessage());
    assertEquals("Spring Boot Admin", actualSlackNotifier.getUsername());
    assertNull(actualSlackNotifier.getChannel());
    assertNull(actualSlackNotifier.getIcon());
    assertNull(actualSlackNotifier.getWebhookUrl());
    assertTrue(actualSlackNotifier.isEnabled());
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualSlackNotifier.getIgnoreChanges());
  }

  /**
   * Test {@link SlackNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link SlackNotifier} WebhookUrl is {@link PagerdutyNotifier#DEFAULT_URI}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenSlackNotifierWebhookUrlIsDefault_uri() throws AssertionError {
    // Arrange
    slackNotifier.setWebhookUrl(PagerdutyNotifier.DEFAULT_URI);

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(slackNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link SlackNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link SlackNotifier} WebhookUrl is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenSlackNotifierWebhookUrlIsNull() throws AssertionError {
    // Arrange
    slackNotifier.setWebhookUrl(null);

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(slackNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link SlackNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_thenReturnNotAllWhoWanderAreLost() {
    // Arrange
    SlackNotifier slackNotifier = new SlackNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));
    slackNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        slackNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test {@link SlackNotifier#getColor(InstanceEvent)}.
   * <ul>
   *   <li>Given {@link IllegalStateException#IllegalStateException(String)} with {@code UP}.</li>
   *   <li>Then throw {@link IllegalStateException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#getColor(InstanceEvent)}
   */
  @Test
  public void testGetColor_givenIllegalStateExceptionWithUp_thenThrowIllegalStateException() {
    // Arrange
    InstanceStatusChangedEvent event = mock(InstanceStatusChangedEvent.class);
    when(event.getStatusInfo()).thenThrow(new IllegalStateException("UP"));

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> slackNotifier.getColor(event));
    verify(event).getStatusInfo();
  }

  /**
   * Test {@link SlackNotifier#getColor(InstanceEvent)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then return {@code #439FE0}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#getColor(InstanceEvent)}
   */
  @Test
  public void testGetColor_whenInstanceIdWithValueIs42_thenReturn439fe0() {
    // Arrange, Act and Assert
    assertEquals("#439FE0", slackNotifier.getColor(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link SlackNotifier#setChannel(String)}
   *   <li>{@link SlackNotifier#setIcon(String)}
   *   <li>{@link SlackNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link SlackNotifier#setUsername(String)}
   *   <li>{@link SlackNotifier#setWebhookUrl(URI)}
   *   <li>{@link SlackNotifier#getChannel()}
   *   <li>{@link SlackNotifier#getIcon()}
   *   <li>{@link SlackNotifier#getUsername()}
   *   <li>{@link SlackNotifier#getWebhookUrl()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    SlackNotifier slackNotifier = new SlackNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));

    // Act
    slackNotifier.setChannel("Channel");
    slackNotifier.setIcon("Icon");
    slackNotifier.setRestTemplate(mock(RestTemplate.class));
    slackNotifier.setUsername("janedoe");
    URI webhookUrl = PagerdutyNotifier.DEFAULT_URI;
    slackNotifier.setWebhookUrl(webhookUrl);
    String actualChannel = slackNotifier.getChannel();
    String actualIcon = slackNotifier.getIcon();
    String actualUsername = slackNotifier.getUsername();
    URI actualWebhookUrl = slackNotifier.getWebhookUrl();

    // Assert
    assertEquals("Channel", actualChannel);
    assertEquals("Icon", actualIcon);
    assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualWebhookUrl.toString());
    assertEquals("janedoe", actualUsername);
    assertSame(webhookUrl, actualWebhookUrl);
  }

  /**
   * Test {@link SlackNotifier#getMessage()}.
   * <p>
   * Method under test: {@link SlackNotifier#getMessage()}
   */
  @Test
  public void testGetMessage() {
    // Arrange, Act and Assert
    assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
        slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage() {
    // Arrange and Act
    slackNotifier.setMessage("Not all who wander are lostattachmentschannel");

    // Assert
    assertEquals("Not all who wander are lostattachmentschannel", slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage2() {
    // Arrange and Act
    slackNotifier.setMessage("Not all who wander are lostjava.lang.VoidMessage");

    // Assert
    assertEquals("Not all who wander are lostjava.lang.VoidMessage", slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <ul>
   *   <li>Then {@link SlackNotifier} Message is {@code MessageNot all who wander are lost42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage_thenSlackNotifierMessageIsMessageNotAllWhoWanderAreLost42() {
    // Arrange and Act
    slackNotifier.setMessage("MessageNot all who wander are lost42");

    // Assert
    assertEquals("MessageNot all who wander are lost42", slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <ul>
   *   <li>Then {@link SlackNotifier} Message is {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage_thenSlackNotifierMessageIsNotAllWhoWanderAreLost() {
    // Arrange and Act
    slackNotifier.setMessage("Not all who wander are lost");

    // Assert
    assertEquals("Not all who wander are lost", slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <ul>
   *   <li>When a string.</li>
   *   <li>Then {@link SlackNotifier} Message is a string.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage_whenAString_thenSlackNotifierMessageIsAString() {
    // Arrange and Act
    slackNotifier.setMessage(
        "Not all who wander are lostde.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEventde"
            + ".codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent");

    // Assert
    assertEquals(
        "Not all who wander are lostde.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEventde"
            + ".codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent",
        slackNotifier.getMessage());
  }

  /**
   * Test {@link SlackNotifier#setMessage(String)}.
   * <ul>
   *   <li>When a string.</li>
   *   <li>Then {@link SlackNotifier} Message is a string.</li>
   * </ul>
   * <p>
   * Method under test: {@link SlackNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage_whenAString_thenSlackNotifierMessageIsAString2() {
    // Arrange and Act
    slackNotifier
        .setMessage("MessageNot all who wander are lostde.codecentric.boot.admin.server.domain.events.InstanceStatusCh"
            + "angedEvent");

    // Assert
    assertEquals("MessageNot all who wander are lostde.codecentric.boot.admin.server.domain.events.InstanceStatusCh"
        + "angedEvent", slackNotifier.getMessage());
  }
}
