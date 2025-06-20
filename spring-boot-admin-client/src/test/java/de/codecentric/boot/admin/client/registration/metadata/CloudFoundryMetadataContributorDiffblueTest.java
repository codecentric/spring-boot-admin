package de.codecentric.boot.admin.client.registration.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { CloudFoundryMetadataContributor.class, CloudFoundryApplicationProperties.class })
@ExtendWith(SpringExtension.class)
class CloudFoundryMetadataContributorDiffblueTest {

	@Autowired
	private CloudFoundryApplicationProperties cloudFoundryApplicationProperties;

	@Autowired
	private CloudFoundryMetadataContributor cloudFoundryMetadataContributor;

	/**
	 * Test
	 * {@link CloudFoundryMetadataContributor#CloudFoundryMetadataContributor(CloudFoundryApplicationProperties)}.
	 * <p>
	 * Method under test:
	 * {@link CloudFoundryMetadataContributor#CloudFoundryMetadataContributor(CloudFoundryApplicationProperties)}
	 */
	@Test
	@DisplayName("Test new CloudFoundryMetadataContributor(CloudFoundryApplicationProperties)")
	@Tag("MaintainedByDiffblue")
	void testNewCloudFoundryMetadataContributor() {
		// Arrange
		CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
		cfApplicationProperties.setApplicationId("42");
		cfApplicationProperties.setInstanceIndex("Instance Index");
		cfApplicationProperties.setUris(new ArrayList<>());

		// Act and Assert
		Map<String, String> metadata = new CloudFoundryMetadataContributor(cfApplicationProperties).getMetadata();
		assertEquals(2, metadata.size());
		assertEquals("42", metadata.get("applicationId"));
		assertEquals("Instance Index", metadata.get("instanceId"));
	}

	/**
	 * Test {@link CloudFoundryMetadataContributor#getMetadata()}.
	 * <ul>
	 * <li>Given {@link CloudFoundryApplicationProperties} (default constructor)
	 * ApplicationId is {@code not blank}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CloudFoundryMetadataContributor#getMetadata()}
	 */
	@Test
	@DisplayName("Test getMetadata(); given CloudFoundryApplicationProperties (default constructor) ApplicationId is 'not blank'")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenCloudFoundryApplicationPropertiesApplicationIdIsNotBlank() {
		// Arrange
		CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
		cfApplicationProperties.setUris(new ArrayList<>());
		cfApplicationProperties.setApplicationId("not blank");
		cfApplicationProperties.setInstanceIndex(null);

		// Act and Assert
		assertTrue(new CloudFoundryMetadataContributor(cfApplicationProperties).getMetadata().isEmpty());
	}

	/**
	 * Test {@link CloudFoundryMetadataContributor#getMetadata()}.
	 * <ul>
	 * <li>Given {@link CloudFoundryMetadataContributor}.</li>
	 * <li>Then return Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CloudFoundryMetadataContributor#getMetadata()}
	 */
	@Test
	@DisplayName("Test getMetadata(); given CloudFoundryMetadataContributor; then return Empty")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_givenCloudFoundryMetadataContributor_thenReturnEmpty() {
		// Arrange, Act and Assert
		assertTrue(cloudFoundryMetadataContributor.getMetadata().isEmpty());
	}

	/**
	 * Test {@link CloudFoundryMetadataContributor#getMetadata()}.
	 * <ul>
	 * <li>Then return size is two.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CloudFoundryMetadataContributor#getMetadata()}
	 */
	@Test
	@DisplayName("Test getMetadata(); then return size is two")
	@Tag("MaintainedByDiffblue")
	void testGetMetadata_thenReturnSizeIsTwo() {
		// Arrange
		CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
		cfApplicationProperties.setApplicationId("42");
		cfApplicationProperties.setInstanceIndex("Instance Index");
		cfApplicationProperties.setUris(new ArrayList<>());

		// Act
		Map<String, String> actualMetadata = new CloudFoundryMetadataContributor(cfApplicationProperties).getMetadata();

		// Assert
		assertEquals(2, actualMetadata.size());
		assertEquals("42", actualMetadata.get("applicationId"));
		assertEquals("Instance Index", actualMetadata.get("instanceId"));
	}

}
