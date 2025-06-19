package de.codecentric.boot.admin.client.registration.metadata;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class CompositeMetadataContributorDiffblueTest {
  /**
   * Test {@link CompositeMetadataContributor#CompositeMetadataContributor(List)}.
   * <p>
   * Method under test: {@link CompositeMetadataContributor#CompositeMetadataContributor(List)}
   */
  @Test
  @DisplayName("Test new CompositeMetadataContributor(List)")
  @Tag("MaintainedByDiffblue")
  void testNewCompositeMetadataContributor() {
    // Arrange, Act and Assert
    assertTrue(new CompositeMetadataContributor(new ArrayList<>()).getMetadata().isEmpty());
  }

  /**
   * Test {@link CompositeMetadataContributor#getMetadata()}.
   * <p>
   * Method under test: {@link CompositeMetadataContributor#getMetadata()}
   */
  @Test
  @DisplayName("Test getMetadata()")
  @Tag("MaintainedByDiffblue")
  void testGetMetadata() {
    // Arrange, Act and Assert
    assertTrue(new CompositeMetadataContributor(new ArrayList<>()).getMetadata().isEmpty());
  }

  /**
   * Test {@link CompositeMetadataContributor#getMetadata()}.
   * <ul>
   *   <li>Then calls {@link MetadataContributor#getMetadata()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CompositeMetadataContributor#getMetadata()}
   */
  @Test
  @DisplayName("Test getMetadata(); then calls getMetadata()")
  @Tag("MaintainedByDiffblue")
  void testGetMetadata_thenCallsGetMetadata() {
    // Arrange
    MetadataContributor metadataContributor = mock(MetadataContributor.class);
    when(metadataContributor.getMetadata()).thenReturn(new HashMap<>());

    ArrayList<MetadataContributor> delegates = new ArrayList<>();
    delegates.add(metadataContributor);

    // Act
    Map<String, String> actualMetadata = new CompositeMetadataContributor(delegates).getMetadata();

    // Assert
    verify(metadataContributor).getMetadata();
    assertTrue(actualMetadata.isEmpty());
  }

  /**
   * Test {@link CompositeMetadataContributor#getMetadata()}.
   * <ul>
   *   <li>Then calls {@link MetadataContributor#getMetadata()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link CompositeMetadataContributor#getMetadata()}
   */
  @Test
  @DisplayName("Test getMetadata(); then calls getMetadata()")
  @Tag("MaintainedByDiffblue")
  void testGetMetadata_thenCallsGetMetadata2() {
    // Arrange
    MetadataContributor metadataContributor = mock(MetadataContributor.class);
    when(metadataContributor.getMetadata()).thenReturn(new HashMap<>());
    MetadataContributor metadataContributor2 = mock(MetadataContributor.class);
    when(metadataContributor2.getMetadata()).thenReturn(new HashMap<>());

    ArrayList<MetadataContributor> delegates = new ArrayList<>();
    delegates.add(metadataContributor2);
    delegates.add(metadataContributor);

    // Act
    Map<String, String> actualMetadata = new CompositeMetadataContributor(delegates).getMetadata();

    // Assert
    verify(metadataContributor2).getMetadata();
    verify(metadataContributor).getMetadata();
    assertTrue(actualMetadata.isEmpty());
  }
}
