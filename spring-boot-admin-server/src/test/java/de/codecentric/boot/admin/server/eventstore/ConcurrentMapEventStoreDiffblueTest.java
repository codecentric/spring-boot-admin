package de.codecentric.boot.admin.server.eventstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {InMemoryEventStore.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class ConcurrentMapEventStoreDiffblueTest {
  @Autowired
  private ConcurrentMapEventStore concurrentMapEventStore;

  @MockitoBean
  private HazelcastEventStore hazelcastEventStore;

  /**
   * Test {@link ConcurrentMapEventStore#append(List)}.
   * <ul>
   *   <li>Given {@link InMemoryEventStore#InMemoryEventStore()}.</li>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#append(List)}
   */
  @Test
  public void testAppend_givenInMemoryEventStore_whenArrayList() throws AssertionError {
    // Arrange
    InMemoryEventStore inMemoryEventStore = new InMemoryEventStore();

    // Act and Assert
    FirstStep<Void> createResult = StepVerifier.create(inMemoryEventStore.append(new ArrayList<>()));
    createResult.expectComplete().verify();
    FirstStep<InstanceEvent> createResult2 = StepVerifier.create(inMemoryEventStore.findAll());
    createResult2.expectComplete().verify();
  }

  /**
   * Test {@link ConcurrentMapEventStore#append(List)}.
   * <ul>
   *   <li>Then throw {@link IllegalArgumentException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#append(List)}
   */
  @Test
  public void testAppend_thenThrowIllegalArgumentException() {
    // Arrange
    when(concurrentMapEventStore.append(Mockito.<List<InstanceEvent>>any()))
        .thenThrow(new IllegalArgumentException("foo"));

    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> concurrentMapEventStore.append(events));
    verify(concurrentMapEventStore).append(isA(List.class));
  }

  /**
   * Test {@link ConcurrentMapEventStore#append(List)}.
   * <ul>
   *   <li>Then throw {@link IllegalArgumentException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#append(List)}
   */
  @Test
  public void testAppend_thenThrowIllegalArgumentException2() {
    // Arrange
    when(concurrentMapEventStore.append(Mockito.<List<InstanceEvent>>any()))
        .thenThrow(new IllegalArgumentException("foo"));

    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> concurrentMapEventStore.append(events));
    verify(concurrentMapEventStore).append(isA(List.class));
  }

  /**
   * Test {@link ConcurrentMapEventStore#doAppend(List)}.
   * <ul>
   *   <li>Given {@link ConcurrentMapEventStore}.</li>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   *   <li>Then return {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#doAppend(List)}
   */
  @Test
  public void testDoAppend_givenConcurrentMapEventStore_whenArrayList_thenReturnTrue() {
    // Arrange, Act and Assert
    assertTrue(concurrentMapEventStore.doAppend(new ArrayList<>()));
  }

  /**
   * Test {@link ConcurrentMapEventStore#doAppend(List)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then throw {@link OptimisticLockingException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#doAppend(List)}
   */
  @Test
  public void testDoAppend_givenInstanceIdWithValueIs42_thenThrowOptimisticLockingException() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertThrows(OptimisticLockingException.class, () -> concurrentMapEventStore.doAppend(events));
  }

  /**
   * Test {@link ConcurrentMapEventStore#doAppend(List)}.
   * <ul>
   *   <li>Given {@link InstanceId} with {@code Value}.</li>
   *   <li>Then throw {@link IllegalArgumentException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#doAppend(List)}
   */
  @Test
  public void testDoAppend_givenInstanceIdWithValue_thenThrowIllegalArgumentException() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("Value"), 1L));
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> concurrentMapEventStore.doAppend(events));
  }

  /**
   * Test {@link ConcurrentMapEventStore#getLastVersion(List)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then return one.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#getLastVersion(List)}
   */
  @Test
  public void testGetLastVersion_givenInstanceIdWithValueIs42_thenReturnOne() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertEquals(1L, ConcurrentMapEventStore.getLastVersion(events));
  }

  /**
   * Test {@link ConcurrentMapEventStore#getLastVersion(List)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then return one.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#getLastVersion(List)}
   */
  @Test
  public void testGetLastVersion_givenInstanceIdWithValueIs42_thenReturnOne2() {
    // Arrange
    ArrayList<InstanceEvent> events = new ArrayList<>();
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
    events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

    // Act and Assert
    assertEquals(1L, ConcurrentMapEventStore.getLastVersion(events));
  }

  /**
   * Test {@link ConcurrentMapEventStore#getLastVersion(List)}.
   * <ul>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   *   <li>Then return minus one.</li>
   * </ul>
   * <p>
   * Method under test: {@link ConcurrentMapEventStore#getLastVersion(List)}
   */
  @Test
  public void testGetLastVersion_whenArrayList_thenReturnMinusOne() {
    // Arrange, Act and Assert
    assertEquals(-1L, ConcurrentMapEventStore.getLastVersion(new ArrayList<>()));
  }
}
