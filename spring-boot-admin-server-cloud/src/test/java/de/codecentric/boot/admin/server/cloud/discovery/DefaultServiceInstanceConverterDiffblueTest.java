package de.codecentric.boot.admin.server.cloud.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.codecentric.boot.admin.server.domain.values.Registration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties.Metadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { DefaultServiceInstanceConverter.class })
@ExtendWith(SpringExtension.class)
class DefaultServiceInstanceConverterDiffblueTest {

	@Autowired
	private DefaultServiceInstanceConverter defaultServiceInstanceConverter;

	/**
	 * Test
	 * {@link DefaultServiceInstanceConverter#getMetadataValue(ServiceInstance, String[])}.
	 * <ul>
	 * <li>Then return {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadataValue(ServiceInstance, String[])}
	 */
	@Test
	@DisplayName("Test getMetadataValue(ServiceInstance, String[]); then return 'null'")
	@Tag("MaintainedByDiffblue")
	void testGetMetadataValue_thenReturnNull() {
		// Arrange, Act and Assert
		assertNull(DefaultServiceInstanceConverter
			.getMetadataValue(new DefaultServiceInstance("42", "42", "localhost", 8080, true), "Keys"));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#convert(ServiceInstance)}.
	 * <p>
	 * Method under test: {@link DefaultServiceInstanceConverter#convert(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test convert(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testConvert() {
		// Arrange and Act
		Registration actualConvertResult = defaultServiceInstanceConverter
			.convert(new DefaultServiceInstance("42", "42", "localhost", 8080, true));

		// Assert
		assertEquals("42", actualConvertResult.getName());
		assertEquals("https://localhost:8080", actualConvertResult.getServiceUrl());
		assertEquals("https://localhost:8080/actuator", actualConvertResult.getManagementUrl());
		assertEquals("https://localhost:8080/actuator/health", actualConvertResult.getHealthUrl());
		assertNull(actualConvertResult.getSource());
		assertTrue(actualConvertResult.getMetadata().isEmpty());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#convert(ServiceInstance)}.
	 * <p>
	 * Method under test: {@link DefaultServiceInstanceConverter#convert(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test convert(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testConvert2() {
		// Arrange
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();
		KubernetesServiceInstanceConverter kubernetesServiceInstanceConverter = new KubernetesServiceInstanceConverter(
				new KubernetesDiscoveryProperties(true, true, namespaces, true, 3L, true,
						"Converting service '{}' running at '{}' with metadata {}", knownSecurePorts, serviceLabels,
						"Converting service '{}' running at '{}' with metadata {}",
						new Metadata(true, "Converting service '{}' running at '{}' with metadata {}", true,
								"Converting service '{}' running at '{}' with metadata {}", true,
								"Converting service '{}' running at '{}' with metadata {}"),
						3, true));

		// Act
		Registration actualConvertResult = kubernetesServiceInstanceConverter.convert(new DefaultServiceInstance(
				"Converting service '{}' running at '{}' with metadata {}", "42", "localhost", 8080, true));

		// Assert
		assertEquals("42", actualConvertResult.getName());
		assertEquals("https://localhost:8080", actualConvertResult.getServiceUrl());
		assertEquals("https://localhost:8080/actuator", actualConvertResult.getManagementUrl());
		assertEquals("https://localhost:8080/actuator/health", actualConvertResult.getHealthUrl());
		assertNull(actualConvertResult.getSource());
		assertTrue(actualConvertResult.getMetadata().isEmpty());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getHealthUrl(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getHealthUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getHealthUrl(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl() {
		// Arrange, Act and Assert
		assertEquals("https://localhost:8080/actuator/health",
				defaultServiceInstanceConverter
					.getHealthUrl(new DefaultServiceInstance("42", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getHealthUrl(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getHealthUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getHealthUrl(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl2() {
		// Arrange
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();
		KubernetesServiceInstanceConverter kubernetesServiceInstanceConverter = new KubernetesServiceInstanceConverter(
				new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true, "Filter", knownSecurePorts,
						serviceLabels, "Primary Port Name",
						new Metadata(true, "Labels Prefix", true, "Annotations Prefix", true, "Ports Prefix"), 1,
						true));

		// Act and Assert
		assertEquals("https://localhost:8080/actuator/health",
				kubernetesServiceInstanceConverter
					.getHealthUrl(new DefaultServiceInstance("localhost", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getHealthPath(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getHealthPath(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getHealthPath(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetHealthPath() {
		// Arrange, Act and Assert
		assertEquals("health", defaultServiceInstanceConverter
			.getHealthPath(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementUrl(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementUrl(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetManagementUrl() {
		// Arrange, Act and Assert
		assertEquals("https://localhost:8080/actuator",
				defaultServiceInstanceConverter
					.getManagementUrl(new DefaultServiceInstance("42", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementUrl(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementUrl(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetManagementUrl2() {
		// Arrange
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();
		KubernetesServiceInstanceConverter kubernetesServiceInstanceConverter = new KubernetesServiceInstanceConverter(
				new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true, "Filter", knownSecurePorts,
						serviceLabels, "Primary Port Name",
						new Metadata(true, "Labels Prefix", true, "Annotations Prefix", true, "Ports Prefix"), 1,
						true));

		// Act and Assert
		assertEquals("https://localhost:8080/actuator",
				kubernetesServiceInstanceConverter
					.getManagementUrl(new DefaultServiceInstance("localhost", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementHost(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementHost(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementHost(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetManagementHost() {
		// Arrange, Act and Assert
		assertEquals("localhost", defaultServiceInstanceConverter
			.getManagementHost(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementPort(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementPort(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementPort(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetManagementPort() {
		// Arrange
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();
		KubernetesServiceInstanceConverter kubernetesServiceInstanceConverter = new KubernetesServiceInstanceConverter(
				new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true, "Filter", knownSecurePorts,
						serviceLabels, "Primary Port Name",
						new Metadata(true, "Labels Prefix", true, "Annotations Prefix", true, "Ports Prefix"), 1,
						true));

		// Act and Assert
		assertEquals(8080, kubernetesServiceInstanceConverter
			.getManagementPort(new DefaultServiceInstance("Instance Id", "42", "localhost", 8080, true)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementPort(ServiceInstance)}.
	 * <ul>
	 * <li>Given {@link DefaultServiceInstanceConverter}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementPort(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementPort(ServiceInstance); given DefaultServiceInstanceConverter")
	@Tag("MaintainedByDiffblue")
	void testGetManagementPort_givenDefaultServiceInstanceConverter() {
		// Arrange, Act and Assert
		assertEquals(8080, defaultServiceInstanceConverter
			.getManagementPort(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getManagementPath(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getManagementPath(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getManagementPath(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetManagementPath() {
		// Arrange, Act and Assert
		assertEquals("/actuator", defaultServiceInstanceConverter
			.getManagementPath(new DefaultServiceInstance("42", "42", "localhost", 8080, true)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getServiceUrl(ServiceInstance)}.
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getServiceUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getServiceUrl(ServiceInstance)")
	@Tag("MaintainedByDiffblue")
	void testGetServiceUrl() {
		// Arrange, Act and Assert
		assertEquals("https://localhost:8080",
				defaultServiceInstanceConverter
					.getServiceUrl(new DefaultServiceInstance("42", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}.
	 * <ul>
	 * <li>Given {@code 42}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code 42} is {@code 42}.</li>
	 * <li>Then return {@link HashMap#HashMap()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getMetadata(ServiceInstance); given '42'; when HashMap() '42' is '42'; then return HashMap()")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_given42_whenHashMap42Is42_thenReturnHashMap() {
		// Arrange
		DefaultServiceInstanceConverter defaultServiceInstanceConverter = new DefaultServiceInstanceConverter();

		HashMap<String, String> metadata = new HashMap<>();
		metadata.put("42", "42");
		metadata.put("foo", "foo");

		// Act and Assert
		assertEquals(metadata, defaultServiceInstanceConverter
			.getMetadata(new DefaultServiceInstance("42", "42", "localhost", 8080, true, metadata)));
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}.
	 * <ul>
	 * <li>Given {@link DefaultServiceInstanceConverter}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getMetadata(ServiceInstance); given DefaultServiceInstanceConverter")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenDefaultServiceInstanceConverter() {
		// Arrange, Act and Assert
		assertTrue(defaultServiceInstanceConverter
			.getMetadata(new DefaultServiceInstance("42", "42", "localhost", 8080, true))
			.isEmpty());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}.
	 * <ul>
	 * <li>Given {@code null}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code foo} is {@code null}.</li>
	 * <li>Then return Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getMetadata(ServiceInstance); given 'null'; when HashMap() 'foo' is 'null'; then return Empty")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenNull_whenHashMapFooIsNull_thenReturnEmpty() {
		// Arrange
		DefaultServiceInstanceConverter defaultServiceInstanceConverter = new DefaultServiceInstanceConverter();

		HashMap<String, String> metadata = new HashMap<>();
		metadata.put("foo", null);

		// Act and Assert
		assertTrue(defaultServiceInstanceConverter
			.getMetadata(new DefaultServiceInstance("42", "42", "localhost", 8080, true, metadata))
			.isEmpty());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}.
	 * <ul>
	 * <li>Given {@code null}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code null} is {@code foo}.</li>
	 * <li>Then return Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getMetadata(ServiceInstance); given 'null'; when HashMap() 'null' is 'foo'; then return Empty")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenNull_whenHashMapNullIsFoo_thenReturnEmpty() {
		// Arrange
		DefaultServiceInstanceConverter defaultServiceInstanceConverter = new DefaultServiceInstanceConverter();

		HashMap<String, String> metadata = new HashMap<>();
		metadata.put(null, "foo");

		// Act and Assert
		assertTrue(defaultServiceInstanceConverter
			.getMetadata(new DefaultServiceInstance("42", "42", "localhost", 8080, true, metadata))
			.isEmpty());
	}

	/**
	 * Test {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}.
	 * <ul>
	 * <li>When {@link HashMap#HashMap()} {@code foo} is {@code foo}.</li>
	 * <li>Then return size is one.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link DefaultServiceInstanceConverter#getMetadata(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getMetadata(ServiceInstance); when HashMap() 'foo' is 'foo'; then return size is one")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_whenHashMapFooIsFoo_thenReturnSizeIsOne() {
		// Arrange
		DefaultServiceInstanceConverter defaultServiceInstanceConverter = new DefaultServiceInstanceConverter();

		HashMap<String, String> metadata = new HashMap<>();
		metadata.put("foo", "foo");

		// Act
		Map<String, String> actualMetadata = defaultServiceInstanceConverter
			.getMetadata(new DefaultServiceInstance("42", "42", "localhost", 8080, true, metadata));

		// Assert
		assertEquals(1, actualMetadata.size());
		assertEquals("foo", actualMetadata.get("foo"));
	}

	/**
	 * Test getters and setters.
	 * <p>
	 * Methods under test:
	 * <ul>
	 * <li>default or parameterless constructor of {@link DefaultServiceInstanceConverter}
	 * <li>{@link DefaultServiceInstanceConverter#setHealthEndpointPath(String)}
	 * <li>{@link DefaultServiceInstanceConverter#setManagementContextPath(String)}
	 * <li>{@link DefaultServiceInstanceConverter#getHealthEndpointPath()}
	 * <li>{@link DefaultServiceInstanceConverter#getManagementContextPath()}
	 * </ul>
	 */
	@Test
	@DisplayName("Test getters and setters")
	@Tag("MaintainedByDiffblue")
	void testGettersAndSetters() {
		// Arrange and Act
		DefaultServiceInstanceConverter actualDefaultServiceInstanceConverter = new DefaultServiceInstanceConverter();
		actualDefaultServiceInstanceConverter.setHealthEndpointPath("https://config.us-east-2.amazonaws.com");
		actualDefaultServiceInstanceConverter.setManagementContextPath("Management Context Path");
		String actualHealthEndpointPath = actualDefaultServiceInstanceConverter.getHealthEndpointPath();

		// Assert
		assertEquals("Management Context Path", actualDefaultServiceInstanceConverter.getManagementContextPath());
		assertEquals("https://config.us-east-2.amazonaws.com", actualHealthEndpointPath);
	}

}
