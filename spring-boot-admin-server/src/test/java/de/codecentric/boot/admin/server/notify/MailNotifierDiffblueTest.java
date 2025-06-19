package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {MailNotifier.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class MailNotifierDiffblueTest {
  @MockitoBean
  private InstanceRepository instanceRepository;

  @MockitoBean
  private JavaMailSender javaMailSender;

  @Autowired
  private MailNotifier mailNotifier;

  @MockitoBean
  private TemplateEngine templateEngine;

  /**
   * Test {@link MailNotifier#MailNotifier(JavaMailSender, InstanceRepository, TemplateEngine)}.
   * <p>
   * Method under test: {@link MailNotifier#MailNotifier(JavaMailSender, InstanceRepository, TemplateEngine)}
   */
  @Test
  public void testNewMailNotifier() {
    // Arrange and Act
    MailNotifier actualMailNotifier = new MailNotifier(javaMailSender, instanceRepository, templateEngine);

    // Assert
    assertEquals("META-INF/spring-boot-admin-server/mail/status-changed.html", actualMailNotifier.getTemplate());
    assertEquals("Spring Boot Admin <noreply@localhost>", actualMailNotifier.getFrom());
    assertNull(actualMailNotifier.getBaseUrl());
    assertEquals(0, actualMailNotifier.getCc().length);
    assertTrue(actualMailNotifier.isEnabled());
    assertTrue(actualMailNotifier.getAdditionalProperties().isEmpty());
    assertArrayEquals(new String[]{"UNKNOWN:UP"}, actualMailNotifier.getIgnoreChanges());
    assertArrayEquals(new String[]{"root@localhost"}, actualMailNotifier.getTo());
  }

  /**
   * Test {@link MailNotifier#doNotify(InstanceEvent, Instance)}.
   * <p>
   * Method under test: {@link MailNotifier#doNotify(InstanceEvent, Instance)}
   */
  @Test
  public void testDoNotify() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Void> createResult = StepVerifier
        .create(mailNotifier.doNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link MailNotifier#getBody(Context)}.
   * <ul>
   *   <li>Given {@link TemplateEngine} {@link TemplateEngine#process(String, IContext)} return {@code Process}.</li>
   *   <li>Then return {@code Process}.</li>
   * </ul>
   * <p>
   * Method under test: {@link MailNotifier#getBody(Context)}
   */
  @Test
  public void testGetBody_givenTemplateEngineProcessReturnProcess_thenReturnProcess() {
    // Arrange
    when(templateEngine.process(Mockito.<String>any(), Mockito.<IContext>any())).thenReturn("Process");

    // Act
    String actualBody = mailNotifier.getBody(new Context());

    // Assert
    verify(templateEngine).process(eq("META-INF/spring-boot-admin-server/mail/status-changed.html"),
        isA(IContext.class));
    assertEquals("Process", actualBody);
  }

  /**
   * Test {@link MailNotifier#getBody(Context)}.
   * <ul>
   *   <li>Then throw {@link RuntimeException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link MailNotifier#getBody(Context)}
   */
  @Test
  public void testGetBody_thenThrowRuntimeException() {
    // Arrange
    when(templateEngine.process(Mockito.<String>any(), Mockito.<IContext>any())).thenThrow(new RuntimeException("foo"));

    // Act and Assert
    assertThrows(RuntimeException.class, () -> mailNotifier.getBody(new Context()));
    verify(templateEngine).process(eq("META-INF/spring-boot-admin-server/mail/status-changed.html"),
        isA(IContext.class));
  }

  /**
   * Test {@link MailNotifier#getSubject(Context)}.
   * <ul>
   *   <li>Given {@link TemplateEngine} {@link TemplateEngine#process(String, Set, IContext)} return {@code Process}.</li>
   *   <li>Then return {@code Process}.</li>
   * </ul>
   * <p>
   * Method under test: {@link MailNotifier#getSubject(Context)}
   */
  @Test
  public void testGetSubject_givenTemplateEngineProcessReturnProcess_thenReturnProcess() {
    // Arrange
    when(templateEngine.process(Mockito.<String>any(), Mockito.<Set<String>>any(), Mockito.<IContext>any()))
        .thenReturn("Process");

    // Act
    String actualSubject = mailNotifier.getSubject(new Context());

    // Assert
    verify(templateEngine).process(eq("META-INF/spring-boot-admin-server/mail/status-changed.html"), isA(Set.class),
        isA(IContext.class));
    assertEquals("Process", actualSubject);
  }

  /**
   * Test {@link MailNotifier#getSubject(Context)}.
   * <ul>
   *   <li>Then throw {@link RuntimeException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link MailNotifier#getSubject(Context)}
   */
  @Test
  public void testGetSubject_thenThrowRuntimeException() {
    // Arrange
    when(templateEngine.process(Mockito.<String>any(), Mockito.<Set<String>>any(), Mockito.<IContext>any()))
        .thenThrow(new RuntimeException("subject"));

    // Act and Assert
    assertThrows(RuntimeException.class, () -> mailNotifier.getSubject(new Context()));
    verify(templateEngine).process(eq("META-INF/spring-boot-admin-server/mail/status-changed.html"), isA(Set.class),
        isA(IContext.class));
  }

  /**
   * Test {@link MailNotifier#getTo()}.
   * <p>
   * Method under test: {@link MailNotifier#getTo()}
   */
  @Test
  public void testGetTo() {
    // Arrange, Act and Assert
    assertArrayEquals(new String[]{"root@localhost"}, mailNotifier.getTo());
  }

  /**
   * Test {@link MailNotifier#setTo(String[])}.
   * <p>
   * Method under test: {@link MailNotifier#setTo(String[])}
   */
  @Test
  public void testSetTo() {
    // Arrange and Act
    mailNotifier.setTo(new String[]{"alice.liddell@example.org"});

    // Assert
    assertArrayEquals(new String[]{"alice.liddell@example.org"}, mailNotifier.getTo());
  }

  /**
   * Test {@link MailNotifier#getCc()}.
   * <p>
   * Method under test: {@link MailNotifier#getCc()}
   */
  @Test
  public void testGetCc() {
    // Arrange, Act and Assert
    assertEquals(0, mailNotifier.getCc().length);
  }

  /**
   * Test {@link MailNotifier#setCc(String[])}.
   * <p>
   * Method under test: {@link MailNotifier#setCc(String[])}
   */
  @Test
  public void testSetCc() {
    // Arrange and Act
    mailNotifier.setCc(new String[]{"ada.lovelace@example.org"});

    // Assert
    assertArrayEquals(new String[]{"ada.lovelace@example.org"}, mailNotifier.getCc());
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link MailNotifier#setAdditionalProperties(Map)}
   *   <li>{@link MailNotifier#setBaseUrl(String)}
   *   <li>{@link MailNotifier#setFrom(String)}
   *   <li>{@link MailNotifier#setTemplate(String)}
   *   <li>{@link MailNotifier#getAdditionalProperties()}
   *   <li>{@link MailNotifier#getBaseUrl()}
   *   <li>{@link MailNotifier#getFrom()}
   *   <li>{@link MailNotifier#getTemplate()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    MailNotifier mailNotifier = new MailNotifier(mailSender, repository, new TemplateEngine());
    HashMap<String, Object> additionalProperties = new HashMap<>();

    // Act
    mailNotifier.setAdditionalProperties(additionalProperties);
    mailNotifier.setBaseUrl("https://example.org/example");
    mailNotifier.setFrom("jane.doe@example.org");
    mailNotifier.setTemplate("Template");
    Map<String, Object> actualAdditionalProperties = mailNotifier.getAdditionalProperties();
    String actualBaseUrl = mailNotifier.getBaseUrl();
    String actualFrom = mailNotifier.getFrom();

    // Assert
    assertEquals("Template", mailNotifier.getTemplate());
    assertEquals("https://example.org/example", actualBaseUrl);
    assertEquals("jane.doe@example.org", actualFrom);
    assertTrue(actualAdditionalProperties.isEmpty());
    assertSame(additionalProperties, actualAdditionalProperties);
  }
}
