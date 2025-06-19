package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;
import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.config.ServiceHostType;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties.ForwardHeadersStrategy;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
import org.springframework.util.unit.DataSize;

class CloudFoundryApplicationFactoryDiffblueTest {
  /**
   * Test {@link CloudFoundryApplicationFactory#getServiceBaseUrl()}.
   * <ul>
   *   <li>Then return {@code https://example.org/example}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CloudFoundryApplicationFactory#getServiceBaseUrl()}
   */
  @Test
  @DisplayName("Test getServiceBaseUrl(); then return 'https://example.org/example'")
  @Tag("MaintainedByDiffblue")
  void testGetServiceBaseUrl_thenReturnHttpsExampleOrgExample() {
    // Arrange
    InstanceProperties instance = new InstanceProperties();
    instance.setHealthUrl("https://example.org/example");
    instance.setManagementBaseUrl("https://example.org/example");
    instance.setManagementUrl("https://example.org/example");
    instance.setMetadata(new HashMap<>());
    instance.setName("Name");
    instance.setPreferIp(true);
    instance.setServiceBaseUrl("https://example.org/example");
    instance.setServiceHostType(ServiceHostType.IP);
    instance.setServicePath("Service Path");
    instance.setServiceUrl("https://example.org/example");

    Ssl ssl = new Ssl();
    ssl.setBundle("Bundle");
    ssl.setCertificate("Certificate");
    ssl.setCertificatePrivateKey("Certificate Private Key");
    ssl.setCiphers(new String[]{"Ciphers"});
    ssl.setClientAuth(ClientAuth.NONE);
    ssl.setEnabled(true);
    ssl.setEnabledProtocols(new String[]{"Enabled Protocols"});
    ssl.setKeyAlias("Key Alias");
    ssl.setKeyPassword("iloveyou");
    ssl.setKeyStore("Key Store");
    ssl.setKeyStorePassword("iloveyou");
    ssl.setKeyStoreProvider("Key Store Provider");
    ssl.setKeyStoreType("Key Store Type");
    ssl.setProtocol("Protocol");
    ssl.setServerNameBundles(new ArrayList<>());
    ssl.setTrustCertificate("Trust Certificate");
    ssl.setTrustCertificatePrivateKey("Trust Certificate Private Key");
    ssl.setTrustStore("Trust Store");
    ssl.setTrustStorePassword("iloveyou");
    ssl.setTrustStoreProvider("Trust Store Provider");
    ssl.setTrustStoreType("Trust Store Type");

    ManagementServerProperties management = new ManagementServerProperties();
    management.setAddress(mock(InetAddress.class));
    management.setBasePath("Base Path");
    management.setPort(8080);
    management.setSsl(ssl);

    Ssl ssl2 = new Ssl();
    ssl2.setBundle("Bundle");
    ssl2.setCertificate("Certificate");
    ssl2.setCertificatePrivateKey("Certificate Private Key");
    ssl2.setCiphers(new String[]{"Ciphers"});
    ssl2.setClientAuth(ClientAuth.NONE);
    ssl2.setEnabled(true);
    ssl2.setEnabledProtocols(new String[]{"Enabled Protocols"});
    ssl2.setKeyAlias("Key Alias");
    ssl2.setKeyPassword("iloveyou");
    ssl2.setKeyStore("Key Store");
    ssl2.setKeyStorePassword("iloveyou");
    ssl2.setKeyStoreProvider("Key Store Provider");
    ssl2.setKeyStoreType("Key Store Type");
    ssl2.setProtocol("Protocol");
    ssl2.setServerNameBundles(new ArrayList<>());
    ssl2.setTrustCertificate("Trust Certificate");
    ssl2.setTrustCertificatePrivateKey("Trust Certificate Private Key");
    ssl2.setTrustStore("Trust Store");
    ssl2.setTrustStorePassword("iloveyou");
    ssl2.setTrustStoreProvider("Trust Store Provider");
    ssl2.setTrustStoreType("Trust Store Type");

    ServerProperties server = new ServerProperties();
    server.setAddress(mock(InetAddress.class));
    server.setForwardHeadersStrategy(ForwardHeadersStrategy.NATIVE);
    server.setMaxHttpRequestHeaderSize(DataSize.ofBytes(1L));
    server.setPort(8080);
    server.setServerHeader("Server Header");
    server.setShutdown(Shutdown.GRACEFUL);
    server.setSsl(ssl2);

    WebEndpointProperties webEndpoint = new WebEndpointProperties();
    webEndpoint.setBasePath("");

    CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
    cfApplicationProperties.setApplicationId("42");
    cfApplicationProperties.setInstanceIndex("Instance Index");
    cfApplicationProperties.setUris(new ArrayList<>());

    // Act and Assert
    assertEquals("https://example.org/example",
        new CloudFoundryApplicationFactory(instance, management, server,
            new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint, mock(MetadataContributor.class),
            cfApplicationProperties).getServiceBaseUrl());
  }
}
