package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.config.ServiceHostType;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties.ForwardHeadersStrategy;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.Ssl.ClientAuth;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.unit.DataSize;

@ContextConfiguration(classes = { DefaultApplicationFactory.class, InstanceProperties.class,
		ManagementServerProperties.class, ServerProperties.class, WebEndpointProperties.class })
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class DefaultApplicationFactoryDiffblueTest {

	@Autowired
	private DefaultApplicationFactory defaultApplicationFactory;

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
	private WebEndpointProperties webEndpointProperties;

	/**
	 * Test {@link DefaultApplicationFactory#createApplication()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#createApplication()}
	 */
	@Test
	@DisplayName("Test createApplication(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testCreateApplication_thenThrowIllegalStateException() {
		// Arrange, Act and Assert
		assertThrows(IllegalStateException.class, () -> defaultApplicationFactory.createApplication());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getName()}.
	 * <ul>
	 * <li>Given {@link WebEndpointProperties} (default constructor) BasePath is empty
	 * string.</li>
	 * <li>Then return {@code Name}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getName()}
	 */
	@Test
	@DisplayName("Test getName(); given WebEndpointProperties (default constructor) BasePath is empty string; then return 'Name'")
	@Tag("MaintainedByDiffblue")
	void testGetName_givenWebEndpointPropertiesBasePathIsEmptyString_thenReturnName() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("Name",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getName());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceUrl()}
	 */
	@Test
	@DisplayName("Test getServiceUrl(); then return 'https://example.org/example'")
	@Tag("MaintainedByDiffblue")
	void testGetServiceUrl_thenReturnHttpsExampleOrgExample() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/exampleService%20Path}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceUrl()}
	 */
	@Test
	@DisplayName("Test getServiceUrl(); then return 'https://example.org/exampleService%20Path'")
	@Tag("MaintainedByDiffblue")
	void testGetServiceUrl_thenReturnHttpsExampleOrgExampleService20Path() {
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
		instance.setServiceUrl(null);

		Ssl ssl = new Ssl();
		ssl.setBundle("Bundle");
		ssl.setCertificate("Certificate");
		ssl.setCertificatePrivateKey("Certificate Private Key");
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/exampleService%20Path",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceBaseUrl()}.
	 * <ul>
	 * <li>Given {@link InstanceProperties} (default constructor) PreferIp is
	 * {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceBaseUrl()}
	 */
	@Test
	@DisplayName("Test getServiceBaseUrl(); given InstanceProperties (default constructor) PreferIp is 'false'")
	@Tag("MaintainedByDiffblue")
	void testGetServiceBaseUrl_givenInstancePropertiesPreferIpIsFalse() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl("https://example.org/example");
		instance.setManagementUrl("https://example.org/example");
		instance.setMetadata(new HashMap<>());
		instance.setName("Name");
		instance.setPreferIp(false);
		instance.setServiceBaseUrl("");
		instance.setServiceHostType(ServiceHostType.IP);
		instance.setServicePath("Service Path");
		instance.setServiceUrl("https://example.org/example");

		Ssl ssl = new Ssl();
		ssl.setBundle("Bundle");
		ssl.setCertificate("Certificate");
		ssl.setCertificatePrivateKey("Certificate Private Key");
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("42 Main St");

		Ssl ssl2 = new Ssl();
		ssl2.setBundle("Bundle");
		ssl2.setCertificate("Certificate");
		ssl2.setCertificatePrivateKey("Certificate Private Key");
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		server.setAddress(address);
		server.setForwardHeadersStrategy(ForwardHeadersStrategy.NATIVE);
		server.setMaxHttpRequestHeaderSize(DataSize.ofBytes(1L));
		server.setPort(8080);
		server.setServerHeader("Server Header");
		server.setShutdown(Shutdown.GRACEFUL);
		server.setSsl(ssl2);

		WebEndpointProperties webEndpoint = new WebEndpointProperties();
		webEndpoint.setBasePath("");

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceBaseUrl());
		verify(address).getHostAddress();
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceBaseUrl()}.
	 * <ul>
	 * <li>Given {@link Ssl} (default constructor) Enabled is {@code false}.</li>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceBaseUrl()}
	 */
	@Test
	@DisplayName("Test getServiceBaseUrl(); given Ssl (default constructor) Enabled is 'false'; then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetServiceBaseUrl_givenSslEnabledIsFalse_thenThrowIllegalStateException() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl("https://example.org/example");
		instance.setManagementUrl("https://example.org/example");
		instance.setMetadata(new HashMap<>());
		instance.setName("Name");
		instance.setPreferIp(true);
		instance.setServiceBaseUrl("");
		instance.setServiceHostType(ServiceHostType.IP);
		instance.setServicePath("Service Path");
		instance.setServiceUrl("https://example.org/example");

		Ssl ssl = new Ssl();
		ssl.setBundle("Bundle");
		ssl.setCertificate("Certificate");
		ssl.setCertificatePrivateKey("Certificate Private Key");
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("42 Main St");

		Ssl ssl2 = new Ssl();
		ssl2.setBundle("Bundle");
		ssl2.setCertificate("Certificate");
		ssl2.setCertificatePrivateKey("Certificate Private Key");
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(false);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		server.setAddress(address);
		server.setForwardHeadersStrategy(ForwardHeadersStrategy.NATIVE);
		server.setMaxHttpRequestHeaderSize(DataSize.ofBytes(1L));
		server.setPort(8080);
		server.setServerHeader("Server Header");
		server.setShutdown(Shutdown.GRACEFUL);
		server.setSsl(ssl2);

		WebEndpointProperties webEndpoint = new WebEndpointProperties();
		webEndpoint.setBasePath("");

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceBaseUrl());
		verify(address).getHostAddress();
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceBaseUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceBaseUrl()}
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceBaseUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getServiceBaseUrl()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getServiceBaseUrl()}
	 */
	@Test
	@DisplayName("Test getServiceBaseUrl(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetServiceBaseUrl_thenThrowIllegalStateException() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl("https://example.org/example");
		instance.setManagementUrl("https://example.org/example");
		instance.setMetadata(new HashMap<>());
		instance.setName("Name");
		instance.setPreferIp(true);
		instance.setServiceBaseUrl("");
		instance.setServiceHostType(ServiceHostType.IP);
		instance.setServicePath("Service Path");
		instance.setServiceUrl("https://example.org/example");

		Ssl ssl = new Ssl();
		ssl.setBundle("Bundle");
		ssl.setCertificate("Certificate");
		ssl.setCertificatePrivateKey("Certificate Private Key");
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("42 Main St");

		Ssl ssl2 = new Ssl();
		ssl2.setBundle("Bundle");
		ssl2.setCertificate("Certificate");
		ssl2.setCertificatePrivateKey("Certificate Private Key");
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		server.setAddress(address);
		server.setForwardHeadersStrategy(ForwardHeadersStrategy.NATIVE);
		server.setMaxHttpRequestHeaderSize(DataSize.ofBytes(1L));
		server.setPort(8080);
		server.setServerHeader("Server Header");
		server.setShutdown(Shutdown.GRACEFUL);
		server.setSsl(ssl2);

		WebEndpointProperties webEndpoint = new WebEndpointProperties();
		webEndpoint.setBasePath("");

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getServiceBaseUrl());
		verify(address).getHostAddress();
	}

	/**
	 * Test {@link DefaultApplicationFactory#getManagementUrl()}.
	 * <ul>
	 * <li>Given {@link InstanceProperties} (default constructor) ManagementBaseUrl is
	 * {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getManagementUrl()}
	 */
	@Test
	@DisplayName("Test getManagementUrl(); given InstanceProperties (default constructor) ManagementBaseUrl is 'null'")
	@Tag("MaintainedByDiffblue")
	void testGetManagementUrl_givenInstancePropertiesManagementBaseUrlIsNull() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl(null);
		instance.setManagementUrl(null);
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example/",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getManagementUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getManagementUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getManagementUrl()}
	 */
	@Test
	@DisplayName("Test getManagementUrl(); then return 'https://example.org/example'")
	@Tag("MaintainedByDiffblue")
	void testGetManagementUrl_thenReturnHttpsExampleOrgExample() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getManagementUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getManagementUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example/}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getManagementUrl()}
	 */
	@Test
	@DisplayName("Test getManagementUrl(); then return 'https://example.org/example/'")
	@Tag("MaintainedByDiffblue")
	void testGetManagementUrl_thenReturnHttpsExampleOrgExample2() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl("https://example.org/example");
		instance.setManagementUrl(null);
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example/",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getManagementUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getManagementBaseUrl()}.
	 * <ul>
	 * <li>Given {@link InstanceProperties} (default constructor) ManagementBaseUrl is
	 * empty string.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getManagementBaseUrl()}
	 */
	@Test
	@DisplayName("Test getManagementBaseUrl(); given InstanceProperties (default constructor) ManagementBaseUrl is empty string")
	@Tag("MaintainedByDiffblue")
	void testGetManagementBaseUrl_givenInstancePropertiesManagementBaseUrlIsEmptyString() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl("https://example.org/example");
		instance.setManagementBaseUrl("");
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getManagementBaseUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getManagementBaseUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getManagementBaseUrl()}
	 */
	@Test
	@DisplayName("Test getManagementBaseUrl(); then return 'https://example.org/example'")
	@Tag("MaintainedByDiffblue")
	void testGetManagementBaseUrl_thenReturnHttpsExampleOrgExample() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getManagementBaseUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#isManagementPortEqual()}.
	 * <ul>
	 * <li>Then return {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#isManagementPortEqual()}
	 */
	@Test
	@DisplayName("Test isManagementPortEqual(); then return 'true'")
	@Tag("MaintainedByDiffblue")
	void testIsManagementPortEqual_thenReturnTrue() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertTrue(new DefaultApplicationFactory(instance, management, server,
				new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint, mock(MetadataContributor.class))
			.isManagementPortEqual());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getEndpointsWebPath()}.
	 * <ul>
	 * <li>Then return empty string.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getEndpointsWebPath()}
	 */
	@Test
	@DisplayName("Test getEndpointsWebPath(); then return empty string")
	@Tag("MaintainedByDiffblue")
	void testGetEndpointsWebPath_thenReturnEmptyString() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getEndpointsWebPath());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getHealthUrl()}.
	 * <ul>
	 * <li>Given {@link InstanceProperties} (default constructor) ManagementBaseUrl is
	 * {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getHealthUrl()}
	 */
	@Test
	@DisplayName("Test getHealthUrl(); given InstanceProperties (default constructor) ManagementBaseUrl is 'null'")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl_givenInstancePropertiesManagementBaseUrlIsNull() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl(null);
		instance.setManagementBaseUrl(null);
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getHealthUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getHealthUrl()}.
	 * <ul>
	 * <li>Then return {@code https://example.org/example}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getHealthUrl()}
	 */
	@Test
	@DisplayName("Test getHealthUrl(); then return 'https://example.org/example'")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl_thenReturnHttpsExampleOrgExample() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertEquals("https://example.org/example",
				new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getHealthUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getHealthUrl()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getHealthUrl()}
	 */
	@Test
	@DisplayName("Test getHealthUrl(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl_thenThrowIllegalStateException() {
		// Arrange
		InstanceProperties instance = new InstanceProperties();
		instance.setHealthUrl(null);
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getHealthUrl());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getMetadata()}.
	 * <ul>
	 * <li>Given {@link MetadataContributor} {@link MetadataContributor#getMetadata()}
	 * return {@link HashMap#HashMap()}.</li>
	 * <li>Then return Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getMetadata()}
	 */
	@Test
	@DisplayName("Test getMetadata(); given MetadataContributor getMetadata() return HashMap(); then return Empty")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenMetadataContributorGetMetadataReturnHashMap_thenReturnEmpty() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		MetadataContributor metadataContributor = mock(MetadataContributor.class);
		when(metadataContributor.getMetadata()).thenReturn(new HashMap<>());

		// Act
		Map<String, String> actualMetadata = new DefaultApplicationFactory(instance, management, server,
				new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint, metadataContributor)
			.getMetadata();

		// Assert
		verify(metadataContributor).getMetadata();
		assertTrue(actualMetadata.isEmpty());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getMetadata()}.
	 * <ul>
	 * <li>Then throw {@link IllegalArgumentException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getMetadata()}
	 */
	@Test
	@DisplayName("Test getMetadata(); then throw IllegalArgumentException")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_thenThrowIllegalArgumentException() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		MetadataContributor metadataContributor = mock(MetadataContributor.class);
		when(metadataContributor.getMetadata()).thenThrow(new IllegalArgumentException("foo"));

		// Act and Assert
		assertThrows(IllegalArgumentException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint, metadataContributor)
					.getMetadata());
		verify(metadataContributor).getMetadata();
	}

	/**
	 * Test {@link DefaultApplicationFactory#getLocalServerPort()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getLocalServerPort()}
	 */
	@Test
	@DisplayName("Test getLocalServerPort(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetLocalServerPort_thenThrowIllegalStateException() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getLocalServerPort());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getLocalManagementPort()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getLocalManagementPort()}
	 */
	@Test
	@DisplayName("Test getLocalManagementPort(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetLocalManagementPort_thenThrowIllegalStateException() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getLocalManagementPort());
	}

	/**
	 * Test {@link DefaultApplicationFactory#getHealthEndpointPath()}.
	 * <ul>
	 * <li>Then throw {@link IllegalStateException}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link DefaultApplicationFactory#getHealthEndpointPath()}
	 */
	@Test
	@DisplayName("Test getHealthEndpointPath(); then throw IllegalStateException")
	@Tag("MaintainedByDiffblue")
	void testGetHealthEndpointPath_thenThrowIllegalStateException() {
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
		ssl.setCiphers(new String[] { "Ciphers" });
		ssl.setClientAuth(ClientAuth.NONE);
		ssl.setEnabled(true);
		ssl.setEnabledProtocols(new String[] { "Enabled Protocols" });
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
		ssl2.setCiphers(new String[] { "Ciphers" });
		ssl2.setClientAuth(ClientAuth.NONE);
		ssl2.setEnabled(true);
		ssl2.setEnabledProtocols(new String[] { "Enabled Protocols" });
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

		// Act and Assert
		assertThrows(IllegalStateException.class,
				() -> new DefaultApplicationFactory(instance, management, server,
						new PathMappedEndpoints("Base Path", new ArrayList<>()), webEndpoint,
						mock(MetadataContributor.class))
					.getHealthEndpointPath());
	}

}
