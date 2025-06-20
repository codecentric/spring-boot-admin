package de.codecentric.boot.admin.server.notify;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardOperatorOverloader;
import org.springframework.expression.spel.support.StandardTypeComparator;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = { MicrosoftTeamsNotifier.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisabledInAotMode
@RunWith(SpringJUnit4ClassRunner.class)
public class MicrosoftTeamsNotifierDiffblueTest {

	@MockitoBean
	private InstanceRepository instanceRepository;

	@Autowired
	private MicrosoftTeamsNotifier microsoftTeamsNotifier;

	@MockitoBean
	private RestTemplate restTemplate;

	/**
	 * Test
	 * {@link MicrosoftTeamsNotifier#MicrosoftTeamsNotifier(InstanceRepository, RestTemplate)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#MicrosoftTeamsNotifier(InstanceRepository, RestTemplate)}
	 */
	@Test
	public void testNewMicrosoftTeamsNotifier() {
		// Arrange and Act
		MicrosoftTeamsNotifier actualMicrosoftTeamsNotifier = new MicrosoftTeamsNotifier(instanceRepository,
				mock(RestTemplate.class));

		// Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event"
				+ ".statusInfo.status}", actualMicrosoftTeamsNotifier.getStatusActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin",
				actualMicrosoftTeamsNotifier.getDeregisterActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin",
				actualMicrosoftTeamsNotifier.getRegisterActivitySubtitle());
		assertEquals("De-Registered", actualMicrosoftTeamsNotifier.getDeRegisteredTitle());
		assertEquals("Registered", actualMicrosoftTeamsNotifier.getRegisteredTitle());
		assertEquals("Spring Boot Admin Notification", actualMicrosoftTeamsNotifier.getMessageSummary());
		assertEquals("Status Changed", actualMicrosoftTeamsNotifier.getStatusChangedTitle());
		assertEquals(
				"event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? '6db33f' : 'b32d36') : '439fe0'",
				actualMicrosoftTeamsNotifier.getThemeColor());
		assertNull(actualMicrosoftTeamsNotifier.getWebhookUrl());
		assertTrue(actualMicrosoftTeamsNotifier.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualMicrosoftTeamsNotifier.getIgnoreChanges());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testShouldNotify() {
		// Arrange
		InstanceId instance = InstanceId.of("42");
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act and Assert
		assertTrue(microsoftTeamsNotifier.shouldNotify(new InstanceRegisteredEvent(instance, 1L, registration), null));
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testShouldNotify2() {
		// Arrange, Act and Assert
		assertTrue(microsoftTeamsNotifier.shouldNotify(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null));
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#shouldNotify(InstanceEvent, Instance)}
	 */
	@Test
	public void testShouldNotify_whenNull_thenReturnFalse() {
		// Arrange, Act and Assert
		assertFalse(microsoftTeamsNotifier.shouldNotify(null, null));
	}

	/**
	 * Test
	 * {@link MicrosoftTeamsNotifier#evaluateExpression(EvaluationContext, Expression)}.
	 * <ul>
	 * <li>When {@link StandardEvaluationContext#StandardEvaluationContext()}.</li>
	 * <li>Then return {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#evaluateExpression(EvaluationContext, Expression)}
	 */
	@Test
	public void testEvaluateExpression_whenStandardEvaluationContext_thenReturn42() {
		// Arrange
		StandardEvaluationContext context = new StandardEvaluationContext();

		// Act and Assert
		assertEquals("42", microsoftTeamsNotifier.evaluateExpression(context, new LiteralExpression("42")));
	}

	/**
	 * Test
	 * {@link MicrosoftTeamsNotifier#createEvaluationContext(InstanceEvent, Instance)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#createEvaluationContext(InstanceEvent, Instance)}
	 */
	@Test
	public void testCreateEvaluationContext() {
		// Arrange and Act
		EvaluationContext actualCreateEvaluationContextResult = microsoftTeamsNotifier
			.createEvaluationContext(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L), null);

		// Assert
		Object value = actualCreateEvaluationContextResult.getRootObject().getValue();
		assertTrue(value instanceof Map);
		List<PropertyAccessor> propertyAccessors = actualCreateEvaluationContextResult.getPropertyAccessors();
		assertEquals(2, propertyAccessors.size());
		assertTrue(propertyAccessors.get(1) instanceof MapAccessor);
		assertTrue(propertyAccessors.get(0) instanceof DataBindingPropertyAccessor);
		assertTrue(actualCreateEvaluationContextResult instanceof SimpleEvaluationContext);
		assertTrue(actualCreateEvaluationContextResult.getOperatorOverloader() instanceof StandardOperatorOverloader);
		assertTrue(actualCreateEvaluationContextResult.getTypeComparator() instanceof StandardTypeComparator);
		assertTrue(actualCreateEvaluationContextResult.getTypeConverter() instanceof StandardTypeConverter);
		assertNull(actualCreateEvaluationContextResult.getBeanResolver());
		assertEquals(3, ((Map<String, Object>) value).size());
		List<ConstructorResolver> constructorResolvers = actualCreateEvaluationContextResult.getConstructorResolvers();
		assertTrue(constructorResolvers.isEmpty());
		assertTrue(((Map<String, Object>) value).containsKey("event"));
		assertTrue(((Map<String, Object>) value).containsKey("instance"));
		assertTrue(((Map<String, Object>) value).containsKey("lastStatus"));
		assertTrue(actualCreateEvaluationContextResult.isAssignmentEnabled());
		assertSame(constructorResolvers, actualCreateEvaluationContextResult.getIndexAccessors());
		assertSame(constructorResolvers, actualCreateEvaluationContextResult.getMethodResolvers());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#getThemeColor()}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#getThemeColor()}
	 */
	@Test
	public void testGetThemeColor() {
		// Arrange, Act and Assert
		assertEquals(
				"event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? '6db33f' : 'b32d36') : '439fe0'",
				microsoftTeamsNotifier.getThemeColor());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#setThemeColor(String)}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#setThemeColor(String)}
	 */
	@Test
	public void testSetThemeColor() {
		// Arrange and Act
		microsoftTeamsNotifier.setThemeColor("Theme Color");

		// Assert
		assertEquals("Theme Color", microsoftTeamsNotifier.getThemeColor());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#getDeregisterActivitySubtitle()}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#getDeregisterActivitySubtitle()}
	 */
	@Test
	public void testGetDeregisterActivitySubtitle() {
		// Arrange, Act and Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin",
				microsoftTeamsNotifier.getDeregisterActivitySubtitle());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#setDeregisterActivitySubtitle(String)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#setDeregisterActivitySubtitle(String)}
	 */
	@Test
	public void testSetDeregisterActivitySubtitle() {
		// Arrange and Act
		microsoftTeamsNotifier.setDeregisterActivitySubtitle("Dr");

		// Assert
		assertEquals("Dr", microsoftTeamsNotifier.getDeregisterActivitySubtitle());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#getRegisterActivitySubtitle()}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#getRegisterActivitySubtitle()}
	 */
	@Test
	public void testGetRegisterActivitySubtitle() {
		// Arrange, Act and Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin",
				microsoftTeamsNotifier.getRegisterActivitySubtitle());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#setRegisterActivitySubtitle(String)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifier#setRegisterActivitySubtitle(String)}
	 */
	@Test
	public void testSetRegisterActivitySubtitle() {
		// Arrange and Act
		microsoftTeamsNotifier.setRegisterActivitySubtitle("Dr");

		// Assert
		assertEquals("Dr", microsoftTeamsNotifier.getRegisterActivitySubtitle());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#getStatusActivitySubtitle()}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#getStatusActivitySubtitle()}
	 */
	@Test
	public void testGetStatusActivitySubtitle() {
		// Arrange, Act and Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event"
				+ ".statusInfo.status}", microsoftTeamsNotifier.getStatusActivitySubtitle());
	}

	/**
	 * Test {@link MicrosoftTeamsNotifier#setStatusActivitySubtitle(String)}.
	 * <p>
	 * Method under test: {@link MicrosoftTeamsNotifier#setStatusActivitySubtitle(String)}
	 */
	@Test
	public void testSetStatusActivitySubtitle() {
		// Arrange and Act
		microsoftTeamsNotifier.setStatusActivitySubtitle("Dr");

		// Assert
		assertEquals("Dr", microsoftTeamsNotifier.getStatusActivitySubtitle());
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>{@link MicrosoftTeamsNotifier#setDeRegisteredTitle(String)}
	 * <li>{@link MicrosoftTeamsNotifier#setMessageSummary(String)}
	 * <li>{@link MicrosoftTeamsNotifier#setRegisteredTitle(String)}
	 * <li>{@link MicrosoftTeamsNotifier#setRestTemplate(RestTemplate)}
	 * <li>{@link MicrosoftTeamsNotifier#setStatusChangedTitle(String)}
	 * <li>{@link MicrosoftTeamsNotifier#setWebhookUrl(URI)}
	 * <li>{@link MicrosoftTeamsNotifier#getDeRegisteredTitle()}
	 * <li>{@link MicrosoftTeamsNotifier#getMessageSummary()}
	 * <li>{@link MicrosoftTeamsNotifier#getRegisteredTitle()}
	 * <li>{@link MicrosoftTeamsNotifier#getStatusChangedTitle()}
	 * <li>{@link MicrosoftTeamsNotifier#getWebhookUrl()}
	 * </ul>
	 */
	@Test
	public void testGettersAndSetters() {
		// Arrange
		MicrosoftTeamsNotifier microsoftTeamsNotifier = new MicrosoftTeamsNotifier(
				new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(RestTemplate.class));

		// Act
		microsoftTeamsNotifier.setDeRegisteredTitle("Dr");
		microsoftTeamsNotifier.setMessageSummary("Message Summary");
		microsoftTeamsNotifier.setRegisteredTitle("Dr");
		microsoftTeamsNotifier.setRestTemplate(mock(RestTemplate.class));
		microsoftTeamsNotifier.setStatusChangedTitle("Dr");
		URI webhookUrl = PagerdutyNotifier.DEFAULT_URI;
		microsoftTeamsNotifier.setWebhookUrl(webhookUrl);
		String actualDeRegisteredTitle = microsoftTeamsNotifier.getDeRegisteredTitle();
		String actualMessageSummary = microsoftTeamsNotifier.getMessageSummary();
		String actualRegisteredTitle = microsoftTeamsNotifier.getRegisteredTitle();
		String actualStatusChangedTitle = microsoftTeamsNotifier.getStatusChangedTitle();
		URI actualWebhookUrl = microsoftTeamsNotifier.getWebhookUrl();

		// Assert
		assertEquals("Dr", actualDeRegisteredTitle);
		assertEquals("Dr", actualRegisteredTitle);
		assertEquals("Dr", actualStatusChangedTitle);
		assertEquals("Message Summary", actualMessageSummary);
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", actualWebhookUrl.toString());
		assertSame(webhookUrl, actualWebhookUrl);
	}

}
