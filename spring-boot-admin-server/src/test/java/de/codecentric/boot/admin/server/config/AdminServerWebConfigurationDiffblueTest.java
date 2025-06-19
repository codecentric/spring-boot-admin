package de.codecentric.boot.admin.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ReactiveRestApiConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfiguration;
import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.utils.jackson.AdminServerModule;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient.Builder;
import de.codecentric.boot.admin.server.web.reactive.AdminControllerHandlerMapping;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = {AdminServerWebConfiguration.class, AdminServerProperties.class, InstanceRegistry.class,
    ApplicationRegistry.class, InstanceEventPublisher.class})
@DisabledInAotMode
@RunWith(MockitoJUnitRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AdminServerWebConfigurationDiffblueTest {
  @Autowired
  private AdminServerProperties adminServerProperties;

  @Autowired
  private AdminServerWebConfiguration adminServerWebConfiguration;

  @MockitoBean
  private InstanceEventStore instanceEventStore;

  @MockitoBean
  private InstanceFilter instanceFilter;

  @MockitoBean
  private InstanceIdGenerator instanceIdGenerator;

  @MockitoBean
  private InstanceRepository instanceRepository;

  /**
   * Test {@link AdminServerWebConfiguration#adminJacksonModule()}.
   * <p>
   * Method under test: {@link AdminServerWebConfiguration#adminJacksonModule()}
   */
  @Test
  public void testAdminJacksonModule() {
    // Arrange and Act
    SimpleModule actualAdminJacksonModuleResult = adminServerWebConfiguration.adminJacksonModule();

    // Assert
    assertTrue(actualAdminJacksonModuleResult instanceof AdminServerModule);
    Iterable<? extends Module> dependencies = actualAdminJacksonModuleResult.getDependencies();
    assertTrue(dependencies instanceof List);
    Version versionResult = actualAdminJacksonModuleResult.version();
    assertEquals("", versionResult.getArtifactId());
    assertEquals("", versionResult.getGroupId());
    assertEquals("//0.0.0", versionResult.toFullString());
    assertEquals("de.codecentric.boot.admin.server.utils.jackson.AdminServerModule",
        actualAdminJacksonModuleResult.getModuleName());
    assertEquals("de.codecentric.boot.admin.server.utils.jackson.AdminServerModule",
        actualAdminJacksonModuleResult.getTypeId());
    assertEquals(0, versionResult.getMajorVersion());
    assertEquals(0, versionResult.getMinorVersion());
    assertEquals(0, versionResult.getPatchLevel());
    assertFalse(versionResult.isSnapshot());
    assertTrue(versionResult.isUknownVersion());
    assertTrue(versionResult.isUnknownVersion());
    assertTrue(((List<? extends Module>) dependencies).isEmpty());
  }

  /**
   * Test {@link AdminServerWebConfiguration#instancesController(InstanceRegistry, InstanceEventStore)}.
   * <p>
   * Method under test: {@link AdminServerWebConfiguration#instancesController(InstanceRegistry, InstanceEventStore)}
   */
  @Test
  public void testInstancesController() throws AssertionError {
    // Arrange
    InstanceRegistry instanceRegistry = new InstanceRegistry(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
        mock(InstanceFilter.class));

    // Act and Assert
    FirstStep<InstanceEvent> createResult = StepVerifier
        .create(adminServerWebConfiguration.instancesController(instanceRegistry, new InMemoryEventStore()).events());
    createResult.expectComplete().verify();
  }

  /**
   * Test {@link AdminServerWebConfiguration#applicationsController(ApplicationRegistry, ApplicationEventPublisher)}.
   * <p>
   * Method under test: {@link AdminServerWebConfiguration#applicationsController(ApplicationRegistry, ApplicationEventPublisher)}
   */
  @Test
  public void testApplicationsController() throws AssertionError {
    // Arrange, Act and Assert
    FirstStep<Application> createResult = StepVerifier.create(
        adminServerWebConfiguration
            .applicationsController(
                new ApplicationRegistry(
                    new InstanceRegistry(new EventsourcingInstanceRepository(new InMemoryEventStore()),
                        mock(InstanceIdGenerator.class), mock(InstanceFilter.class)),
                    mock(InstanceEventPublisher.class)),
                mock(ApplicationEventPublisher.class))
            .applications());
    createResult.expectComplete().verify();
  }

  /**
   * Test ReactiveRestApiConfiguration {@link ReactiveRestApiConfiguration#adminHandlerMapping(RequestedContentTypeResolver)}.
   * <p>
   * Method under test: {@link ReactiveRestApiConfiguration#adminHandlerMapping(RequestedContentTypeResolver)}
   */
  @Test
  public void testReactiveRestApiConfigurationAdminHandlerMapping() throws IllegalStateException {
    // Arrange
    RequestedContentTypeResolver webFluxContentTypeResolver = mock(RequestedContentTypeResolver.class);

    // Act
    RequestMappingHandlerMapping actualAdminHandlerMappingResult = new ReactiveRestApiConfiguration(
        new AdminServerProperties()).adminHandlerMapping(webFluxContentTypeResolver);

    // Assert
    assertTrue(actualAdminHandlerMappingResult instanceof AdminControllerHandlerMapping);
    assertTrue(actualAdminHandlerMappingResult.getCorsProcessor() instanceof DefaultCorsProcessor);
    PathPatternParser pathPatternParser = actualAdminHandlerMappingResult.getPathPatternParser();
    assertEquals('/', pathPatternParser.getPathOptions().separator());
    assertNull(actualAdminHandlerMappingResult.getApplicationContext());
    assertEquals(0, actualAdminHandlerMappingResult.getOrder());
    assertFalse(pathPatternParser.isMatchOptionalTrailingSeparator());
    assertTrue(actualAdminHandlerMappingResult.getHandlerMethods().isEmpty());
    assertTrue(actualAdminHandlerMappingResult.getPathPrefixes().isEmpty());
    assertTrue(pathPatternParser.isCaseSensitive());
    assertSame(webFluxContentTypeResolver, actualAdminHandlerMappingResult.getContentTypeResolver());
  }

  /**
   * Test ReactiveRestApiConfiguration {@link ReactiveRestApiConfiguration#instancesProxyController(InstanceRegistry, Builder)}.
   * <ul>
   *   <li>Then calls {@link Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ReactiveRestApiConfiguration#instancesProxyController(InstanceRegistry, Builder)}
   */
  @Test
  public void testReactiveRestApiConfigurationInstancesProxyController_thenCallsBuild() {
    // Arrange
    ReactiveRestApiConfiguration reactiveRestApiConfiguration = new ReactiveRestApiConfiguration(
        new AdminServerProperties());
    InstanceRegistry instanceRegistry = new InstanceRegistry(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
        mock(InstanceFilter.class));

    WebClient.Builder builder = mock(WebClient.Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
    Builder instanceWebClientBuilder = mock(Builder.class);
    when(instanceWebClientBuilder.build()).thenReturn(buildResult);

    // Act
    reactiveRestApiConfiguration.instancesProxyController(instanceRegistry, instanceWebClientBuilder);

    // Assert
    verify(instanceWebClientBuilder).build();
    verify(builder).build();
  }

  /**
   * Test ServletRestApiConfiguration {@link ServletRestApiConfiguration#adminHandlerMapping(ContentNegotiationManager)}.
   * <p>
   * Method under test: {@link ServletRestApiConfiguration#adminHandlerMapping(ContentNegotiationManager)}
   */
  @Test
  public void testServletRestApiConfigurationAdminHandlerMapping() {
    // Arrange
    ServletRestApiConfiguration servletRestApiConfiguration = new ServletRestApiConfiguration(
        new AdminServerProperties());
    ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    // Act
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping actualAdminHandlerMappingResult = servletRestApiConfiguration
        .adminHandlerMapping(contentNegotiationManager);

    // Assert
    assertTrue(
        actualAdminHandlerMappingResult instanceof de.codecentric.boot.admin.server.web.servlet.AdminControllerHandlerMapping);
    assertTrue(actualAdminHandlerMappingResult.getPathMatcher() instanceof AntPathMatcher);
    assertTrue(actualAdminHandlerMappingResult
        .getCorsProcessor() instanceof org.springframework.web.cors.DefaultCorsProcessor);
    assertTrue(actualAdminHandlerMappingResult
        .getNamingStrategy() instanceof RequestMappingInfoHandlerMethodMappingNamingStrategy);
    assertNull(actualAdminHandlerMappingResult.getDefaultHandler());
    assertNull(actualAdminHandlerMappingResult.getFileExtensions());
    assertNull(actualAdminHandlerMappingResult.getCorsConfigurationSource());
    assertNull(actualAdminHandlerMappingResult.getAdaptedInterceptors());
    assertEquals(0, actualAdminHandlerMappingResult.getOrder());
    assertFalse(actualAdminHandlerMappingResult.useRegisteredSuffixPatternMatch());
    assertFalse(actualAdminHandlerMappingResult.useSuffixPatternMatch());
    assertFalse(actualAdminHandlerMappingResult.useTrailingSlashMatch());
    assertTrue(actualAdminHandlerMappingResult.getHandlerMethods().isEmpty());
    assertTrue(actualAdminHandlerMappingResult.getPathPrefixes().isEmpty());
    assertSame(contentNegotiationManager, actualAdminHandlerMappingResult.getContentNegotiationManager());
  }

  /**
   * Test ServletRestApiConfiguration {@link ServletRestApiConfiguration#instancesProxyController(InstanceRegistry, Builder)}.
   * <ul>
   *   <li>Then calls {@link Builder#build()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ServletRestApiConfiguration#instancesProxyController(InstanceRegistry, Builder)}
   */
  @Test
  public void testServletRestApiConfigurationInstancesProxyController_thenCallsBuild() {
    // Arrange
    ServletRestApiConfiguration servletRestApiConfiguration = new ServletRestApiConfiguration(
        new AdminServerProperties());
    InstanceRegistry instanceRegistry = new InstanceRegistry(
        new EventsourcingInstanceRepository(new InMemoryEventStore()), mock(InstanceIdGenerator.class),
        mock(InstanceFilter.class));

    WebClient.Builder builder = mock(WebClient.Builder.class);
    when(builder.build()).thenReturn(mock(WebClient.class));
    InstanceWebClient buildResult = InstanceWebClient.builder().webClient(builder).build();
    Builder instanceWebClientBuilder = mock(Builder.class);
    when(instanceWebClientBuilder.build()).thenReturn(buildResult);

    // Act
    servletRestApiConfiguration.instancesProxyController(instanceRegistry, instanceWebClientBuilder);

    // Assert
    verify(instanceWebClientBuilder).build();
    verify(builder).build();
  }
}
