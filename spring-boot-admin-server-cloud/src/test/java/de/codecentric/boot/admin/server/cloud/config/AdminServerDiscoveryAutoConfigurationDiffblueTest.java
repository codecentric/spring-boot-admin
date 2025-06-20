package de.codecentric.boot.admin.server.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import de.codecentric.boot.admin.server.cloud.config.AdminServerDiscoveryAutoConfiguration.EurekaConverterConfiguration;
import de.codecentric.boot.admin.server.cloud.config.AdminServerDiscoveryAutoConfiguration.KubernetesConverterConfiguration;
import de.codecentric.boot.admin.server.cloud.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.server.cloud.discovery.KubernetesServiceInstanceConverter;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties.Metadata;

class AdminServerDiscoveryAutoConfigurationDiffblueTest {

	/**
	 * Test EurekaConverterConfiguration
	 * {@link EurekaConverterConfiguration#serviceInstanceConverter()}.
	 * <p>
	 * Method under test: {@link EurekaConverterConfiguration#serviceInstanceConverter()}
	 */
	@Test
	@DisplayName("Test EurekaConverterConfiguration serviceInstanceConverter()")
	@Tag("MaintainedByDiffblue")
	void testEurekaConverterConfigurationServiceInstanceConverter() {
		// Arrange and Act
		EurekaServiceInstanceConverter actualServiceInstanceConverterResult = new EurekaConverterConfiguration()
			.serviceInstanceConverter();

		// Assert
		assertEquals("/actuator", actualServiceInstanceConverterResult.getManagementContextPath());
		assertEquals("health", actualServiceInstanceConverterResult.getHealthEndpointPath());
	}

	/**
	 * Test KubernetesConverterConfiguration
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}.
	 * <p>
	 * Method under test:
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}
	 */
	@Test
	@DisplayName("Test KubernetesConverterConfiguration serviceInstanceConverter(KubernetesDiscoveryProperties)")
	@Tag("MaintainedByDiffblue")
	void testKubernetesConverterConfigurationServiceInstanceConverter() {
		// Arrange
		KubernetesConverterConfiguration kubernetesConverterConfiguration = new KubernetesConverterConfiguration();
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();

		// Act
		KubernetesServiceInstanceConverter actualServiceInstanceConverterResult = kubernetesConverterConfiguration
			.serviceInstanceConverter(new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true,
					"Filter", knownSecurePorts, serviceLabels, "Primary Port Name",
					new Metadata(true, "Labels Prefix", true, "Annotations Prefix", true, "Ports Prefix"), 1, true));

		// Assert
		assertEquals("/actuator", actualServiceInstanceConverterResult.getManagementContextPath());
		assertEquals("health", actualServiceInstanceConverterResult.getHealthEndpointPath());
	}

	/**
	 * Test KubernetesConverterConfiguration
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}.
	 * <p>
	 * Method under test:
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}
	 */
	@Test
	@DisplayName("Test KubernetesConverterConfiguration serviceInstanceConverter(KubernetesDiscoveryProperties)")
	@Tag("MaintainedByDiffblue")
	void testKubernetesConverterConfigurationServiceInstanceConverter2() {
		// Arrange
		KubernetesConverterConfiguration kubernetesConverterConfiguration = new KubernetesConverterConfiguration();
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();

		// Act
		KubernetesServiceInstanceConverter actualServiceInstanceConverterResult = kubernetesConverterConfiguration
			.serviceInstanceConverter(new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true,
					"Filter", knownSecurePorts, new HashMap<>(), "Primary Port Name", null, 1, true));

		// Assert
		assertEquals("/actuator", actualServiceInstanceConverterResult.getManagementContextPath());
		assertEquals("health", actualServiceInstanceConverterResult.getHealthEndpointPath());
	}

	/**
	 * Test KubernetesConverterConfiguration
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}.
	 * <p>
	 * Method under test:
	 * {@link KubernetesConverterConfiguration#serviceInstanceConverter(KubernetesDiscoveryProperties)}
	 */
	@Test
	@DisplayName("Test KubernetesConverterConfiguration serviceInstanceConverter(KubernetesDiscoveryProperties)")
	@Tag("MaintainedByDiffblue")
	void testKubernetesConverterConfigurationServiceInstanceConverter3() {
		// Arrange
		KubernetesConverterConfiguration kubernetesConverterConfiguration = new KubernetesConverterConfiguration();
		HashSet<String> namespaces = new HashSet<>();
		HashSet<Integer> knownSecurePorts = new HashSet<>();
		HashMap<String, String> serviceLabels = new HashMap<>();

		// Act
		KubernetesServiceInstanceConverter actualServiceInstanceConverterResult = kubernetesConverterConfiguration
			.serviceInstanceConverter(new KubernetesDiscoveryProperties(true, true, namespaces, true, 1L, true,
					"Filter", knownSecurePorts, serviceLabels, "Primary Port Name",
					new Metadata(true, "Labels Prefix", true, "Annotations Prefix", true, null), 1, true));

		// Assert
		assertEquals("/actuator", actualServiceInstanceConverterResult.getManagementContextPath());
		assertEquals("health", actualServiceInstanceConverterResult.getHealthEndpointPath());
	}

	/**
	 * Test {@link AdminServerDiscoveryAutoConfiguration#serviceInstanceConverter()}.
	 * <p>
	 * Method under test:
	 * {@link AdminServerDiscoveryAutoConfiguration#serviceInstanceConverter()}
	 */
	@Test
	@DisplayName("Test serviceInstanceConverter()")
	@Tag("MaintainedByDiffblue")
	void testServiceInstanceConverter() {
		// Arrange and Act
		DefaultServiceInstanceConverter actualServiceInstanceConverterResult = new AdminServerDiscoveryAutoConfiguration()
			.serviceInstanceConverter();

		// Assert
		assertEquals("/actuator", actualServiceInstanceConverterResult.getManagementContextPath());
		assertEquals("health", actualServiceInstanceConverterResult.getHealthEndpointPath());
	}

}
