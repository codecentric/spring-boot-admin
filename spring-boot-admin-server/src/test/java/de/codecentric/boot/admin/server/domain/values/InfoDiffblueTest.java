package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class InfoDiffblueTest {

	/**
	 * Test {@link Info#from(Map)}.
	 * <ul>
	 * <li>Given {@code foo}.</li>
	 * <li>When {@link HashMap#HashMap()} {@code foo} is {@code 42}.</li>
	 * <li>Then return Values is {@link HashMap#HashMap()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Info#from(Map)}
	 */
	@Test
	public void testFrom_givenFoo_whenHashMapFooIs42_thenReturnValuesIsHashMap() {
		// Arrange
		HashMap<String, Object> values = new HashMap<>();
		values.put("foo", "42");

		// Act and Assert
		assertEquals(values, Info.from(values).getValues());
	}

	/**
	 * Test {@link Info#from(Map)}.
	 * <ul>
	 * <li>When {@link HashMap#HashMap()}.</li>
	 * <li>Then return Values Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Info#from(Map)}
	 */
	@Test
	public void testFrom_whenHashMap_thenReturnValuesEmpty() {
		// Arrange, Act and Assert
		assertTrue(Info.from(new HashMap<>()).getValues().isEmpty());
	}

	/**
	 * Test {@link Info#from(Map)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return Values Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Info#from(Map)}
	 */
	@Test
	public void testFrom_whenNull_thenReturnValuesEmpty() {
		// Arrange, Act and Assert
		assertTrue(Info.from(null).getValues().isEmpty());
	}

	/**
	 * Test {@link Info#empty()}.
	 * <p>
	 * Method under test: {@link Info#empty()}
	 */
	@Test
	public void testEmpty() {
		// Arrange, Act and Assert
		assertTrue(Info.empty().getValues().isEmpty());
	}

	/**
	 * Test {@link Info#getValues()}.
	 * <p>
	 * Method under test: {@link Info#getValues()}
	 */
	@Test
	public void testGetValues() {
		// Arrange, Act and Assert
		assertTrue(Info.empty().getValues().isEmpty());
	}

}
