package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {RocketChatNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class RocketChatNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private RestTemplate restTemplate;

  @Autowired
  private RocketChatNotifier rocketChatNotifier;

  /**
   * Test {@link RocketChatNotifier#RocketChatNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#RocketChatNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewRocketChatNotifier() throws EvaluationException {
    // Arrange and Act
    RocketChatNotifier actualRocketChatNotifier = new RocketChatNotifier(instanceRepository, mock(RestTemplate.class));

    // Assert
    Expression message = actualRocketChatNotifier.getMessage();
    assertTrue(message instanceof CompositeStringExpression);
    assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
        message.getExpressionString());
    assertNull(actualRocketChatNotifier.getRoomId());
    assertNull(actualRocketChatNotifier.getToken());
    assertNull(actualRocketChatNotifier.getUrl());
    assertNull(actualRocketChatNotifier.getUserId());
    assertEquals(7, ((CompositeStringExpression) message).getExpressions().length);
    assertTrue(actualRocketChatNotifier.isEnabled());
    Class<String> expectedValueType = String.class;
    assertEquals(expectedValueType, message.getValueType());
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualRocketChatNotifier.getIgnoreChanges());
  }

  /**
   * Test {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_whenInstanceIdWithValueIs42() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(rocketChatNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 42Value}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_whenInstanceIdWithValueIs42Value() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(
        rocketChatNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42Value"), Long.MAX_VALUE), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 424242}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_whenInstanceIdWithValueIs424242() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(rocketChatNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("424242"), 0L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage() {
    // Arrange
    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(new ArrayList<>());

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage2() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 0L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(events);
    eventStore.append(new ArrayList<>());

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage3() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage4() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), Long.MAX_VALUE));

    ArrayList<InstanceEvent> events2 = new ArrayList<>();
    events2.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 0L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(events2);
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage5() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L));

    ArrayList<InstanceEvent> events2 = new ArrayList<>();
    events2.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(events2);
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage6() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    InstanceId instance = InstanceId.of("42");
    events.add(new InstanceDeregisteredEvent(instance, 2L,
        LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage7() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), Long.MAX_VALUE));

    ArrayList<InstanceEvent> events2 = new ArrayList<>();
    events2.add(new InstanceDeregisteredEvent(InstanceId.of("42"), -1L));
    events2.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 0L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(events2);
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage8() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    InstanceId instance = InstanceId.of("42");
    events.add(new InstanceDeregisteredEvent(instance, -1L,
        LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore#InMemoryEventStore()} append {@link ArrayList#ArrayList()}.</li>
   *   <li>Then return {@link Map}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage_givenInMemoryEventStoreAppendArrayList_thenReturnMap() {
    // Arrange
    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(new ArrayList<>());

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@link Map}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage_thenReturnMap() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceId} with {@code Value}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage_whenInstanceIdWithValue() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L));

    InMemoryEventStore eventStore = new InMemoryEventStore();
    eventStore.append(new ArrayList<>());
    eventStore.append(events);

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(new EventsourcingInstanceRepository(eventStore),
        mock(RestTemplate.class));
    rocketChatNotifier.setEnabled(true);
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = rocketChatNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("Value"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(1, ((Map<String, HashMap>) actualCreateMessageResult).size());
    HashMap getResult = ((Map<String, HashMap>) actualCreateMessageResult).get("message");
    assertEquals(2, getResult.size());
    assertEquals("Not all who wander are lost", getResult.get("msg"));
    assertNull(getResult.get("rid"));
  }

  /**
   * Test {@link RocketChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link IMap} {@link IMap#addEntryListener(MapListener, boolean)} return randomUUID.</li>
   *   <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_givenIMapAddEntryListenerReturnRandomUUID_thenCallsAddEntryListener() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new HazelcastEventStore(eventLogs)), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act
    String actualText = rocketChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("Value"), 2L), null);

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
    assertEquals("Not all who wander are lost", actualText);
  }

  /**
   * Test {@link RocketChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore#InMemoryEventStore(int)} with maxLogSizePerAggregate is three.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_givenInMemoryEventStoreWithMaxLogSizePerAggregateIsThree() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore(3)), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        rocketChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L), null));
  }

  /**
   * Test {@link RocketChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long)} with instance is {@link InstanceId} and version is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_whenInstanceDeregisteredEventWithInstanceIsInstanceIdAndVersionIsOne() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        rocketChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test {@link RocketChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceDeregisteredEvent#InstanceDeregisteredEvent(InstanceId, long)} with instance is {@link InstanceId} and version is two.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_whenInstanceDeregisteredEventWithInstanceIsInstanceIdAndVersionIsTwo() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        rocketChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 2L), null));
  }

  /**
   * Test {@link RocketChatNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 42UNKNOWN}.</li>
   *   <li>Then return {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link RocketChatNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_whenInstanceIdWithValueIs42unknown_thenReturnNotAllWhoWanderAreLost() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        rocketChatNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42UNKNOWN"), 2L), null));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link RocketChatNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link RocketChatNotifier#setRoomId(String)}
   *   <li>{@link RocketChatNotifier#setToken(String)}
   *   <li>{@link RocketChatNotifier#setUrl(String)}
   *   <li>{@link RocketChatNotifier#setUserId(String)}
   *   <li>{@link RocketChatNotifier#getMessage()}
   *   <li>{@link RocketChatNotifier#getRoomId()}
   *   <li>{@link RocketChatNotifier#getToken()}
   *   <li>{@link RocketChatNotifier#getUrl()}
   *   <li>{@link RocketChatNotifier#getUserId()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    RocketChatNotifier rocketChatNotifier = new RocketChatNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

    // Act
    rocketChatNotifier.setRestTemplate(mock(RestTemplate.class));
    rocketChatNotifier.setRoomId("42");
    rocketChatNotifier.setToken("ABC123");
    rocketChatNotifier.setUrl("https://example.org/example");
    rocketChatNotifier.setUserId("42");
    Expression actualMessage = rocketChatNotifier.getMessage();
    String actualRoomId = rocketChatNotifier.getRoomId();
    String actualToken = rocketChatNotifier.getToken();
    String actualUrl = rocketChatNotifier.getUrl();

    // Assert
    assertTrue(actualMessage instanceof CompositeStringExpression);
    assertEquals("42", actualRoomId);
    assertEquals("42", rocketChatNotifier.getUserId());
    assertEquals("ABC123", actualToken);
    assertEquals("https://example.org/example", actualUrl);
  }

  /**
   * Test {@link RocketChatNotifier#setMessage(String)}.
   * <p>
   * Method under test: {@link RocketChatNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage() throws EvaluationException {
    // Arrange and Act
    rocketChatNotifier.setMessage("Not all who wander are lost");

    // Assert
    Expression message = rocketChatNotifier.getMessage();
    assertTrue(message instanceof LiteralExpression);
    assertEquals("Not all who wander are lost", message.getExpressionString());
    assertEquals("Not all who wander are lost", message.getValue());
    TypeDescriptor valueTypeDescriptor = message.getValueTypeDescriptor();
    assertEquals("java.lang.String", valueTypeDescriptor.getName());
    assertNull(valueTypeDescriptor.getElementTypeDescriptor());
    assertEquals(0, valueTypeDescriptor.getAnnotations().length);
    assertFalse(valueTypeDescriptor.isArray());
    assertFalse(valueTypeDescriptor.isCollection());
    assertFalse(valueTypeDescriptor.isMap());
    assertFalse(valueTypeDescriptor.isPrimitive());
    Class<String> expectedValueType = String.class;
    Class<?> valueType = message.getValueType();
    assertEquals(expectedValueType, valueType);
    assertSame(valueType, valueTypeDescriptor.getObjectType());
    assertSame(valueType, valueTypeDescriptor.getSource());
    assertSame(valueType, valueTypeDescriptor.getType());
  }
}
