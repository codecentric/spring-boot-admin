package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
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

@ContextConfiguration(classes = {TelegramNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class TelegramNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private RestTemplate restTemplate;

  @Autowired
  private TelegramNotifier telegramNotifier;

  /**
   * Test {@link TelegramNotifier#TelegramNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link TelegramNotifier#TelegramNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewTelegramNotifier() {
    // Arrange and Act
    TelegramNotifier actualTelegramNotifier = new TelegramNotifier(instanceRepository, mock(RestTemplate.class));

    // Assert
    assertEquals("HTML", actualTelegramNotifier.getParseMode());
    assertEquals("https://api.telegram.org", actualTelegramNotifier.getApiUrl());
    assertNull(actualTelegramNotifier.getAuthToken());
    assertNull(actualTelegramNotifier.getChatId());
    assertFalse(actualTelegramNotifier.isDisableNotify());
    assertTrue(actualTelegramNotifier.isEnabled());
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualTelegramNotifier.getIgnoreChanges());
  }

  /**
   * Test {@link TelegramNotifier#doNotify(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link TelegramNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(telegramNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link TelegramNotifier#buildUrl()}.
   * <p>
   * Method under test: {@link TelegramNotifier#buildUrl()}
   */
  @Test
  public void testBuildUrl() {
    // Arrange, Act and Assert
    assertEquals("https://api.telegram.org/botnull/sendmessage?chat_id={chat_id}&text={text}&parse_mode={parse_mode}"
        + "&disable_notification={disable_notification}", telegramNotifier.buildUrl());
  }

  /**
   * Test {@link TelegramNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link TelegramNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_thenReturnNotAllWhoWanderAreLost() {
    // Arrange
    TelegramNotifier telegramNotifier = new TelegramNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    telegramNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        telegramNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link TelegramNotifier#setApiUrl(String)}
   *   <li>{@link TelegramNotifier#setAuthToken(String)}
   *   <li>{@link TelegramNotifier#setChatId(String)}
   *   <li>{@link TelegramNotifier#setDisableNotify(boolean)}
   *   <li>{@link TelegramNotifier#setParseMode(String)}
   *   <li>{@link TelegramNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link TelegramNotifier#getApiUrl()}
   *   <li>{@link TelegramNotifier#getAuthToken()}
   *   <li>{@link TelegramNotifier#getChatId()}
   *   <li>{@link TelegramNotifier#getParseMode()}
   *   <li>{@link TelegramNotifier#isDisableNotify()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    TelegramNotifier telegramNotifier = new TelegramNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

    // Act
    telegramNotifier.setApiUrl("https://example.org/example");
    telegramNotifier.setAuthToken("ABC123");
    telegramNotifier.setChatId("42");
    telegramNotifier.setDisableNotify(true);
    telegramNotifier.setParseMode("Parse Mode");
    telegramNotifier.setRestTemplate(mock(RestTemplate.class));
    String actualApiUrl = telegramNotifier.getApiUrl();
    String actualAuthToken = telegramNotifier.getAuthToken();
    String actualChatId = telegramNotifier.getChatId();
    String actualParseMode = telegramNotifier.getParseMode();

    // Assert
    assertEquals("42", actualChatId);
    assertEquals("ABC123", actualAuthToken);
    assertEquals("Parse Mode", actualParseMode);
    assertEquals("https://example.org/example", actualApiUrl);
    assertTrue(telegramNotifier.isDisableNotify());
  }
}
