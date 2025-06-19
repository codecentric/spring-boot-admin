package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.notify.FeiShuNotifier;
import de.codecentric.boot.admin.server.notify.HazelcastNotificationTrigger;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {NotificationTrigger.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class AbstractEventHandlerDiffblueTest {
  @Autowired
  private AbstractEventHandler<InstanceEvent> abstractEventHandler;

  @MockitoBean
  private Notifier notifier;

  @MockitoBean
  private Publisher<InstanceEvent> publisher;

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} addAll {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Function#apply(Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenArrayListAddAllArrayList_thenCallsApply() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.addAll(new ArrayList<>());

    HazelcastEventStore events2 = new HazelcastEventStore(eventLogs);
    events2.append(events);
    Function<InstanceId, Long> function = mock(Function.class);
    when(function.apply(Mockito.<InstanceId>any())).thenReturn(1L);

    ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();
    sentNotifications.computeIfAbsent(InstanceId.of("42"), function);

    // Act
    new HazelcastNotificationTrigger(mock(FeiShuNotifier.class), events2, sentNotifications).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
    verify(function).apply(isA(InstanceId.class));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link ConcurrentHashMap#ConcurrentHashMap()} IfAbsent {@link InstanceId} with value is {@code 42} is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenConcurrentHashMapIfAbsentInstanceIdWithValueIs42IsOne() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.addAll(new ArrayList<>());

    HazelcastEventStore events2 = new HazelcastEventStore(eventLogs);
    events2.append(events);

    ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();
    sentNotifications.putIfAbsent(InstanceId.of("42"), 1L);
    sentNotifications.putAll(new HashMap<>());
    sentNotifications.computeIfAbsent(InstanceId.of("42"), mock(Function.class));

    // Act
    new HazelcastNotificationTrigger(mock(FeiShuNotifier.class), events2, sentNotifications).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link Function} {@link Function#apply(Object)} return {@link Long#MAX_VALUE}.</li>
   *   <li>Then calls {@link Function#apply(Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenFunctionApplyReturnMax_value_thenCallsApply() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    HazelcastEventStore events = new HazelcastEventStore(eventLogs);
    events.append(new ArrayList<>());
    Function<InstanceId, Long> function = mock(Function.class);
    when(function.apply(Mockito.<InstanceId>any())).thenReturn(Long.MAX_VALUE);

    ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();
    sentNotifications.computeIfAbsent(InstanceId.of("42"), function);

    // Act
    new HazelcastNotificationTrigger(mock(FeiShuNotifier.class), events, sentNotifications).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
    verify(function).apply(isA(InstanceId.class));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link Function} {@link Function#apply(Object)} return one.</li>
   *   <li>Then calls {@link Function#apply(Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenFunctionApplyReturnOne_thenCallsApply() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    HazelcastEventStore events = new HazelcastEventStore(eventLogs);
    events.append(new ArrayList<>());
    Function<InstanceId, Long> function = mock(Function.class);
    when(function.apply(Mockito.<InstanceId>any())).thenReturn(1L);

    ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();
    sentNotifications.computeIfAbsent(InstanceId.of("42"), function);

    // Act
    new HazelcastNotificationTrigger(mock(FeiShuNotifier.class), events, sentNotifications).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
    verify(function).apply(isA(InstanceId.class));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore#HazelcastEventStore(IMap)} with eventLogs is {@link IMap} append {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenHazelcastEventStoreWithEventLogsIsIMapAppendArrayList() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

    HazelcastEventStore events = new HazelcastEventStore(eventLogs);
    events.append(new ArrayList<>());
    FeiShuNotifier notifier = mock(FeiShuNotifier.class);

    // Act
    new HazelcastNotificationTrigger(notifier, events, new ConcurrentHashMap<>()).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link HazelcastEventStore#HazelcastEventStore(IMap)} with eventLogs is {@link IMap}.</li>
   *   <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenHazelcastEventStoreWithEventLogsIsIMap_thenCallsAddEntryListener() {
    // Arrange
    IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
    when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());
    HazelcastEventStore events = new HazelcastEventStore(eventLogs);
    FeiShuNotifier notifier = mock(FeiShuNotifier.class);

    // Act
    new HazelcastNotificationTrigger(notifier, events, new ConcurrentHashMap<>()).start();

    // Assert
    verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
  }

  /**
   * Test {@link AbstractEventHandler#start()}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore#InMemoryEventStore()} append {@link ArrayList#ArrayList()}.</li>
   *   <li>Then calls {@link Function#apply(Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link AbstractEventHandler#start()}
   */
  @Test
  public void testStart_givenInMemoryEventStoreAppendArrayList_thenCallsApply() {
    // Arrange
    InMemoryEventStore events = new InMemoryEventStore();
    events.append(new ArrayList<>());
    Function<InstanceId, Long> function = mock(Function.class);
    when(function.apply(Mockito.<InstanceId>any())).thenReturn(Long.MAX_VALUE);

    ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();
    sentNotifications.computeIfAbsent(InstanceId.of("42"), function);

    // Act
    new HazelcastNotificationTrigger(mock(FeiShuNotifier.class), events, sentNotifications).start();

    // Assert
    verify(function).apply(isA(InstanceId.class));
  }

  /**
   * Test {@link AbstractEventHandler#createScheduler()}.
   * <p>
   * Method under test: {@link AbstractEventHandler#createScheduler()}
   */
  @Test
  public void testCreateScheduler() {
    // Arrange, Act and Assert
    assertFalse(abstractEventHandler.createScheduler().isDisposed());
  }
}
