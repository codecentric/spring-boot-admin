package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

@ContextConfiguration(classes = {PagerdutyNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class PagerdutyNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @Autowired
  private PagerdutyNotifier pagerdutyNotifier;

  @MockitoBean
  private RestTemplate restTemplate;

  /**
   * Test {@link PagerdutyNotifier#PagerdutyNotifier(InstanceRepository, RestTemplate)}.
   * <p>
   * Method under test: {@link PagerdutyNotifier#PagerdutyNotifier(InstanceRepository, RestTemplate)}
   */
  @Test
  public void testNewPagerdutyNotifier() {
    // Arrange and Act
    PagerdutyNotifier actualPagerdutyNotifier = new PagerdutyNotifier(instanceRepository, mock(RestTemplate.class));

    // Assert
    assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
        actualPagerdutyNotifier.getDescription());
    URI url = actualPagerdutyNotifier.getUrl();
    assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", url.toString());
    assertNull(actualPagerdutyNotifier.getClient());
    assertNull(actualPagerdutyNotifier.getServiceKey());
    assertNull(actualPagerdutyNotifier.getClientUrl());
    assertTrue(actualPagerdutyNotifier.isEnabled());
    assertSame(actualPagerdutyNotifier.DEFAULT_URI, url);
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualPagerdutyNotifier.getIgnoreChanges());
  }

  /**
   * Test {@link PagerdutyNotifier#doNotify(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link PagerdutyNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(pagerdutyNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link PagerdutyNotifier#getDescription()}.
   * <p>
   * Method under test: {@link PagerdutyNotifier#getDescription()}
   */
  @Test
  public void testGetDescription() {
    // Arrange, Act and Assert
    assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
        pagerdutyNotifier.getDescription());
  }

  /**
   * Test {@link PagerdutyNotifier#getDescription(InstanceEvent, Instance)} with {@code InstanceEvent}, {@code Instance}.
   * <p>
   * Method under test: {@link PagerdutyNotifier#getDescription(InstanceEvent, Instance)}
   */
  @Test
  public void testGetDescriptionWithInstanceEventInstance() {
    // Arrange
    PagerdutyNotifier pagerdutyNotifier = new PagerdutyNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));
    pagerdutyNotifier.setDescription("The characteristics of someone or something");

    // Act and Assert
    assertEquals("The characteristics of someone or something",
        pagerdutyNotifier.getDescription(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
  }

  /**
   * Test {@link PagerdutyNotifier#setDescription(String)}.
   * <p>
   * Method under test: {@link PagerdutyNotifier#setDescription(String)}
   */
  @Test
  public void testSetDescription() {
    // Arrange and Act
    pagerdutyNotifier.setDescription("The characteristics of someone or something");

    // Assert
    assertEquals("The characteristics of someone or something", pagerdutyNotifier.getDescription());
  }

  /**
   * Test {@link PagerdutyNotifier#getDetails(InstanceEvent)}.
   * <ul>
   *   <li>Given {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then return size is two.</li>
   * </ul>
   * <p>
   * Method under test: {@link PagerdutyNotifier#getDetails(InstanceEvent)}
   */
  @Test
  public void testGetDetails_givenInstanceIdWithValueIs42_thenReturnSizeIsTwo() {
    // Arrange
    InstanceStatusChangedEvent event = mock(InstanceStatusChangedEvent.class);
    when(event.getInstance()).thenReturn(InstanceId.of("42"));
    when(event.getStatusInfo()).thenReturn(null);

    // Act
    Map<String, Object> actualDetails = pagerdutyNotifier.getDetails(event);

    // Assert
    verify(event).getInstance();
    verify(event).getStatusInfo();
    assertEquals(2, actualDetails.size());
    assertEquals("UNKNOWN", actualDetails.get("from"));
    assertNull(actualDetails.get("to"));
  }

  /**
   * Test {@link PagerdutyNotifier#getDetails(InstanceEvent)}.
   * <ul>
   *   <li>When {@link InstanceId} with value is {@code 42}.</li>
   *   <li>Then return Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link PagerdutyNotifier#getDetails(InstanceEvent)}
   */
  @Test
  public void testGetDetails_whenInstanceIdWithValueIs42_thenReturnEmpty() {
    // Arrange, Act and Assert
    assertTrue(pagerdutyNotifier.getDetails(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L)).isEmpty());
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link PagerdutyNotifier#setClient(String)}
   *   <li>{@link PagerdutyNotifier#setClientUrl(URI)}
   *   <li>{@link PagerdutyNotifier#setRestTemplate(RestTemplate)}
   *   <li>{@link PagerdutyNotifier#setServiceKey(String)}
   *   <li>{@link PagerdutyNotifier#setUrl(URI)}
   *   <li>{@link PagerdutyNotifier#getClient()}
   *   <li>{@link PagerdutyNotifier#getClientUrl()}
   *   <li>{@link PagerdutyNotifier#getServiceKey()}
   *   <li>{@link PagerdutyNotifier#getUrl()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    PagerdutyNotifier pagerdutyNotifier = new PagerdutyNotifier(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

    // Act
    pagerdutyNotifier.setClient("Client");
    pagerdutyNotifier.setClientUrl(PagerdutyNotifier.DEFAULT_URI);
    pagerdutyNotifier.setRestTemplate(mock(RestTemplate.class));
    pagerdutyNotifier.setServiceKey("Service Key");
    URI url = PagerdutyNotifier.DEFAULT_URI;
    pagerdutyNotifier.setUrl(url);
    String actualClient = pagerdutyNotifier.getClient();
    URI actualClientUrl = pagerdutyNotifier.getClientUrl();
    String actualServiceKey = pagerdutyNotifier.getServiceKey();
    URI actualUrl = pagerdutyNotifier.getUrl();

    // Assert
    assertEquals("Client", actualClient);
    assertEquals("Service Key", actualServiceKey);
    assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualClientUrl.toString());
    assertSame(url, actualClientUrl);
    assertSame(url, actualUrl);
  }
}
