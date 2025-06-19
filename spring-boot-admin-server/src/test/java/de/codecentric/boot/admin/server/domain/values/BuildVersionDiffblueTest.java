package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class BuildVersionDiffblueTest {
  /**
   * Test {@link BuildVersion#valueOf(String)}.
   * <ul>
   *   <li>When {@code foo}.</li>
   *   <li>Then return Value is {@code foo}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#valueOf(String)}
   */
  @Test
  public void testValueOf_whenFoo_thenReturnValueIsFoo() {
    // Arrange and Act
    BuildVersion actualValueOfResult = BuildVersion.valueOf("foo");

    // Assert
    assertEquals("foo", actualValueOfResult.getValue());
    assertEquals("foo", actualValueOfResult.toString());
  }

  /**
   * Test {@link BuildVersion#valueOf(String)}.
   * <ul>
   *   <li>When {@code null}.</li>
   *   <li>Then return Value is {@link StatusInfo#STATUS_UNKNOWN}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#valueOf(String)}
   */
  @Test
  public void testValueOf_whenNull_thenReturnValueIsStatus_unknown() {
    // Arrange and Act
    BuildVersion actualValueOfResult = BuildVersion.valueOf(null);

    // Assert
    assertEquals(StatusInfo.STATUS_UNKNOWN, actualValueOfResult.getValue());
    assertEquals(StatusInfo.STATUS_UNKNOWN, actualValueOfResult.toString());
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>Given {@code foo}.</li>
   *   <li>When {@link HashMap#HashMap()} {@code foo} is {@code 42}.</li>
   *   <li>Then return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_givenFoo_whenHashMapFooIs42_thenReturnNull() {
    // Arrange
    HashMap<String, Object> map = new HashMap<>();
    map.put("foo", "42");

    // Act and Assert
    assertNull(BuildVersion.from(map));
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>Given {@link HashMap#HashMap()} {@code version} is {@code not blank}.</li>
   *   <li>When {@link HashMap#HashMap()} {@code build.version} is {@code not blank}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_givenHashMapVersionIsNotBlank_whenHashMapBuildVersionIsNotBlank() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<>();
    objectObjectMap.put("version", "not blank");

    HashMap<String, Object> map = new HashMap<>();
    map.put("build", objectObjectMap);
    map.put("build.version", "not blank");
    map.put("version", "not blank");

    // Act
    BuildVersion actualFromResult = BuildVersion.from(map);

    // Assert
    assertEquals("not blank", actualFromResult.getValue());
    assertEquals("not blank", actualFromResult.toString());
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>Given {@code null}.</li>
   *   <li>When {@link HashMap#HashMap()} {@code build.version} is {@code null}.</li>
   *   <li>Then return Value is {@code not blank}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_givenNull_whenHashMapBuildVersionIsNull_thenReturnValueIsNotBlank() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<>();
    objectObjectMap.put("version", null);

    HashMap<String, Object> map = new HashMap<>();
    map.put("build", objectObjectMap);
    map.put("build.version", null);
    map.put("version", "not blank");

    // Act
    BuildVersion actualFromResult = BuildVersion.from(map);

    // Assert
    assertEquals("not blank", actualFromResult.getValue());
    assertEquals("not blank", actualFromResult.toString());
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>Given {@code null}.</li>
   *   <li>When {@link HashMap#HashMap()} {@code version} is {@code null}.</li>
   *   <li>Then return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_givenNull_whenHashMapVersionIsNull_thenReturnNull() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<>();
    objectObjectMap.put("version", null);

    HashMap<String, Object> map = new HashMap<>();
    map.put("build", objectObjectMap);
    map.put("build.version", null);
    map.put("version", null);

    // Act and Assert
    assertNull(BuildVersion.from(map));
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>Given space.</li>
   *   <li>When {@link HashMap#HashMap()} {@code version} is space.</li>
   *   <li>Then return Value is {@link StatusInfo#STATUS_UNKNOWN}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_givenSpace_whenHashMapVersionIsSpace_thenReturnValueIsStatus_unknown() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<>();
    objectObjectMap.put("version", null);

    HashMap<String, Object> map = new HashMap<>();
    map.put("build", objectObjectMap);
    map.put("build.version", null);
    map.put("version", " ");

    // Act
    BuildVersion actualFromResult = BuildVersion.from(map);

    // Assert
    assertEquals(StatusInfo.STATUS_UNKNOWN, actualFromResult.getValue());
    assertEquals(StatusInfo.STATUS_UNKNOWN, actualFromResult.toString());
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()} {@code build.version} is {@code not blank}.</li>
   *   <li>Then return Value is {@code not blank}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_whenHashMapBuildVersionIsNotBlank_thenReturnValueIsNotBlank() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<>();
    objectObjectMap.put("version", null);

    HashMap<String, Object> map = new HashMap<>();
    map.put("build", objectObjectMap);
    map.put("build.version", "not blank");
    map.put("version", "not blank");

    // Act
    BuildVersion actualFromResult = BuildVersion.from(map);

    // Assert
    assertEquals("not blank", actualFromResult.getValue());
    assertEquals("not blank", actualFromResult.toString());
  }

  /**
   * Test {@link BuildVersion#from(Map)}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()}.</li>
   *   <li>Then return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#from(Map)}
   */
  @Test
  public void testFrom_whenHashMap_thenReturnNull() {
    // Arrange, Act and Assert
    assertNull(BuildVersion.from(new HashMap<>()));
  }

  /**
   * Test getters and setters.
   * <p>
   * Methods under test:
   * <ul>
   *   <li>{@link BuildVersion#getValue()}
   *   <li>{@link BuildVersion#toString()}
   * </ul>
   */
  @Test
  public void testGettersAndSetters() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("foo");

    // Act
    String actualValue = valueOfResult.getValue();

    // Assert
    assertEquals("foo", actualValue);
    assertEquals("foo", valueOfResult.toString());
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Given valueOf {@code 42}.</li>
   *   <li>When valueOf {@code 42}.</li>
   *   <li>Then return zero.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_givenValueOf42_whenValueOf42_thenReturnZero() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("42");

    // Act and Assert
    assertEquals(0, valueOfResult.compareTo(BuildVersion.valueOf("42")));
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Given valueOf {@code 42}.</li>
   *   <li>When valueOf {@code foo}.</li>
   *   <li>Then return minus fifty.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_givenValueOf42_whenValueOfFoo_thenReturnMinusFifty() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("42");

    // Act and Assert
    assertEquals(-50, valueOfResult.compareTo(BuildVersion.valueOf("foo")));
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Given valueOf {@code build.version}.</li>
   *   <li>Then return one.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_givenValueOfBuildVersion_thenReturnOne() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("build.version");

    // Act and Assert
    assertEquals(1, valueOfResult.compareTo(BuildVersion.valueOf("build")));
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Given valueOf {@code build}.</li>
   *   <li>Then return minus one.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_givenValueOfBuild_thenReturnMinusOne() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("build");

    // Act and Assert
    assertEquals(-1, valueOfResult.compareTo(BuildVersion.valueOf("build.version")));
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Given valueOf {@code foo}.</li>
   *   <li>When valueOf {@code foo}.</li>
   *   <li>Then return zero.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_givenValueOfFoo_whenValueOfFoo_thenReturnZero() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("foo");

    // Act and Assert
    assertEquals(0, valueOfResult.compareTo(BuildVersion.valueOf("foo")));
  }

  /**
   * Test {@link BuildVersion#compareTo(BuildVersion)} with {@code BuildVersion}.
   * <ul>
   *   <li>Then return minus eleven.</li>
   * </ul>
   * <p>
   * Method under test: {@link BuildVersion#compareTo(BuildVersion)}
   */
  @Test
  public void testCompareToWithBuildVersion_thenReturnMinusEleven() {
    // Arrange
    BuildVersion valueOfResult = BuildVersion.valueOf("[.\\-+]");

    // Act and Assert
    assertEquals(-11, valueOfResult.compareTo(BuildVersion.valueOf("foo")));
  }
}
