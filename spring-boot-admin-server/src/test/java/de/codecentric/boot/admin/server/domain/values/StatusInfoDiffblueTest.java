package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class StatusInfoDiffblueTest {
  /**
   * Test {@link StatusInfo#valueOf(String, Map)} with {@code statusCode}, {@code details}.
   * <ul>
   *   <li>When {@code not blank}.</li>
   *   <li>Then return Status is {@code NOT BLANK}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#valueOf(String, Map)}
   */
  @Test
  public void testValueOfWithStatusCodeDetails_whenNotBlank_thenReturnStatusIsNotBlank() {
    // Arrange and Act
    StatusInfo actualValueOfResult = StatusInfo.valueOf("not blank", null);

    // Assert
    assertEquals("NOT BLANK", actualValueOfResult.getStatus());
    assertFalse(actualValueOfResult.isDown());
    assertFalse(actualValueOfResult.isOffline());
    assertFalse(actualValueOfResult.isUnknown());
    assertFalse(actualValueOfResult.isUp());
    assertTrue(actualValueOfResult.getDetails().isEmpty());
  }

  /**
   * Test {@link StatusInfo#valueOf(String, Map)} with {@code statusCode}, {@code details}.
   * <ul>
   *   <li>When {@code Status Code}.</li>
   *   <li>Then return Status is {@code STATUS CODE}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#valueOf(String, Map)}
   */
  @Test
  public void testValueOfWithStatusCodeDetails_whenStatusCode_thenReturnStatusIsStatusCode() {
    // Arrange and Act
    StatusInfo actualValueOfResult = StatusInfo.valueOf("Status Code", new HashMap<>());

    // Assert
    assertEquals("STATUS CODE", actualValueOfResult.getStatus());
    assertFalse(actualValueOfResult.isDown());
    assertFalse(actualValueOfResult.isOffline());
    assertFalse(actualValueOfResult.isUnknown());
    assertFalse(actualValueOfResult.isUp());
    assertTrue(actualValueOfResult.getDetails().isEmpty());
  }

  /**
   * Test {@link StatusInfo#valueOf(String)} with {@code statusCode}.
   * <ul>
   *   <li>When {@code Status Code}.</li>
   *   <li>Then return Status is {@code STATUS CODE}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#valueOf(String)}
   */
  @Test
  public void testValueOfWithStatusCode_whenStatusCode_thenReturnStatusIsStatusCode() {
    // Arrange and Act
    StatusInfo actualValueOfResult = StatusInfo.valueOf("Status Code");

    // Assert
    assertEquals("STATUS CODE", actualValueOfResult.getStatus());
    assertFalse(actualValueOfResult.isDown());
    assertFalse(actualValueOfResult.isOffline());
    assertFalse(actualValueOfResult.isUnknown());
    assertFalse(actualValueOfResult.isUp());
    assertTrue(actualValueOfResult.getDetails().isEmpty());
  }

  /**
   * Test {@link StatusInfo#ofUnknown()}.
   * <p>
   * Method under test: {@link StatusInfo#ofUnknown()}
   */
  @Test
  public void testOfUnknown() {
    // Arrange and Act
    StatusInfo actualOfUnknownResult = StatusInfo.ofUnknown();

    // Assert
    assertFalse(actualOfUnknownResult.isDown());
    assertFalse(actualOfUnknownResult.isOffline());
    assertFalse(actualOfUnknownResult.isUp());
    assertTrue(actualOfUnknownResult.isUnknown());
    assertTrue(actualOfUnknownResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_UNKNOWN, actualOfUnknownResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofUp()}.
   * <p>
   * Method under test: {@link StatusInfo#ofUp()}
   */
  @Test
  public void testOfUp() {
    // Arrange and Act
    StatusInfo actualOfUpResult = StatusInfo.ofUp();

    // Assert
    assertFalse(actualOfUpResult.isDown());
    assertFalse(actualOfUpResult.isOffline());
    assertFalse(actualOfUpResult.isUnknown());
    assertTrue(actualOfUpResult.isUp());
    assertTrue(actualOfUpResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_UP, actualOfUpResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofUp(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofUp(Map)}
   */
  @Test
  public void testOfUpWithMap_whenHashMap() {
    // Arrange and Act
    StatusInfo actualOfUpResult = StatusInfo.ofUp(new HashMap<>());

    // Assert
    assertFalse(actualOfUpResult.isDown());
    assertFalse(actualOfUpResult.isOffline());
    assertFalse(actualOfUpResult.isUnknown());
    assertTrue(actualOfUpResult.isUp());
    assertTrue(actualOfUpResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_UP, actualOfUpResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofUp(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofUp(Map)}
   */
  @Test
  public void testOfUpWithMap_whenNull() {
    // Arrange and Act
    StatusInfo actualOfUpResult = StatusInfo.ofUp(null);

    // Assert
    assertFalse(actualOfUpResult.isDown());
    assertFalse(actualOfUpResult.isOffline());
    assertFalse(actualOfUpResult.isUnknown());
    assertTrue(actualOfUpResult.isUp());
    assertTrue(actualOfUpResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_UP, actualOfUpResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofDown()}.
   * <p>
   * Method under test: {@link StatusInfo#ofDown()}
   */
  @Test
  public void testOfDown() {
    // Arrange and Act
    StatusInfo actualOfDownResult = StatusInfo.ofDown();

    // Assert
    assertFalse(actualOfDownResult.isOffline());
    assertFalse(actualOfDownResult.isUnknown());
    assertFalse(actualOfDownResult.isUp());
    assertTrue(actualOfDownResult.isDown());
    assertTrue(actualOfDownResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_DOWN, actualOfDownResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofDown(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofDown(Map)}
   */
  @Test
  public void testOfDownWithMap_whenHashMap() {
    // Arrange and Act
    StatusInfo actualOfDownResult = StatusInfo.ofDown(new HashMap<>());

    // Assert
    assertFalse(actualOfDownResult.isOffline());
    assertFalse(actualOfDownResult.isUnknown());
    assertFalse(actualOfDownResult.isUp());
    assertTrue(actualOfDownResult.isDown());
    assertTrue(actualOfDownResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_DOWN, actualOfDownResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofDown(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofDown(Map)}
   */
  @Test
  public void testOfDownWithMap_whenNull() {
    // Arrange and Act
    StatusInfo actualOfDownResult = StatusInfo.ofDown(null);

    // Assert
    assertFalse(actualOfDownResult.isOffline());
    assertFalse(actualOfDownResult.isUnknown());
    assertFalse(actualOfDownResult.isUp());
    assertTrue(actualOfDownResult.isDown());
    assertTrue(actualOfDownResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_DOWN, actualOfDownResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofOffline()}.
   * <p>
   * Method under test: {@link StatusInfo#ofOffline()}
   */
  @Test
  public void testOfOffline() {
    // Arrange and Act
    StatusInfo actualOfOfflineResult = StatusInfo.ofOffline();

    // Assert
    assertFalse(actualOfOfflineResult.isDown());
    assertFalse(actualOfOfflineResult.isUnknown());
    assertFalse(actualOfOfflineResult.isUp());
    assertTrue(actualOfOfflineResult.isOffline());
    assertTrue(actualOfOfflineResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_OFFLINE, actualOfOfflineResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofOffline(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofOffline(Map)}
   */
  @Test
  public void testOfOfflineWithMap_whenHashMap() {
    // Arrange and Act
    StatusInfo actualOfOfflineResult = StatusInfo.ofOffline(new HashMap<>());

    // Assert
    assertFalse(actualOfOfflineResult.isDown());
    assertFalse(actualOfOfflineResult.isUnknown());
    assertFalse(actualOfOfflineResult.isUp());
    assertTrue(actualOfOfflineResult.isOffline());
    assertTrue(actualOfOfflineResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_OFFLINE, actualOfOfflineResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#ofOffline(Map)} with {@code Map}.
   * <ul>
   *   <li>When {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#ofOffline(Map)}
   */
  @Test
  public void testOfOfflineWithMap_whenNull() {
    // Arrange and Act
    StatusInfo actualOfOfflineResult = StatusInfo.ofOffline(null);

    // Assert
    assertFalse(actualOfOfflineResult.isDown());
    assertFalse(actualOfOfflineResult.isUnknown());
    assertFalse(actualOfOfflineResult.isUp());
    assertTrue(actualOfOfflineResult.isOffline());
    assertTrue(actualOfOfflineResult.getDetails().isEmpty());
    assertEquals(StatusInfo.STATUS_OFFLINE, actualOfOfflineResult.getStatus());
  }

  /**
   * Test {@link StatusInfo#getDetails()}.
   * <p>
   * Method under test: {@link StatusInfo#getDetails()}
   */
  @Test
  public void testGetDetails() {
    // Arrange, Act and Assert
    assertTrue(StatusInfo.valueOf("Status Code", new HashMap<>()).getDetails().isEmpty());
  }

  /**
   * Test {@link StatusInfo#isUp()}.
   * <ul>
   *   <li>Given valueOf {@code Status Code} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isUp()}
   */
  @Test
  public void testIsUp_givenValueOfStatusCodeAndHashMap_thenReturnFalse() {
    // Arrange, Act and Assert
    assertFalse(StatusInfo.valueOf("Status Code", new HashMap<>()).isUp());
  }

  /**
   * Test {@link StatusInfo#isUp()}.
   * <ul>
   *   <li>Given valueOf {@link StatusInfo#STATUS_UP} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isUp()}
   */
  @Test
  public void testIsUp_givenValueOfStatus_upAndHashMap_thenReturnTrue() {
    // Arrange, Act and Assert
    assertTrue(StatusInfo.valueOf(StatusInfo.STATUS_UP, new HashMap<>()).isUp());
  }

  /**
   * Test {@link StatusInfo#isOffline()}.
   * <ul>
   *   <li>Given valueOf {@code Status Code} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isOffline()}
   */
  @Test
  public void testIsOffline_givenValueOfStatusCodeAndHashMap_thenReturnFalse() {
    // Arrange, Act and Assert
    assertFalse(StatusInfo.valueOf("Status Code", new HashMap<>()).isOffline());
  }

  /**
   * Test {@link StatusInfo#isOffline()}.
   * <ul>
   *   <li>Given valueOf {@link StatusInfo#STATUS_OFFLINE} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isOffline()}
   */
  @Test
  public void testIsOffline_givenValueOfStatus_offlineAndHashMap_thenReturnTrue() {
    // Arrange, Act and Assert
    assertTrue(StatusInfo.valueOf(StatusInfo.STATUS_OFFLINE, new HashMap<>()).isOffline());
  }

  /**
   * Test {@link StatusInfo#isDown()}.
   * <ul>
   *   <li>Given valueOf {@code Status Code} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isDown()}
   */
  @Test
  public void testIsDown_givenValueOfStatusCodeAndHashMap_thenReturnFalse() {
    // Arrange, Act and Assert
    assertFalse(StatusInfo.valueOf("Status Code", new HashMap<>()).isDown());
  }

  /**
   * Test {@link StatusInfo#isDown()}.
   * <ul>
   *   <li>Given valueOf {@link StatusInfo#STATUS_DOWN} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isDown()}
   */
  @Test
  public void testIsDown_givenValueOfStatus_downAndHashMap_thenReturnTrue() {
    // Arrange, Act and Assert
    assertTrue(StatusInfo.valueOf(StatusInfo.STATUS_DOWN, new HashMap<>()).isDown());
  }

  /**
   * Test {@link StatusInfo#isUnknown()}.
   * <ul>
   *   <li>Given valueOf {@code Status Code} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isUnknown()}
   */
  @Test
  public void testIsUnknown_givenValueOfStatusCodeAndHashMap_thenReturnFalse() {
    // Arrange, Act and Assert
    assertFalse(StatusInfo.valueOf("Status Code", new HashMap<>()).isUnknown());
  }

  /**
   * Test {@link StatusInfo#isUnknown()}.
   * <ul>
   *   <li>Given valueOf {@link StatusInfo#STATUS_UNKNOWN} and {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code true}.</li>
   * </ul>
   * <p>
   * Method under test: {@link StatusInfo#isUnknown()}
   */
  @Test
  public void testIsUnknown_givenValueOfStatus_unknownAndHashMap_thenReturnTrue() {
    // Arrange, Act and Assert
    assertTrue(StatusInfo.valueOf(StatusInfo.STATUS_UNKNOWN, new HashMap<>()).isUnknown());
  }

  /**
   * Test {@link StatusInfo#severity()}.
   * <p>
   * Method under test: {@link StatusInfo#severity()}
   */
  @Test
  public void testSeverity() {
    // Arrange and Act
    Comparator<String> actualSeverityResult = StatusInfo.severity();

    // Assert
    assertEquals(0, actualSeverityResult.compare("foo", "foo"));
  }
}
