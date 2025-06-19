package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
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
import java.net.URI;
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
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {WebexNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class WebexNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private RestTemplate restTemplate;

  @Autowired
  private WebexNotifier webexNotifier;

  /**
   * Test {@link WebexNotifier#WebexNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link WebexNotifier#WebexNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewWebexNotifier() {
    // Arrange, Act and Assert
    Expression message = new WebexNotifier(instanceRepository, mock(RestTemplate.class)).getMessage();
    assertTrue(message instanceof CompositeStringExpression);
    Expression[] expressions = ((CompositeStringExpression) message).getExpressions();
    assertTrue(expressions[0] instanceof LiteralExpression);
    assertTrue(expressions[2] instanceof LiteralExpression);
    assertTrue(expressions[4] instanceof LiteralExpression);
    assertTrue(expressions[6] instanceof LiteralExpression);
    assertTrue(expressions[3] instanceof SpelExpression);
    assertTrue(expressions[5] instanceof SpelExpression);
    assertEquals(7, expressions.length);
  }

  /**
   * Test {@link WebexNotifier#WebexNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link WebexNotifier#WebexNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewWebexNotifier2() {
    // Arrange, Act and Assert
    Expression message = new WebexNotifier(instanceRepository, restTemplate).getMessage();
    assertTrue(message instanceof CompositeStringExpression);
    Expression[] expressions = ((CompositeStringExpression) message).getExpressions();
    assertTrue(expressions[0] instanceof LiteralExpression);
    assertTrue(expressions[2] instanceof LiteralExpression);
    assertTrue(expressions[4] instanceof LiteralExpression);
    assertTrue(expressions[6] instanceof LiteralExpression);
    assertTrue(expressions[3] instanceof SpelExpression);
    assertTrue(expressions[5] instanceof SpelExpression);
    assertEquals(7, expressions.length);
  }

  /**
   * Test {@link WebexNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link WebexNotifier} AuthToken is {@code 42}.</li>
   *   <li>When {@link InstanceId} with value is {@code Event: {}}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenWebexNotifierAuthTokenIs42_whenInstanceIdWithValueIsEvent() throws AssertionError {
    // Arrange
    webexNotifier.setAuthToken("42");

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(webexNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("Event: {}"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link WebexNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link WebexNotifier} AuthToken is {@code foo}.</li>
   *   <li>When {@link InstanceId} with value is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenWebexNotifierAuthTokenIsFoo_whenInstanceIdWithValueIs42() throws AssertionError {
    // Arrange
    webexNotifier.setAuthToken("foo");

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(webexNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link WebexNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link WebexNotifier} AuthToken is {@code Void}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenWebexNotifierAuthTokenIsJavaLangVoid() throws AssertionError {
    // Arrange
    webexNotifier.setAuthToken("java.lang.Void");

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(webexNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("Event: {}"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link WebexNotifier#doNotify(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link WebexNotifier} AuthToken is {@code null}.</li>
   *   <li>When {@link InstanceId} with value is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify_givenWebexNotifierAuthTokenIsNull_whenInstanceIdWithValueIs42() throws AssertionError {
    // Arrange
    webexNotifier.setAuthToken(null);

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(webexNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link WebexNotifier#createMessage(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@link Map}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#createMessage(InstanceEvent, Instance)}
   */
  @Test
  public void testCreateMessage_thenReturnMap() {
    // Arrange
    WebexNotifier webexNotifier = new WebexNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));
    webexNotifier.setMessage("Not all who wander are lost");

    // Act
    Object actualCreateMessageResult = webexNotifier
        .createMessage(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

    // Assert
    assertTrue(actualCreateMessageResult instanceof Map);
    assertEquals(2, ((Map<String, String>) actualCreateMessageResult).size());
    assertEquals("Not all who wander are lost", ((Map<String, String>) actualCreateMessageResult).get("markdown"));
    assertNull(((Map<String, String>) actualCreateMessageResult).get("roomId"));
  }

  /**
   * Test {@link WebexNotifier#getText(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link WebexNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText() {
    // Arrange
    WebexNotifier webexNotifier = new WebexNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));
    webexNotifier.setMessage("Not all who wander are lost");
    InstanceEvent event = mock(InstanceEvent.class);
    when(event.getInstance()).thenThrow(new IllegalStateException("foo"));

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> webexNotifier.getText(event, null));
    verify(event).getInstance();
  }

  /**
   * Test {@link WebexNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link IMap} {@link IMap#addEntryListener(MapListener, boolean)} return randomUUID.</li>
   *   <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_givenIMapAddEntryListenerReturnRandomUUID_thenCallsAddEntryListener() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
    when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    WebexNotifier webexNotifier = new WebexNotifier(
        new EventsourcingInstanceRepository(new HazelcastEventStore(3, eventLog)), mock(RestTemplate.class));
    webexNotifier.setMessage("42");
    InstanceEvent event = mock(InstanceEvent.class);
    when(event.getInstance()).thenThrow(new IllegalStateException("foo"));

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> webexNotifier.getText(event, null));
    verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
    verify(event).getInstance();
  }

  /**
   * Test {@link WebexNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Given {@link IllegalStateException#IllegalStateException(String)} with {@code event}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_givenIllegalStateExceptionWithEvent() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
    when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    WebexNotifier webexNotifier = new WebexNotifier(
        new EventsourcingInstanceRepository(new HazelcastEventStore(3, eventLog)), mock(RestTemplate.class));
    webexNotifier.setMessage("42");
    InstanceEvent event = mock(InstanceEvent.class);
    when(event.getInstance()).thenThrow(new IllegalStateException("event"));

    // Act and Assert
    assertThrows(IllegalStateException.class, () -> webexNotifier.getText(event, null));
    verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
    verify(event).getInstance();
  }

  /**
   * Test {@link WebexNotifier#getText(InstanceEvent, Instance)}.
   * <ul>
   *   <li>Then return {@code Not all who wander are lost}.</li>
   * </ul>
   * <p>
   * Method under test: {@link WebexNotifier#getText(InstanceEvent, Instance)}
   */
  @Test
  public void testGetText_thenReturnNotAllWhoWanderAreLost() {
    // Arrange
    WebexNotifier webexNotifier = new WebexNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));
    webexNotifier.setMessage("Not all who wander are lost");

    // Act and Assert
    assertEquals("Not all who wander are lost",
        webexNotifier.getText(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link WebexNotifier#setAuthToken(String)}
   *   <li>{@link WebexNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link WebexNotifier#setRoomId(String)}
   *   <li>{@link WebexNotifier#setUrl(URI)}
   *   <li>{@link WebexNotifier#getAuthToken()}
   *   <li>{@link WebexNotifier#getMessage()}
   *   <li>{@link WebexNotifier#getRoomId()}
   *   <li>{@link WebexNotifier#getUrl()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    WebexNotifier webexNotifier = new WebexNotifier(new EventsourcingInstanceRepository(new InMemoryEventStore()),
        mock(RestTemplate.class));

    // Act
    webexNotifier.setAuthToken("ABC123");
    webexNotifier.setRestTemplate(mock(RestTemplate.class));
    webexNotifier.setRoomId("42");
    URI url = PagerdutyNotifier.DEFAULT_URI;
    webexNotifier.setUrl(url);
    String actualAuthToken = webexNotifier.getAuthToken();
    Expression actualMessage = webexNotifier.getMessage();
    String actualRoomId = webexNotifier.getRoomId();
    URI actualUrl = webexNotifier.getUrl();

    // Assert
    assertTrue(actualMessage instanceof CompositeStringExpression);
    assertEquals("42", actualRoomId);
    assertEquals("ABC123", actualAuthToken);
    assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualUrl.toString());
    assertSame(url, actualUrl);
  }

  /**
   * Test {@link WebexNotifier#setMessage(String)}.
   * <p>
   * Method under test: {@link WebexNotifier#setMessage(String)}
   */
  @Test
  public void testSetMessage() throws EvaluationException {
    // Arrange and Act
    webexNotifier.setMessage("Not all who wander are lost");

    // Assert
    Expression message = webexNotifier.getMessage();
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
