package de.codecentric.boot.admin.server.config;

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
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.CompositeNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.CompositeNotifierConfiguration.NoSingleNotifierCandidateCondition;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.DingTalkNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.DiscordNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.FeiShuNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.FilteringNotifierWebConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.HipchatNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.LetsChatNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.MailNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.MicrosoftTeamsNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.OpsGenieNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.PagerdutyNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.RocketChatNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.SlackNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.TelegramNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.WebexNotifierConfiguration;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.DingTalkNotifier;
import de.codecentric.boot.admin.server.notify.DiscordNotifier;
import de.codecentric.boot.admin.server.notify.FeiShuNotifier;
import de.codecentric.boot.admin.server.notify.FeiShuNotifier.Card;
import de.codecentric.boot.admin.server.notify.FeiShuNotifier.MessageType;
import de.codecentric.boot.admin.server.notify.HipchatNotifier;
import de.codecentric.boot.admin.server.notify.LetsChatNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.NotifierProxyProperties;
import de.codecentric.boot.admin.server.notify.OpsGenieNotifier;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.notify.RocketChatNotifier;
import de.codecentric.boot.admin.server.notify.SlackNotifier;
import de.codecentric.boot.admin.server.notify.TelegramNotifier;
import de.codecentric.boot.admin.server.notify.WebexNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.EngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.context.StandardEngineContextFactory;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateparser.markup.decoupled.StandardDecoupledTemplateLogicResolver;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@RunWith(MockitoJUnitRunner.class)
public class AdminServerNotifierAutoConfigurationDiffblueTest {

	@Mock
	private FilteringNotifier filteringNotifier;

	@InjectMocks
	private FilteringNotifierWebConfiguration filteringNotifierWebConfiguration;

	@InjectMocks
	private MailNotifierConfiguration mailNotifierConfiguration;

	/**
	 * Test CompositeNotifierConfiguration
	 * {@link CompositeNotifierConfiguration#compositeNotifier(List)}.
	 * <ul>
	 * <li>Then {@link ArrayList#ArrayList()} Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeNotifierConfiguration#compositeNotifier(List)}
	 */
	@Test
	public void testCompositeNotifierConfigurationCompositeNotifier_thenArrayListEmpty() throws AssertionError {
		// Arrange
		CompositeNotifierConfiguration compositeNotifierConfiguration = new CompositeNotifierConfiguration();
		ArrayList<Notifier> notifiers = new ArrayList<>();

		// Act
		CompositeNotifier actualCompositeNotifierResult = compositeNotifierConfiguration.compositeNotifier(notifiers);
		InstanceId instance = InstanceId.of("42");
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance, 1L);

		Mono<Void> actualPublisher = actualCompositeNotifierResult.notify(event);

