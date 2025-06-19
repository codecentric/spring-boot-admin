package de.codecentric.boot.admin.server.ui.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {UiExtensions.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class UiExtensionsDiffblueTest {
  @Autowired
  private List<UiExtension> list;

  @MockitoBean
  private UiExtension uiExtension;

  @Autowired
  private UiExtensions uiExtensions;

  /**
   * Test {@link UiExtensions#UiExtensions(List)}.
   * <ul>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   *   <li>Then return CssExtensions Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#UiExtensions(List)}
   */
  @Test
  @DisplayName("Test new UiExtensions(List); when ArrayList(); then return CssExtensions Empty")
  @Tag("MaintainedByDiffblue")
  void testNewUiExtensions_whenArrayList_thenReturnCssExtensionsEmpty() {
    // Arrange and Act
    UiExtensions actualUiExtensions = new UiExtensions(new ArrayList<>());

    // Assert
    assertFalse(actualUiExtensions.iterator().hasNext());
    assertTrue(actualUiExtensions.getCssExtensions().isEmpty());
    assertTrue(actualUiExtensions.getExtensions().isEmpty());
    assertTrue(actualUiExtensions.getJsExtensions().isEmpty());
  }

  /**
   * Test {@link UiExtensions#iterator()}.
   * <p>
   * Method under test: {@link UiExtensions#iterator()}
   */
  @Test
  @DisplayName("Test iterator()")
  @Tag("MaintainedByDiffblue")
  void testIterator() {
    // Arrange, Act and Assert
    assertFalse(UiExtensions.EMPTY.iterator().hasNext());
  }

  /**
   * Test {@link UiExtensions#getCssExtensions()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link UiExtension}.</li>
   *   <li>Then return Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getCssExtensions()}
   */
  @Test
  @DisplayName("Test getCssExtensions(); given ArrayList() add UiExtension; then return Empty")
  @Tag("MaintainedByDiffblue")
  void testGetCssExtensions_givenArrayListAddUiExtension_thenReturnEmpty() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn("Resource Path");

    ArrayList<UiExtension> extensions = new ArrayList<>();
    extensions.add(uiExtension);
    extensions.add(uiExtension);

    // Act
    List<UiExtension> actualCssExtensions = new UiExtensions(extensions).getCssExtensions();

    // Assert
    verify(uiExtension, atLeast(1)).getResourcePath();
    assertTrue(actualCssExtensions.isEmpty());
  }

  /**
   * Test {@link UiExtensions#getCssExtensions()}.
   * <ul>
   *   <li>Given {@link UiExtension} {@link UiExtension#getResourcePath()} return {@code Resource Path}.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getCssExtensions()}
   */
  @Test
  @DisplayName("Test getCssExtensions(); given UiExtension getResourcePath() return 'Resource Path'")
  @Tag("MaintainedByDiffblue")
  void testGetCssExtensions_givenUiExtensionGetResourcePathReturnResourcePath() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn("Resource Path");

    // Act
    List<UiExtension> actualCssExtensions = uiExtensions.getCssExtensions();

    // Assert
    verify(uiExtension).getResourcePath();
    assertTrue(actualCssExtensions.isEmpty());
  }

  /**
   * Test {@link UiExtensions#getCssExtensions()}.
   * <ul>
   *   <li>Given {@link UiExtension}.</li>
   *   <li>Then return Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getCssExtensions()}
   */
  @Test
  @DisplayName("Test getCssExtensions(); given UiExtension; then return Empty")
  @Tag("MaintainedByDiffblue")
  void testGetCssExtensions_givenUiExtension_thenReturnEmpty() {
    // Arrange, Act and Assert
    assertTrue(UiExtensions.EMPTY.getCssExtensions().isEmpty());
  }

  /**
   * Test {@link UiExtensions#getCssExtensions()}.
   * <ul>
   *   <li>Then return size is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getCssExtensions()}
   */
  @Test
  @DisplayName("Test getCssExtensions(); then return size is one")
  @Tag("MaintainedByDiffblue")
  void testGetCssExtensions_thenReturnSizeIsOne() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn(".css");

    // Act
    List<UiExtension> actualCssExtensions = uiExtensions.getCssExtensions();

    // Assert
    verify(uiExtension).getResourcePath();
    assertEquals(1, actualCssExtensions.size());
  }

  /**
   * Test {@link UiExtensions#getJsExtensions()}.
   * <ul>
   *   <li>Given {@link ArrayList#ArrayList()} add {@link UiExtension}.</li>
   *   <li>Then return Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getJsExtensions()}
   */
  @Test
  @DisplayName("Test getJsExtensions(); given ArrayList() add UiExtension; then return Empty")
  @Tag("MaintainedByDiffblue")
  void testGetJsExtensions_givenArrayListAddUiExtension_thenReturnEmpty() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn("Resource Path");

    ArrayList<UiExtension> extensions = new ArrayList<>();
    extensions.add(uiExtension);
    extensions.add(uiExtension);

    // Act
    List<UiExtension> actualJsExtensions = new UiExtensions(extensions).getJsExtensions();

    // Assert
    verify(uiExtension, atLeast(1)).getResourcePath();
    assertTrue(actualJsExtensions.isEmpty());
  }

  /**
   * Test {@link UiExtensions#getJsExtensions()}.
   * <ul>
   *   <li>Given {@link UiExtension} {@link UiExtension#getResourcePath()} return {@code .js}.</li>
   *   <li>Then return size is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getJsExtensions()}
   */
  @Test
  @DisplayName("Test getJsExtensions(); given UiExtension getResourcePath() return '.js'; then return size is one")
  @Tag("MaintainedByDiffblue")
  void testGetJsExtensions_givenUiExtensionGetResourcePathReturnJs_thenReturnSizeIsOne() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn(".js");

    // Act
    List<UiExtension> actualJsExtensions = uiExtensions.getJsExtensions();

    // Assert
    verify(uiExtension).getResourcePath();
    assertEquals(1, actualJsExtensions.size());
  }

  /**
   * Test {@link UiExtensions#getJsExtensions()}.
   * <ul>
   *   <li>Given {@link UiExtension} {@link UiExtension#getResourcePath()} return {@code Resource Path}.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getJsExtensions()}
   */
  @Test
  @DisplayName("Test getJsExtensions(); given UiExtension getResourcePath() return 'Resource Path'")
  @Tag("MaintainedByDiffblue")
  void testGetJsExtensions_givenUiExtensionGetResourcePathReturnResourcePath() {
    // Arrange
    when(uiExtension.getResourcePath()).thenReturn("Resource Path");

    // Act
    List<UiExtension> actualJsExtensions = uiExtensions.getJsExtensions();

    // Assert
    verify(uiExtension).getResourcePath();
    assertTrue(actualJsExtensions.isEmpty());
  }

  /**
   * Test {@link UiExtensions#getJsExtensions()}.
   * <ul>
   *   <li>Given {@link UiExtension}.</li>
   *   <li>Then return Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link UiExtensions#getJsExtensions()}
   */
  @Test
  @DisplayName("Test getJsExtensions(); given UiExtension; then return Empty")
  @Tag("MaintainedByDiffblue")
  void testGetJsExtensions_givenUiExtension_thenReturnEmpty() {
    // Arrange, Act and Assert
    assertTrue(UiExtensions.EMPTY.getJsExtensions().isEmpty());
  }
}
