package de.codecentric.boot.admin.server.cloud.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { EurekaServiceInstanceConverter.class })
@ExtendWith(SpringExtension.class)
class EurekaServiceInstanceConverterDiffblueTest {

	@Autowired
	private EurekaServiceInstanceConverter eurekaServiceInstanceConverter;

	/**
	 * Test {@link EurekaServiceInstanceConverter#getHealthUrl(ServiceInstance)}.
	 * <ul>
	 * <li>Then return toString is {@code https://localhost:8080/actuator/health}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link EurekaServiceInstanceConverter#getHealthUrl(ServiceInstance)}
	 */
	@Test
	@DisplayName("Test getHealthUrl(ServiceInstance); then return toString is 'https://localhost:8080/actuator/health'")
	@Tag("MaintainedByDiffblue")
	void testGetHealthUrl_thenReturnToStringIsHttpsLocalhost8080ActuatorHealth() {
		// Arrange, Act and Assert
		assertEquals("https://localhost:8080/actuator/health",
				eurekaServiceInstanceConverter
					.getHealthUrl(new DefaultServiceInstance("42", "42", "localhost", 8080, true))
					.toString());
	}

	/**
	 * Test new {@link EurekaServiceInstanceConverter} (default constructor).
	 * <p>
	 * Method under test: default or parameterless constructor of
	 * {@link EurekaServiceInstanceConverter}
	 */
	@Test
	@DisplayName("Test new EurekaServiceInstanceConverter (default constructor)")
	@Tag("MaintainedByDiffblue")
	void testNewEurekaServiceInstanceConverter() {
		// Arrange and Act
		EurekaServiceInstanceConverter actualEurekaServiceInstanceConverter = new EurekaServiceInstanceConverter();

		// Assert
		assertEquals("/actuator", actualEurekaServiceInstanceConverter.getManagementContextPath());
		assertEquals("health", actualEurekaServiceInstanceConverter.getHealthEndpointPath());
	}

}