		// Assert
		assertEquals("DEREGISTERED", event.getType());
		assertEquals(1L, event.getVersion());
		assertTrue(notifiers.isEmpty());
		assertSame(instance, event.getInstance());
		FirstStep<Void> createResult = StepVerifier.create(actualPublisher);
		createResult.expectComplete().verify();
	}

	/**
	 * Test CompositeNotifierConfiguration
	 * {@link CompositeNotifierConfiguration#compositeNotifier(List)}.
	 * <ul>
	 * <li>Then {@link ArrayList#ArrayList()} Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeNotifierConfiguration#compositeNotifier(List)}
	 */
	@Test
	public void testCompositeNotifierConfigurationCompositeNotifier_thenArrayListEmpty2() {
		// Arrange
		CompositeNotifierConfiguration compositeNotifierConfiguration = new CompositeNotifierConfiguration();
		ArrayList<Notifier> notifiers = new ArrayList<>();

		// Act
		compositeNotifierConfiguration.compositeNotifier(notifiers);

		// Assert that nothing has changed
		assertTrue(notifiers.isEmpty());
	}

	/**
	 * Test CompositeNotifierConfiguration
	 * {@link CompositeNotifierConfiguration#compositeNotifier(List)}.
	 * <ul>
	 * <li>Then {@link ArrayList#ArrayList()} size is one.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeNotifierConfiguration#compositeNotifier(List)}
	 */
	@Test
	public void testCompositeNotifierConfigurationCompositeNotifier_thenArrayListSizeIsOne() throws AssertionError {
		// Arrange
		CompositeNotifierConfiguration compositeNotifierConfiguration = new CompositeNotifierConfiguration();

		ArrayList<Notifier> notifiers = new ArrayList<>();
		notifiers.add(mock(Notifier.class));

		// Act
		CompositeNotifier actualCompositeNotifierResult = compositeNotifierConfiguration.compositeNotifier(notifiers);
		InstanceId instance = InstanceId.of("42");
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance, 1L);

		Mono<Void> actualPublisher = actualCompositeNotifierResult.notify(event);

		// Assert
		assertEquals("DEREGISTERED", event.getType());
		assertEquals(1, notifiers.size());
		assertEquals(1L, event.getVersion());
		assertSame(instance, event.getInstance());
		FirstStep<Void> createResult = StepVerifier.create(actualPublisher);
		createResult.expectError().verify();
	}

	/**
	 * Test CompositeNotifierConfiguration
	 * {@link CompositeNotifierConfiguration#compositeNotifier(List)}.
	 * <ul>
	 * <li>Then {@link ArrayList#ArrayList()} size is two.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeNotifierConfiguration#compositeNotifier(List)}
	 */
	@Test
	public void testCompositeNotifierConfigurationCompositeNotifier_thenArrayListSizeIsTwo() throws AssertionError {
		// Arrange
		CompositeNotifierConfiguration compositeNotifierConfiguration = new CompositeNotifierConfiguration();

		ArrayList<Notifier> notifiers = new ArrayList<>();
		notifiers.add(mock(Notifier.class));
		notifiers.add(mock(Notifier.class));

		// Act
		CompositeNotifier actualCompositeNotifierResult = compositeNotifierConfiguration.compositeNotifier(notifiers);
		InstanceId instance = InstanceId.of("42");
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance, 1L);

		Mono<Void> actualPublisher = actualCompositeNotifierResult.notify(event);

		// Assert
		assertEquals("DEREGISTERED", event.getType());
		assertEquals(1L, event.getVersion());
		assertEquals(2, notifiers.size());
		assertSame(instance, event.getInstance());
		FirstStep<Void> createResult = StepVerifier.create(actualPublisher);
		createResult.expectError().verify();
	}

	/**
	 * Test CompositeNotifierConfiguration_NoSingleNotifierCandidateCondition new
	 * {@link CompositeNotifierConfiguration.NoSingleNotifierCandidateCondition} (default
	 * constructor).
	 * <p>
	 * Method under test: default or parameterless constructor of
	 * {@link CompositeNotifierConfiguration.NoSingleNotifierCandidateCondition}
	 */
	@Test
	public void testCompositeNotifierConfiguration_NoSingleNotifierCandidateConditionNewNoSingleNotifierCandidateCondition() {
		// Arrange, Act and Assert
		assertEquals(ConfigurationPhase.REGISTER_BEAN,
				new NoSingleNotifierCandidateCondition().getConfigurationPhase());
	}

	/**
	 * Test DingTalkNotifierConfiguration
	 * {@link DingTalkNotifierConfiguration#dingTalkNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link DingTalkNotifierConfiguration#dingTalkNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testDingTalkNotifierConfigurationDingTalkNotifier() {
		// Arrange
		DingTalkNotifierConfiguration dingTalkNotifierConfiguration = new DingTalkNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		DingTalkNotifier actualDingTalkNotifierResult = dingTalkNotifierConfiguration.dingTalkNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name} #{instance.id} is #{event.statusInfo.status}",
				actualDingTalkNotifierResult.getMessage());
		assertNull(actualDingTalkNotifierResult.getSecret());
		assertNull(actualDingTalkNotifierResult.getWebhookUrl());
		assertTrue(actualDingTalkNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualDingTalkNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test DingTalkNotifierConfiguration
	 * {@link DingTalkNotifierConfiguration#dingTalkNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DingTalkNotifierConfiguration#dingTalkNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testDingTalkNotifierConfigurationDingTalkNotifier_givenMinusOne() {
		// Arrange
		DingTalkNotifierConfiguration dingTalkNotifierConfiguration = new DingTalkNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		DingTalkNotifier actualDingTalkNotifierResult = dingTalkNotifierConfiguration.dingTalkNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name} #{instance.id} is #{event.statusInfo.status}",
				actualDingTalkNotifierResult.getMessage());
		assertNull(actualDingTalkNotifierResult.getSecret());
		assertNull(actualDingTalkNotifierResult.getWebhookUrl());
		assertTrue(actualDingTalkNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualDingTalkNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test DiscordNotifierConfiguration
	 * {@link DiscordNotifierConfiguration#discordNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifierConfiguration#discordNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testDiscordNotifierConfigurationDiscordNotifier() {
		// Arrange
		DiscordNotifierConfiguration discordNotifierConfiguration = new DiscordNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		DiscordNotifier actualDiscordNotifierResult = discordNotifierConfiguration.discordNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualDiscordNotifierResult.getMessage());
		assertNull(actualDiscordNotifierResult.getAvatarUrl());
		assertNull(actualDiscordNotifierResult.getUsername());
		assertNull(actualDiscordNotifierResult.getWebhookUrl());
		assertFalse(actualDiscordNotifierResult.isTts());
		assertTrue(actualDiscordNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualDiscordNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test DiscordNotifierConfiguration
	 * {@link DiscordNotifierConfiguration#discordNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DiscordNotifierConfiguration#discordNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testDiscordNotifierConfigurationDiscordNotifier_givenMinusOne() {
		// Arrange
		DiscordNotifierConfiguration discordNotifierConfiguration = new DiscordNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		DiscordNotifier actualDiscordNotifierResult = discordNotifierConfiguration.discordNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualDiscordNotifierResult.getMessage());
		assertNull(actualDiscordNotifierResult.getAvatarUrl());
		assertNull(actualDiscordNotifierResult.getUsername());
		assertNull(actualDiscordNotifierResult.getWebhookUrl());
		assertFalse(actualDiscordNotifierResult.isTts());
		assertTrue(actualDiscordNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualDiscordNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test FeiShuNotifierConfiguration
	 * {@link FeiShuNotifierConfiguration#feiShuNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link FeiShuNotifierConfiguration#feiShuNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testFeiShuNotifierConfigurationFeiShuNotifier() {
		// Arrange
		FeiShuNotifierConfiguration feiShuNotifierConfiguration = new FeiShuNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		FeiShuNotifier actualFeiShuNotifierResult = feiShuNotifierConfiguration.feiShuNotifier(repository,
				proxyProperties);

		// Assert
		Card card = actualFeiShuNotifierResult.getCard();
		assertEquals("Codecentric's Spring Boot Admin notice", card.getTitle());
		assertEquals(
				"ServiceName: #{instance.registration.name}(#{instance.id}) \n"
						+ "ServiceUrl: #{instance.registration.serviceUrl} \n"
						+ "Status: changed status from [#{lastStatus}] to [#{event.statusInfo.status}]",
				actualFeiShuNotifierResult.getMessage());
		assertEquals("red", card.getThemeColor());
		assertNull(actualFeiShuNotifierResult.getSecret());
		assertNull(actualFeiShuNotifierResult.getWebhookUrl());
		assertEquals(MessageType.interactive, actualFeiShuNotifierResult.getMessageType());
		assertTrue(actualFeiShuNotifierResult.isEnabled());
		assertTrue(actualFeiShuNotifierResult.isAtAll());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualFeiShuNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test FeiShuNotifierConfiguration
	 * {@link FeiShuNotifierConfiguration#feiShuNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link FeiShuNotifierConfiguration#feiShuNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testFeiShuNotifierConfigurationFeiShuNotifier_givenMinusOne() {
		// Arrange
		FeiShuNotifierConfiguration feiShuNotifierConfiguration = new FeiShuNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		FeiShuNotifier actualFeiShuNotifierResult = feiShuNotifierConfiguration.feiShuNotifier(repository,
				proxyProperties);

		// Assert
		Card card = actualFeiShuNotifierResult.getCard();
		assertEquals("Codecentric's Spring Boot Admin notice", card.getTitle());
		assertEquals(
				"ServiceName: #{instance.registration.name}(#{instance.id}) \n"
						+ "ServiceUrl: #{instance.registration.serviceUrl} \n"
						+ "Status: changed status from [#{lastStatus}] to [#{event.statusInfo.status}]",
				actualFeiShuNotifierResult.getMessage());
		assertEquals("red", card.getThemeColor());
		assertNull(actualFeiShuNotifierResult.getSecret());
		assertNull(actualFeiShuNotifierResult.getWebhookUrl());
		assertEquals(MessageType.interactive, actualFeiShuNotifierResult.getMessageType());
		assertTrue(actualFeiShuNotifierResult.isEnabled());
		assertTrue(actualFeiShuNotifierResult.isAtAll());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualFeiShuNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test FilteringNotifierWebConfiguration
	 * {@link FilteringNotifierWebConfiguration#notificationFilterController()}.
	 * <p>
	 * Method under test:
	 * {@link FilteringNotifierWebConfiguration#notificationFilterController()}
	 */
	@Test
	public void testFilteringNotifierWebConfigurationNotificationFilterController() {
		// Arrange, Act and Assert
		assertTrue(filteringNotifierWebConfiguration.notificationFilterController().getFilters().isEmpty());
	}

	/**
	 * Test HipchatNotifierConfiguration
	 * {@link HipchatNotifierConfiguration#hipchatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link HipchatNotifierConfiguration#hipchatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testHipchatNotifierConfigurationHipchatNotifier_givenMinusOne() {
		// Arrange
		HipchatNotifierConfiguration hipchatNotifierConfiguration = new HipchatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		HipchatNotifier actualHipchatNotifierResult = hipchatNotifierConfiguration.hipchatNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals(
				"<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status"
						+ "}</strong>",
				actualHipchatNotifierResult.getDescription());
		assertNull(actualHipchatNotifierResult.getAuthToken());
		assertNull(actualHipchatNotifierResult.getRoomId());
		assertNull(actualHipchatNotifierResult.getUrl());
		assertFalse(actualHipchatNotifierResult.isNotify());
		assertTrue(actualHipchatNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualHipchatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test HipchatNotifierConfiguration
	 * {@link HipchatNotifierConfiguration#hipchatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Then return Description is a string.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link HipchatNotifierConfiguration#hipchatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testHipchatNotifierConfigurationHipchatNotifier_thenReturnDescriptionIsAString() {
		// Arrange
		HipchatNotifierConfiguration hipchatNotifierConfiguration = new HipchatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		HipchatNotifier actualHipchatNotifierResult = hipchatNotifierConfiguration.hipchatNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals(
				"<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status"
						+ "}</strong>",
				actualHipchatNotifierResult.getDescription());
		assertNull(actualHipchatNotifierResult.getAuthToken());
		assertNull(actualHipchatNotifierResult.getRoomId());
		assertNull(actualHipchatNotifierResult.getUrl());
		assertFalse(actualHipchatNotifierResult.isNotify());
		assertTrue(actualHipchatNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualHipchatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test LetsChatNotifierConfiguration
	 * {@link LetsChatNotifierConfiguration#letsChatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link LetsChatNotifierConfiguration#letsChatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testLetsChatNotifierConfigurationLetsChatNotifier() {
		// Arrange
		LetsChatNotifierConfiguration letsChatNotifierConfiguration = new LetsChatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		LetsChatNotifier actualLetsChatNotifierResult = letsChatNotifierConfiguration.letsChatNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualLetsChatNotifierResult.getMessage());
		assertEquals("Spring Boot Admin", actualLetsChatNotifierResult.getUsername());
		assertNull(actualLetsChatNotifierResult.getRoom());
		assertNull(actualLetsChatNotifierResult.getToken());
		assertNull(actualLetsChatNotifierResult.getUrl());
		assertTrue(actualLetsChatNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualLetsChatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test LetsChatNotifierConfiguration
	 * {@link LetsChatNotifierConfiguration#letsChatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link LetsChatNotifierConfiguration#letsChatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testLetsChatNotifierConfigurationLetsChatNotifier_givenMinusOne() {
		// Arrange
		LetsChatNotifierConfiguration letsChatNotifierConfiguration = new LetsChatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		LetsChatNotifier actualLetsChatNotifierResult = letsChatNotifierConfiguration.letsChatNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualLetsChatNotifierResult.getMessage());
		assertEquals("Spring Boot Admin", actualLetsChatNotifierResult.getUsername());
		assertNull(actualLetsChatNotifierResult.getRoom());
		assertNull(actualLetsChatNotifierResult.getToken());
		assertNull(actualLetsChatNotifierResult.getUrl());
		assertTrue(actualLetsChatNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualLetsChatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test MailNotifierConfiguration
	 * {@link MailNotifierConfiguration#mailNotifier(JavaMailSender, InstanceRepository, TemplateEngine)}.
	 * <p>
	 * Method under test:
	 * {@link MailNotifierConfiguration#mailNotifier(JavaMailSender, InstanceRepository, TemplateEngine)}
	 */
	@Test
	public void testMailNotifierConfigurationMailNotifier() {
		// Arrange
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		// Act
		MailNotifier actualMailNotifierResult = mailNotifierConfiguration.mailNotifier(mailSender, repository,
				new TemplateEngine());

		// Assert
		assertEquals("META-INF/spring-boot-admin-server/mail/status-changed.html",
				actualMailNotifierResult.getTemplate());
		assertEquals("Spring Boot Admin <noreply@localhost>", actualMailNotifierResult.getFrom());
		assertNull(actualMailNotifierResult.getBaseUrl());
		assertEquals(0, actualMailNotifierResult.getCc().length);
		assertTrue(actualMailNotifierResult.isEnabled());
		assertTrue(actualMailNotifierResult.getAdditionalProperties().isEmpty());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualMailNotifierResult.getIgnoreChanges());
		assertArrayEquals(new String[] { "root@localhost" }, actualMailNotifierResult.getTo());
	}

	/**
	 * Test MailNotifierConfiguration
	 * {@link MailNotifierConfiguration#mailNotifierTemplateEngine()}.
	 * <p>
	 * Method under test: {@link MailNotifierConfiguration#mailNotifierTemplateEngine()}
	 */
	@Test
	public void testMailNotifierConfigurationMailNotifierTemplateEngine() {
		// Arrange and Act
		TemplateEngine actualMailNotifierTemplateEngineResult = mailNotifierConfiguration.mailNotifierTemplateEngine();

		// Assert
		assertTrue(actualMailNotifierTemplateEngineResult.getConfiguration() instanceof EngineConfiguration);
		assertTrue(actualMailNotifierTemplateEngineResult.getCacheManager() instanceof StandardCacheManager);
		assertTrue(actualMailNotifierTemplateEngineResult
			.getEngineContextFactory() instanceof StandardEngineContextFactory);
		assertTrue(actualMailNotifierTemplateEngineResult instanceof SpringTemplateEngine);
		assertTrue(actualMailNotifierTemplateEngineResult
			.getDecoupledTemplateLogicResolver() instanceof StandardDecoupledTemplateLogicResolver);
		Map<String, Set<IDialect>> dialectsByPrefix = actualMailNotifierTemplateEngineResult.getDialectsByPrefix();
		assertEquals(1, dialectsByPrefix.size());
		assertEquals(1, actualMailNotifierTemplateEngineResult.getDialects().size());
		assertEquals(1, actualMailNotifierTemplateEngineResult.getLinkBuilders().size());
		assertEquals(1, actualMailNotifierTemplateEngineResult.getMessageResolvers().size());
		assertEquals(1, actualMailNotifierTemplateEngineResult.getTemplateResolvers().size());
		assertFalse(((SpringTemplateEngine) actualMailNotifierTemplateEngineResult).getEnableSpringELCompiler());
		assertFalse(((SpringTemplateEngine) actualMailNotifierTemplateEngineResult)
			.getRenderHiddenMarkersBeforeCheckboxes());
		assertTrue(dialectsByPrefix.containsKey(null));
		assertTrue(actualMailNotifierTemplateEngineResult.isInitialized());
	}

	/**
	 * Test MicrosoftTeamsNotifierConfiguration
	 * {@link MicrosoftTeamsNotifierConfiguration#microsoftTeamsNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifierConfiguration#microsoftTeamsNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testMicrosoftTeamsNotifierConfigurationMicrosoftTeamsNotifier() {
		// Arrange
		MicrosoftTeamsNotifierConfiguration microsoftTeamsNotifierConfiguration = new MicrosoftTeamsNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		MicrosoftTeamsNotifier actualMicrosoftTeamsNotifierResult = microsoftTeamsNotifierConfiguration
			.microsoftTeamsNotifier(repository, proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event"
				+ ".statusInfo.status}", actualMicrosoftTeamsNotifierResult.getStatusActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin",
				actualMicrosoftTeamsNotifierResult.getDeregisterActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin",
				actualMicrosoftTeamsNotifierResult.getRegisterActivitySubtitle());
		assertEquals("De-Registered", actualMicrosoftTeamsNotifierResult.getDeRegisteredTitle());
		assertEquals("Registered", actualMicrosoftTeamsNotifierResult.getRegisteredTitle());
		assertEquals("Spring Boot Admin Notification", actualMicrosoftTeamsNotifierResult.getMessageSummary());
		assertEquals("Status Changed", actualMicrosoftTeamsNotifierResult.getStatusChangedTitle());
		assertEquals(
				"event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? '6db33f' : 'b32d36') : '439fe0'",
				actualMicrosoftTeamsNotifierResult.getThemeColor());
		assertNull(actualMicrosoftTeamsNotifierResult.getWebhookUrl());
		assertTrue(actualMicrosoftTeamsNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualMicrosoftTeamsNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test MicrosoftTeamsNotifierConfiguration
	 * {@link MicrosoftTeamsNotifierConfiguration#microsoftTeamsNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link MicrosoftTeamsNotifierConfiguration#microsoftTeamsNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testMicrosoftTeamsNotifierConfigurationMicrosoftTeamsNotifier_givenMinusOne() {
		// Arrange
		MicrosoftTeamsNotifierConfiguration microsoftTeamsNotifierConfiguration = new MicrosoftTeamsNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		MicrosoftTeamsNotifier actualMicrosoftTeamsNotifierResult = microsoftTeamsNotifierConfiguration
			.microsoftTeamsNotifier(repository, proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event"
				+ ".statusInfo.status}", actualMicrosoftTeamsNotifierResult.getStatusActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin",
				actualMicrosoftTeamsNotifierResult.getDeregisterActivitySubtitle());
		assertEquals("#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin",
				actualMicrosoftTeamsNotifierResult.getRegisterActivitySubtitle());
		assertEquals("De-Registered", actualMicrosoftTeamsNotifierResult.getDeRegisteredTitle());
		assertEquals("Registered", actualMicrosoftTeamsNotifierResult.getRegisteredTitle());
		assertEquals("Spring Boot Admin Notification", actualMicrosoftTeamsNotifierResult.getMessageSummary());
		assertEquals("Status Changed", actualMicrosoftTeamsNotifierResult.getStatusChangedTitle());
		assertEquals(
				"event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? '6db33f' : 'b32d36') : '439fe0'",
				actualMicrosoftTeamsNotifierResult.getThemeColor());
		assertNull(actualMicrosoftTeamsNotifierResult.getWebhookUrl());
		assertTrue(actualMicrosoftTeamsNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualMicrosoftTeamsNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test OpsGenieNotifierConfiguration
	 * {@link OpsGenieNotifierConfiguration#opsgenieNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link OpsGenieNotifierConfiguration#opsgenieNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testOpsGenieNotifierConfigurationOpsgenieNotifier() {
		// Arrange
		OpsGenieNotifierConfiguration opsGenieNotifierConfiguration = new OpsGenieNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		OpsGenieNotifier actualOpsgenieNotifierResult = opsGenieNotifierConfiguration.opsgenieNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
				actualOpsgenieNotifierResult.getMessage());
		assertEquals("https://api.opsgenie.com/v2/alerts", actualOpsgenieNotifierResult.getUrl().toString());
		assertNull(actualOpsgenieNotifierResult.getActions());
		assertNull(actualOpsgenieNotifierResult.getApiKey());
		assertNull(actualOpsgenieNotifierResult.getEntity());
		assertNull(actualOpsgenieNotifierResult.getSource());
		assertNull(actualOpsgenieNotifierResult.getTags());
		assertNull(actualOpsgenieNotifierResult.getUser());
		assertTrue(actualOpsgenieNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualOpsgenieNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test OpsGenieNotifierConfiguration
	 * {@link OpsGenieNotifierConfiguration#opsgenieNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link OpsGenieNotifierConfiguration#opsgenieNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testOpsGenieNotifierConfigurationOpsgenieNotifier_givenMinusOne() {
		// Arrange
		OpsGenieNotifierConfiguration opsGenieNotifierConfiguration = new OpsGenieNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		OpsGenieNotifier actualOpsgenieNotifierResult = opsGenieNotifierConfiguration.opsgenieNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
				actualOpsgenieNotifierResult.getMessage());
		assertEquals("https://api.opsgenie.com/v2/alerts", actualOpsgenieNotifierResult.getUrl().toString());
		assertNull(actualOpsgenieNotifierResult.getActions());
		assertNull(actualOpsgenieNotifierResult.getApiKey());
		assertNull(actualOpsgenieNotifierResult.getEntity());
		assertNull(actualOpsgenieNotifierResult.getSource());
		assertNull(actualOpsgenieNotifierResult.getTags());
		assertNull(actualOpsgenieNotifierResult.getUser());
		assertTrue(actualOpsgenieNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualOpsgenieNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test PagerdutyNotifierConfiguration
	 * {@link PagerdutyNotifierConfiguration#pagerdutyNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link PagerdutyNotifierConfiguration#pagerdutyNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testPagerdutyNotifierConfigurationPagerdutyNotifier() {
		// Arrange
		PagerdutyNotifierConfiguration pagerdutyNotifierConfiguration = new PagerdutyNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		PagerdutyNotifier actualPagerdutyNotifierResult = pagerdutyNotifierConfiguration.pagerdutyNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
				actualPagerdutyNotifierResult.getDescription());
		URI url = actualPagerdutyNotifierResult.getUrl();
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", url.toString());
		assertNull(actualPagerdutyNotifierResult.getClient());
		assertNull(actualPagerdutyNotifierResult.getServiceKey());
		assertNull(actualPagerdutyNotifierResult.getClientUrl());
		assertTrue(actualPagerdutyNotifierResult.isEnabled());
		assertSame(actualPagerdutyNotifierResult.DEFAULT_URI, url);
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualPagerdutyNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test PagerdutyNotifierConfiguration
	 * {@link PagerdutyNotifierConfiguration#pagerdutyNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link PagerdutyNotifierConfiguration#pagerdutyNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testPagerdutyNotifierConfigurationPagerdutyNotifier_givenMinusOne() {
		// Arrange
		PagerdutyNotifierConfiguration pagerdutyNotifierConfiguration = new PagerdutyNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		PagerdutyNotifier actualPagerdutyNotifierResult = pagerdutyNotifierConfiguration.pagerdutyNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}",
				actualPagerdutyNotifierResult.getDescription());
		URI url = actualPagerdutyNotifierResult.getUrl();
		assertEquals("https://events.pagerduty.com/generic/2010-04-15/create_event.json", url.toString());
		assertNull(actualPagerdutyNotifierResult.getClient());
		assertNull(actualPagerdutyNotifierResult.getServiceKey());
		assertNull(actualPagerdutyNotifierResult.getClientUrl());
		assertTrue(actualPagerdutyNotifierResult.isEnabled());
		assertSame(actualPagerdutyNotifierResult.DEFAULT_URI, url);
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualPagerdutyNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test RocketChatNotifierConfiguration
	 * {@link RocketChatNotifierConfiguration#rocketChatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link RocketChatNotifierConfiguration#rocketChatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testRocketChatNotifierConfigurationRocketChatNotifier() throws EvaluationException {
		// Arrange
		RocketChatNotifierConfiguration rocketChatNotifierConfiguration = new RocketChatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		RocketChatNotifier actualRocketChatNotifierResult = rocketChatNotifierConfiguration
			.rocketChatNotifier(repository, proxyProperties);

		// Assert
		Expression message = actualRocketChatNotifierResult.getMessage();
		assertTrue(message instanceof CompositeStringExpression);
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				message.getExpressionString());
		assertNull(actualRocketChatNotifierResult.getRoomId());
		assertNull(actualRocketChatNotifierResult.getToken());
		assertNull(actualRocketChatNotifierResult.getUrl());
		assertNull(actualRocketChatNotifierResult.getUserId());
		assertEquals(7, ((CompositeStringExpression) message).getExpressions().length);
		assertTrue(actualRocketChatNotifierResult.isEnabled());
		Class<String> expectedValueType = String.class;
		assertEquals(expectedValueType, message.getValueType());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualRocketChatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test RocketChatNotifierConfiguration
	 * {@link RocketChatNotifierConfiguration#rocketChatNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link RocketChatNotifierConfiguration#rocketChatNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testRocketChatNotifierConfigurationRocketChatNotifier_givenMinusOne() throws EvaluationException {
		// Arrange
		RocketChatNotifierConfiguration rocketChatNotifierConfiguration = new RocketChatNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		RocketChatNotifier actualRocketChatNotifierResult = rocketChatNotifierConfiguration
			.rocketChatNotifier(repository, proxyProperties);

		// Assert
		Expression message = actualRocketChatNotifierResult.getMessage();
		assertTrue(message instanceof CompositeStringExpression);
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				message.getExpressionString());
		assertNull(actualRocketChatNotifierResult.getRoomId());
		assertNull(actualRocketChatNotifierResult.getToken());
		assertNull(actualRocketChatNotifierResult.getUrl());
		assertNull(actualRocketChatNotifierResult.getUserId());
		assertEquals(7, ((CompositeStringExpression) message).getExpressions().length);
		assertTrue(actualRocketChatNotifierResult.isEnabled());
		Class<String> expectedValueType = String.class;
		assertEquals(expectedValueType, message.getValueType());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualRocketChatNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test SlackNotifierConfiguration
	 * {@link SlackNotifierConfiguration#slackNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link SlackNotifierConfiguration#slackNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testSlackNotifierConfigurationSlackNotifier() {
		// Arrange
		SlackNotifierConfiguration slackNotifierConfiguration = new SlackNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		SlackNotifier actualSlackNotifierResult = slackNotifierConfiguration.slackNotifier(repository, proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualSlackNotifierResult.getMessage());
		assertEquals("Spring Boot Admin", actualSlackNotifierResult.getUsername());
		assertNull(actualSlackNotifierResult.getChannel());
		assertNull(actualSlackNotifierResult.getIcon());
		assertNull(actualSlackNotifierResult.getWebhookUrl());
		assertTrue(actualSlackNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualSlackNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test SlackNotifierConfiguration
	 * {@link SlackNotifierConfiguration#slackNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SlackNotifierConfiguration#slackNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testSlackNotifierConfigurationSlackNotifier_givenMinusOne() {
		// Arrange
		SlackNotifierConfiguration slackNotifierConfiguration = new SlackNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		SlackNotifier actualSlackNotifierResult = slackNotifierConfiguration.slackNotifier(repository, proxyProperties);

		// Assert
		assertEquals("*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*",
				actualSlackNotifierResult.getMessage());
		assertEquals("Spring Boot Admin", actualSlackNotifierResult.getUsername());
		assertNull(actualSlackNotifierResult.getChannel());
		assertNull(actualSlackNotifierResult.getIcon());
		assertNull(actualSlackNotifierResult.getWebhookUrl());
		assertTrue(actualSlackNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualSlackNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test TelegramNotifierConfiguration
	 * {@link TelegramNotifierConfiguration#telegramNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link TelegramNotifierConfiguration#telegramNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testTelegramNotifierConfigurationTelegramNotifier_givenMinusOne() {
		// Arrange
		TelegramNotifierConfiguration telegramNotifierConfiguration = new TelegramNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		TelegramNotifier actualTelegramNotifierResult = telegramNotifierConfiguration.telegramNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("HTML", actualTelegramNotifierResult.getParseMode());
		assertEquals("https://api.telegram.org", actualTelegramNotifierResult.getApiUrl());
		assertNull(actualTelegramNotifierResult.getAuthToken());
		assertNull(actualTelegramNotifierResult.getChatId());
		assertFalse(actualTelegramNotifierResult.isDisableNotify());
		assertTrue(actualTelegramNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualTelegramNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test TelegramNotifierConfiguration
	 * {@link TelegramNotifierConfiguration#telegramNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Then return ParseMode is {@code HTML}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link TelegramNotifierConfiguration#telegramNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testTelegramNotifierConfigurationTelegramNotifier_thenReturnParseModeIsHtml() {
		// Arrange
		TelegramNotifierConfiguration telegramNotifierConfiguration = new TelegramNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		TelegramNotifier actualTelegramNotifierResult = telegramNotifierConfiguration.telegramNotifier(repository,
				proxyProperties);

		// Assert
		assertEquals("HTML", actualTelegramNotifierResult.getParseMode());
		assertEquals("https://api.telegram.org", actualTelegramNotifierResult.getApiUrl());
		assertNull(actualTelegramNotifierResult.getAuthToken());
		assertNull(actualTelegramNotifierResult.getChatId());
		assertFalse(actualTelegramNotifierResult.isDisableNotify());
		assertTrue(actualTelegramNotifierResult.isEnabled());
		assertArrayEquals(new String[] { "UNKNOWN:UP" }, actualTelegramNotifierResult.getIgnoreChanges());
	}

	/**
	 * Test WebexNotifierConfiguration
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <p>
	 * Method under test:
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testWebexNotifierConfigurationWebexNotifier() {
		// Arrange
		WebexNotifierConfiguration webexNotifierConfiguration = new WebexNotifierConfiguration();
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act and Assert
		Expression message = webexNotifierConfiguration.webexNotifier(repository, proxyProperties).getMessage();
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
	 * Test WebexNotifierConfiguration
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given {@code 8080}.</li>
	 * <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testWebexNotifierConfigurationWebexNotifier_given8080_thenCallsAddEntryListener() {
		// Arrange
		WebexNotifierConfiguration webexNotifierConfiguration = new WebexNotifierConfiguration();
		IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
		when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(
				new HazelcastEventStore(eventLogs));

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(8080);
		proxyProperties.setUsername("janedoe");

		// Act
		WebexNotifier actualWebexNotifierResult = webexNotifierConfiguration.webexNotifier(repository, proxyProperties);

		// Assert
		verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
		Expression message = actualWebexNotifierResult.getMessage();
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
	 * Test WebexNotifierConfiguration
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}.
	 * <ul>
	 * <li>Given minus one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link WebexNotifierConfiguration#webexNotifier(InstanceRepository, NotifierProxyProperties)}
	 */
	@Test
	public void testWebexNotifierConfigurationWebexNotifier_givenMinusOne() {
		// Arrange
		WebexNotifierConfiguration webexNotifierConfiguration = new WebexNotifierConfiguration();
		IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
		when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());
		EventsourcingInstanceRepository repository = new EventsourcingInstanceRepository(
				new HazelcastEventStore(eventLogs));

		NotifierProxyProperties proxyProperties = new NotifierProxyProperties();
		proxyProperties.setHost("localhost");
		proxyProperties.setPassword("iloveyou");
		proxyProperties.setPort(-1);
		proxyProperties.setUsername("janedoe");

		// Act
		WebexNotifier actualWebexNotifierResult = webexNotifierConfiguration.webexNotifier(repository, proxyProperties);

		// Assert
		verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
		Expression message = actualWebexNotifierResult.getMessage();
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

}
