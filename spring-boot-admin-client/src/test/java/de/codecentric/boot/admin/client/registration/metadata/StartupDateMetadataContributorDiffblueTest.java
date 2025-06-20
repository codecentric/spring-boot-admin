package de.codecentric.boot.admin.client.registration.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { StartupDateMetadataContributor.class })
@ExtendWith(SpringExtension.class)
class StartupDateMetadataContributorDiffblueTest {

	@Autowired
	private StartupDateMetadataContributor startupDateMetadataContributor;

	/**
	 * Test new {@link StartupDateMetadataContributor} (default constructor).
	 * <p>
	 * Method under test: default or parameterless constructor of
	 * {@link StartupDateMetadataContributor}
	 */
	@Test
	@DisplayName("Test new StartupDateMetadataContributor (default constructor)")
	@Tag("MaintainedByDiffblue")
	void testNewStartupDateMetadataContributor() {
		// Arrange, Act and Assert
		assertEquals(1, new StartupDateMetadataContributor().getMetadata().size());
	}

}
