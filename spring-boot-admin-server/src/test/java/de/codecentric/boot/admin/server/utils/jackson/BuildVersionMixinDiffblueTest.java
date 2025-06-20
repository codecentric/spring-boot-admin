package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import org.junit.Test;

public class BuildVersionMixinDiffblueTest {

	/**
	 * Test {@link BuildVersionMixin#valueOf(String)}.
	 * <ul>
	 * <li>When {@code foo}.</li>
	 * <li>Then return Value is {@code foo}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link BuildVersionMixin#valueOf(String)}
	 */
	@Test
	public void testValueOf_whenFoo_thenReturnValueIsFoo() {
		// Arrange and Act
		BuildVersion actualValueOfResult = BuildVersionMixin.valueOf("foo");

		// Assert
		assertEquals("foo", actualValueOfResult.getValue());
		assertEquals("foo", actualValueOfResult.toString());
	}

	/**
	 * Test {@link BuildVersionMixin#valueOf(String)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return Value is {@code UNKNOWN}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link BuildVersionMixin#valueOf(String)}
	 */
	@Test
	public void testValueOf_whenNull_thenReturnValueIsUnknown() {
		// Arrange and Act
		BuildVersion actualValueOfResult = BuildVersionMixin.valueOf(null);

		// Assert
		assertEquals("UNKNOWN", actualValueOfResult.getValue());
		assertEquals("UNKNOWN", actualValueOfResult.toString());
	}

}
