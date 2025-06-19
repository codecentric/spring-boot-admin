package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class InfoMixinDiffblueTest {
  /**
   * Test {@link InfoMixin#from(Map)}.
   * <ul>
   *   <li>Given {@code foo}.</li>
   *   <li>When {@link HashMap#HashMap()} {@code foo} is {@code 42}.</li>
   *   <li>Then return Values is {@link HashMap#HashMap()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoMixin#from(Map)}
   */
  @Test
  public void testFrom_givenFoo_whenHashMapFooIs42_thenReturnValuesIsHashMap() {
    // Arrange
    HashMap<String, Object> values = new HashMap<>();
    values.put("foo", "42");

    // Act and Assert
    assertEquals(values, InfoMixin.from(values).getValues());
  }

  /**
   * Test {@link InfoMixin#from(Map)}.
   * <ul>
   *   <li>When {@link HashMap#HashMap()}.</li>
   *   <li>Then return Values Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoMixin#from(Map)}
   */
  @Test
  public void testFrom_whenHashMap_thenReturnValuesEmpty() {
    // Arrange, Act and Assert
    assertTrue(InfoMixin.from(new HashMap<>()).getValues().isEmpty());
  }

  /**
   * Test {@link InfoMixin#from(Map)}.
   * <ul>
   *   <li>When {@code null}.</li>
   *   <li>Then return Values Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link InfoMixin#from(Map)}
   */
  @Test
  public void testFrom_whenNull_thenReturnValuesEmpty() {
    // Arrange, Act and Assert
    assertTrue(InfoMixin.from(null).getValues().isEmpty());
  }
}
