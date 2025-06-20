package de.codecentric.boot.admin.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class SpringBootAdminClientCloudFoundryAutoConfigurationDiffblueTest {

	/**
	 * Test
	 * {@link SpringBootAdminClientCloudFoundryAutoConfiguration#cloudFoundryMetadataContributor(CloudFoundryApplicationProperties)}.
	 * <p>
	 * Method under test:
	 * {@link SpringBootAdminClientCloudFoundryAutoConfiguration#cloudFoundryMetadataContributor(CloudFoundryApplicationProperties)}
	 */
	@Test
	@DisplayName("Test cloudFoundryMetadataContributor(CloudFoundryApplicationProperties)")
	@Tag("MaintainedByDiffblue")
	void testCloudFoundryMetadataContributor() {
		// Arrange
		SpringBootAdminClientCloudFoundryAutoConfiguration springBootAdminClientCloudFoundryAutoConfiguration = new SpringBootAdminClientCloudFoundryAutoConfiguration();

		CloudFoundryApplicationProperties cloudFoundryApplicationProperties = new CloudFoundryApplicationProperties();
		cloudFoundryApplicationProperties.setApplicationId("42");
		cloudFoundryApplicationProperties.setInstanceIndex("Instance Index");
		cloudFoundryApplicationProperties.setUris(new ArrayList<>());

		// Act
		Map<String, String> actualMetadata = springBootAdminClientCloudFoundryAutoConfiguration
			.cloudFoundryMetadataContributor(cloudFoundryApplicationProperties)
			.getMetadata();

		// Assert
		assertEquals(2, actualMetadata.size());
		assertEquals("42", actualMetadata.get("applicationId"));
		assertEquals("Instance Index", actualMetadata.get("instanceId"));
	}

}
