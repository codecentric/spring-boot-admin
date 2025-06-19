package de.codecentric.boot.admin.server.ui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.Cache;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.Palette;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class AdminServerUiPropertiesDiffblueTest {
  /**
   * Test Cache {@link Cache#toCacheControl()}.
   * <ul>
   *   <li>Given {@link Cache} (default constructor) MaxAge is {@code null}.</li>
   *   <li>Then return HeaderValue is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link Cache#toCacheControl()}
   */
  @Test
  @DisplayName("Test Cache toCacheControl(); given Cache (default constructor) MaxAge is 'null'; then return HeaderValue is 'null'")
  @Tag("MaintainedByDiffblue")
  void testCacheToCacheControl_givenCacheMaxAgeIsNull_thenReturnHeaderValueIsNull() {
    // Arrange
    Cache cache = new Cache();
    cache.setMaxAge(null);

    // Act and Assert
    assertNull(cache.toCacheControl().getHeaderValue());
  }

  /**
   * Test Cache {@link Cache#toCacheControl()}.
   * <ul>
   *   <li>Given {@link Cache} (default constructor) NoCache is {@code true}.</li>
   *   <li>Then return HeaderValue is {@code no-cache}.</li>
   * </ul>
   * <p>
   * Method under test: {@link Cache#toCacheControl()}
   */
  @Test
  @DisplayName("Test Cache toCacheControl(); given Cache (default constructor) NoCache is 'true'; then return HeaderValue is 'no-cache'")
  @Tag("MaintainedByDiffblue")
  void testCacheToCacheControl_givenCacheNoCacheIsTrue_thenReturnHeaderValueIsNoCache() {
    // Arrange
    Cache cache = new Cache();
    cache.setNoCache(true);

    // Act and Assert
    assertEquals("no-cache", cache.toCacheControl().getHeaderValue());
  }

  /**
   * Test Cache {@link Cache#toCacheControl()}.
   * <ul>
   *   <li>Given {@link Cache} (default constructor) NoStore is {@code true}.</li>
   *   <li>Then return HeaderValue is {@code no-store}.</li>
   * </ul>
   * <p>
   * Method under test: {@link Cache#toCacheControl()}
   */
  @Test
  @DisplayName("Test Cache toCacheControl(); given Cache (default constructor) NoStore is 'true'; then return HeaderValue is 'no-store'")
  @Tag("MaintainedByDiffblue")
  void testCacheToCacheControl_givenCacheNoStoreIsTrue_thenReturnHeaderValueIsNoStore() {
    // Arrange
    Cache cache = new Cache();
    cache.setNoStore(true);

    // Act and Assert
    assertEquals("no-store", cache.toCacheControl().getHeaderValue());
  }

  /**
   * Test Cache {@link Cache#toCacheControl()}.
   * <ul>
   *   <li>Given {@link Cache} (default constructor).</li>
   *   <li>Then return HeaderValue is {@code max-age=3600}.</li>
   * </ul>
   * <p>
   * Method under test: {@link Cache#toCacheControl()}
   */
  @Test
  @DisplayName("Test Cache toCacheControl(); given Cache (default constructor); then return HeaderValue is 'max-age=3600'")
  @Tag("MaintainedByDiffblue")
  void testCacheToCacheControl_givenCache_thenReturnHeaderValueIsMaxAge3600() {
    // Arrange, Act and Assert
    assertEquals("max-age=3600", new Cache().toCacheControl().getHeaderValue());
  }

  /**
   * Test Palette getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>default or parameterless constructor of {@link Palette}
   *   <li>{@link Palette#set100(String)}
   *   <li>{@link Palette#set200(String)}
   *   <li>{@link Palette#set300(String)}
   *   <li>{@link Palette#set400(String)}
   *   <li>{@link Palette#set500(String)}
   *   <li>{@link Palette#set50(String)}
   *   <li>{@link Palette#set600(String)}
   *   <li>{@link Palette#set700(String)}
   *   <li>{@link Palette#set800(String)}
   *   <li>{@link Palette#set900(String)}
   * </ul>
   */
  @Test
  @DisplayName("Test Palette getters and setters")
  @Tag("MaintainedByDiffblue")
  void testPaletteGettersAndSetters() {
    // Arrange and Act
    Palette actualPalette = new Palette();
    actualPalette.set100("Shade100");
    actualPalette.set200("Shade200");
    actualPalette.set300("Shade300");
    actualPalette.set400("Shade400");
    actualPalette.set500("Shade500");
    actualPalette.set50("Shade50");
    actualPalette.set600("Shade600");
    actualPalette.set700("Shade700");
    actualPalette.set800("Shade800");
    actualPalette.set900("Shade900");

    // Assert
    assertEquals("Shade100", actualPalette.getShade100());
    assertEquals("Shade200", actualPalette.getShade200());
    assertEquals("Shade300", actualPalette.getShade300());
    assertEquals("Shade400", actualPalette.getShade400());
    assertEquals("Shade50", actualPalette.getShade50());
    assertEquals("Shade500", actualPalette.getShade500());
    assertEquals("Shade600", actualPalette.getShade600());
    assertEquals("Shade700", actualPalette.getShade700());
    assertEquals("Shade800", actualPalette.getShade800());
    assertEquals("Shade900", actualPalette.getShade900());
  }
}
