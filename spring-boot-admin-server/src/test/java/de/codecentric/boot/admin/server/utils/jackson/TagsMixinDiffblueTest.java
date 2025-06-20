package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TagsMixinDiffblueTest {

	/**
	 * Test {@link TagsMixin#from(Map)}.
	 * <ul>
	 * <li>Given {@code 42}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code 42} is {@code 42}.</li>
	 * <li>Then return Values is {@link HashMap#HashMap()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link TagsMixin#from(Map)}
	 */
	@Test
	public void testFrom_given42_whenHashMap42Is42_thenReturnValuesIsHashMap() {
		// Arrange
		HashMap<String, Object> map = new HashMap<>();
		map.put("42", "42");
		map.put("foo", "42");

		// Act and Assert
		assertEquals(map, TagsMixin.from(map).getValues());
	}

	/**
	 * Test {@link TagsMixin#from(Map)}.
	 * <ul>
	 * <li>Given {@code foo}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code foo} is {@code 42}.</li>
	 * <li>Then return Values size is one.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link TagsMixin#from(Map)}
	 */
	@Test
	public void testFrom_givenFoo_whenHashMapFooIs42_thenReturnValuesSizeIsOne() {
		// Arrange
		HashMap<String, Object> map = new HashMap<>();
		map.put("foo", "42");

		// Act and Assert
		Map<String, String> values = TagsMixin.from(map).getValues();
		assertEquals(1, values.size());
		assertEquals("42", values.get("foo"));
	}

	/**
	 * Test {@link TagsMixin#from(Map)}.
	 * <ul>
	 * <li>Given {@link HashMap#HashMap()} {@code null} is {@link HashMap#HashMap()}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code null} is {@link HashMap#HashMap()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link TagsMixin#from(Map)}
	 */
	@Test
	public void testFrom_givenHashMapNullIsHashMap_whenHashMapNullIsHashMap() {
		// Arrange
		HashMap<Object, Object> objectObjectMap = new HashMap<>();
		objectObjectMap.put(null, new HashMap<>());

		HashMap<Object, Object> objectObjectMap2 = new HashMap<>();
		objectObjectMap2.put(null, objectObjectMap);

		HashMap<String, Object> map = new HashMap<>();
		map.put(null, objectObjectMap2);

		// Act and Assert
		assertTrue(TagsMixin.from(map).getValues().isEmpty());
	}

	/**
	 * Test {@link TagsMixin#from(Map)}.
	 * <ul>
	 * <li>When {@link HashMap#HashMap()}.</li>
	 * <li>Then return Values Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link TagsMixin#from(Map)}
	 */
	@Test
	public void testFrom_whenHashMap_thenReturnValuesEmpty() {
		// Arrange, Act and Assert
		assertTrue(TagsMixin.from(new HashMap<>()).getValues().isEmpty());
	}

}
