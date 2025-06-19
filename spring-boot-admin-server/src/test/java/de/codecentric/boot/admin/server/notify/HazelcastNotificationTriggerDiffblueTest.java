package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.server.reactive.ChannelSendOperator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(MockitoJUnitRunner.class)
public class HazelcastNotificationTriggerDiffblueTest {
  @Mock
  private ConcurrentMap<InstanceId, Long> concurrentMap;

  @InjectMocks
  private HazelcastNotificationTrigger hazelcastNotificationTrigger;

  @Mock
  private Notifier notifier;

  /**
   * Test {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}.
   * <p>
   * Method under test: {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}
   */
  @Test
  public void testSendNotifications() {
    // Arrange
    Mono<Void> mono = mock(Mono.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    when(mono.doOnError(Mockito.<Consumer<Throwable>>any()))
        .thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
    when(notifier.notify(Mockito.<InstanceEvent>any())).thenReturn(mono);
    when(concurrentMap.replace(Mockito.<InstanceId>any(), Mockito.<Long>any(), Mockito.<Long>any())).thenReturn(true);
    when(concurrentMap.getOrDefault(Mockito.<Object>any(), Mockito.<Long>any())).thenReturn(0L);

    // Act
    hazelcastNotificationTrigger.sendNotifications(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Assert
    verify(notifier).notify(isA(InstanceEvent.class));
    verify(concurrentMap).getOrDefault(isA(Object.class), eq(-1L));
    verify(concurrentMap).replace(isA(InstanceId.class), eq(0L), eq(1L));
    verify(mono).doOnError(isA(Consumer.class));
  }

  /**
   * Test {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}.
   * <p>
   * Method under test: {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}
   */
  @Test
  public void testSendNotifications2() {
    // Arrange
    ChannelSendOperator<Object> channelSendOperator = mock(ChannelSendOperator.class);
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    ChannelSendOperator<Object> channelSendOperator2 = new ChannelSendOperator<>(source, mock(Function.class));

    when(channelSendOperator.onErrorResume(Mockito.<Function<Throwable, Mono<Void>>>any()))
        .thenReturn(channelSendOperator2);
    Mono<Void> mono = mock(Mono.class);
    when(mono.doOnError(Mockito.<Consumer<Throwable>>any())).thenReturn(channelSendOperator);
    when(notifier.notify(Mockito.<InstanceEvent>any())).thenReturn(mono);
    when(concurrentMap.replace(Mockito.<InstanceId>any(), Mockito.<Long>any(), Mockito.<Long>any())).thenReturn(true);
    when(concurrentMap.getOrDefault(Mockito.<Object>any(), Mockito.<Long>any())).thenReturn(0L);

    // Act
    Mono<Void> actualSendNotificationsResult = hazelcastNotificationTrigger
        .sendNotifications(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Assert
    verify(notifier).notify(isA(InstanceEvent.class));
    verify(concurrentMap).getOrDefault(isA(Object.class), eq(-1L));
    verify(concurrentMap).replace(isA(InstanceId.class), eq(0L), eq(1L));
    verify(mono).doOnError(isA(Consumer.class));
    verify(channelSendOperator).onErrorResume(isA(Function.class));
    assertSame(channelSendOperator2, actualSendNotificationsResult);
  }

  /**
   * Test {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}.
   * <ul>
   *   <li>Given {@link ConcurrentMap} {@link ConcurrentMap#getOrDefault(Object, Object)} return one.</li>
   * </ul>
   * <p>
   * Method under test: {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}
   */
  @Test
  public void testSendNotifications_givenConcurrentMapGetOrDefaultReturnOne() throws AssertionError {
    // Arrange
    when(concurrentMap.getOrDefault(Mockito.<Object>any(), Mockito.<Long>any())).thenReturn(1L);

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(hazelcastNotificationTrigger.sendNotifications(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
    createResult.expectComplete().verify();
    verify(concurrentMap).getOrDefault(isA(Object.class), eq(-1L));
  }

  /**
   * Test {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}.
   * <ul>
   *   <li>Then calls {@link ConcurrentMap#putIfAbsent(Object, Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}
   */
  @Test
  public void testSendNotifications_thenCallsPutIfAbsent() {
    // Arrange
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    when(notifier.notify(Mockito.<InstanceEvent>any()))
        .thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
    when(concurrentMap.putIfAbsent(Mockito.<InstanceId>any(), Mockito.<Long>any())).thenReturn(null);
    when(concurrentMap.getOrDefault(Mockito.<Object>any(), Mockito.<Long>any())).thenReturn(-1L);

    // Act
    hazelcastNotificationTrigger.sendNotifications(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Assert
    verify(notifier).notify(isA(InstanceEvent.class));
    verify(concurrentMap).getOrDefault(isA(Object.class), eq(-1L));
    verify(concurrentMap).putIfAbsent(isA(InstanceId.class), eq(1L));
  }

  /**
   * Test {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}.
   * <ul>
   *   <li>Then calls {@link ConcurrentMap#replace(Object, Object, Object)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HazelcastNotificationTrigger#sendNotifications(InstanceEvent)}
   */
  @Test
  public void testSendNotifications_thenCallsReplace() {
    // Arrange
    Flux<?> source = Flux.fromIterable(new ArrayList<>());
    when(notifier.notify(Mockito.<InstanceEvent>any()))
        .thenReturn(new ChannelSendOperator<>(source, mock(Function.class)));
    when(concurrentMap.replace(Mockito.<InstanceId>any(), Mockito.<Long>any(), Mockito.<Long>any())).thenReturn(true);
    when(concurrentMap.getOrDefault(Mockito.<Object>any(), Mockito.<Long>any())).thenReturn(0L);

    // Act
    hazelcastNotificationTrigger.sendNotifications(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Assert
    verify(notifier).notify(isA(InstanceEvent.class));
    verify(concurrentMap).getOrDefault(isA(Object.class), eq(-1L));
    verify(concurrentMap).replace(isA(InstanceId.class), eq(0L), eq(1L));
  }
}
