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
import java.net.URI;
import java.util.Map;
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

@ContextConfiguration(classes = {LetsChatNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class LetsChatNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @Autowired
  private LetsChatNotifier letsChatNotifier;

  @MockitoBean
  private RestTemplate restTemplate;

  /**
   * Test {@link LetsChatNotifier#LetsChatNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link LetsChatNotifier#LetsChatNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewLetsChatNotifier() {
    // Arrange and Act
    LetsChatNotifier actualLetsChatNotifier = new LetsChatNotifier(instanceRepository, mock(RestTemplate.class));

    // Assert
    assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
        actualLetsChatNotifier.getMessage());
    assertEquals("Spring Boot Admin", actualLetsChatNotifier.getUsername());
    assertNull(actualLetsChatNotifier.getRoom());
    assertNull(actualLetsChatNotifier.getToken());
    assertNull(actualLetsChatNotifier.getUrl());
    assertTrue(actualLetsChatNotifier.isEnabled());
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualLetsChatNotifier.getIgnoreChanges());
  }

  /**
   * Test {@link LetsChatNotifier#doNotify(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link LetsChatNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(letsChatNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LetsChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@link Map}.</li>
   * </ul>
   * <p>
   * Method under test: {@link LetsChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage_thenReturnMap() {
    // Arrange
    LetsChatNotifier letsChatNotifier = new LetsChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    letsChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = letsChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, String>) actualCreateMessageResult).size());
    assertEquals("Not all who wander are lost", ((Map<String, String>) actualCreateMessageResult).get("text"));
  }

  /**
   * Test {@link LetsChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link LetsChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_thenReturnNotAllWhoWanderAreLost() {
    // Arrange
    LetsChatNotifier letsChatNotifier = new LetsChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    letsChatNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        letsChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link LetsChatNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link LetsChatNotifier#setRoom(String)}
   *   <li>{@link LetsChatNotifier#setToken(String)}
   *   <li>{@link LetsChatNotifier#setUrl(URI)}
   *   <li>{@link LetsChatNotifier#setUsername(String)}
   *   <li>{@link LetsChatNotifier#getRoom()}
   *   <li>{@link LetsChatNotifier#getToken()}
   *   <li>{@link LetsChatNotifier#getUrl()}
   *   <li>{@link LetsChatNotifier#getUsername()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    LetsChatNotifier letsChatNotifier = new LetsChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

    // Act
    letsChatNotifier.setRestTemplate(mock(RestTemplate.class));
    letsChatNotifier.setRoom("Room");
    letsChatNotifier.setToken("ABC123");
    URI url = PagerdutyNotifier.DEFAULT_URI;
    letsChatNotifier.setUrl(url);
    letsChatNotifier.setUsername("janedoe");
    String actualRoom = letsChatNotifier.getRoom();
    String actualToken = letsChatNotifier.getToken();
    URI actualUrl = letsChatNotifier.getUrl();
    String actualUsername = letsChatNotifier.getUsername();

    // Assert
    assertEquals("ABC123", actualToken);
    assertEquals("Room", actualRoom);
    assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualUrl.toString());
    assertEquals("janedoe", actualUsername);
    assertSame(url, actualUrl);
  }

  /**
   * Test {@link LetsChatNotifier#getMessage()}.
   * <p>
   * Method under test: {@link LetsChatNotifier#getMessage()}
   */
  @Test
  public void testGetMessage() {
    // Arrange, Act and Assert
    assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
        letsChatNotifier.getMessage());
  }

  /**
   * Test {@link LetsChatNotifier#setMessage(String)}.
   * <p>
   * Method under test: {@link LetsChatNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage() {
    // Arrange and Act
    letsChatNotifier.setMessage("Not all who wander are lost");

    // Assert
    assertEquals("Not all who wander are lost", letsChatNotifier.getMessage());
  }
}
