package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertNull;
import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {ServletApplicationFactory.class, InstanceProperties.class,
    ManagementServerProperties.class, ServerProperties.class, WebEndpointProperties.class, DispatcherServletPath.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class ServletApplicationFactoryDiffblueTest {
  @MockitoBean
  private DispatcherServletPath dispatcherServletPath;

  @Autowired
  private InstanceProperties instanceProperties;

  @Autowired
  private ManagementServerProperties managementServerProperties;

  @MockitoBean
  private MetadataContributor metadataContributor;

  @MockitoBean
  private PathMappedEndpoints pathMappedEndpoints;

  @Autowired
  private ServerProperties serverProperties;

  @Autowired
  private ServletApplicationFactory servletApplicationFactory;

  @MockitoBean
  private ServletContext servletContext;

  @Autowired
  private WebEndpointProperties webEndpointProperties;

  /**
   * Test {@link ServletApplicationFactory#getDispatcherServletPrefix()}.
   * <ul>
   *   <li>Then return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link ServletApplicationFactory#getDispatcherServletPrefix()}
   */
  @Test
  @DisplayName("Test getDispatcherServletPrefix(); then return 'null'")
  @Tag("MaintainedByDiffblue")
  void testGetDispatcherServletPrefix_thenReturnNull() {
    // Arrange, Act and Assert
    assertNull(servletApplicationFactory.getDispatcherServletPrefix());
  }
}
