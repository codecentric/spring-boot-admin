package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {CompositeNotifier.class})
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class CompositeNotifierDiffblueTest {
  @Autowired
  private CompositeNotifier compositeNotifier;

  @MockitoBean
  private Iterable<Notifier> iterable;

  /**
   * Test {@link CompositeNotifier#CompositeNotifier(Iterable)}.
   * <ul>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CompositeNotifier#CompositeNotifier(Iterable)}
   */
  @Test
  public void testNewCompositeNotifier_whenArrayList() throws AssertionError {
    // Arrange and Act
    CompositeNotifier actualCompositeNotifier = new CompositeNotifier(new ArrayList<>());

    // Assert
    FirstStep<Void> createResult = StepVerifier
        .create(actualCompositeNotifier.notify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link CompositeNotifier#notify(InstanceEvent)} with {@code InstanceEvent}.
   * <p>
   * Method under test: {@link CompositeNotifier#notify(InstanceEvent)}
   */
  @Test
  public void testNotifyWithInstanceEvent() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(compositeNotifier.notify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)));
    createResult.expectError().verify();
  }
}
