package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import org.junit.Test;

public class InstanceIdMixinDiffblueTest {

	/**
	 * Test {@link InstanceIdMixin#of(String)}.
	 * <ul>
	 * <li>When {@code 42}.</li>
	 * <li>Then return Value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceIdMixin#of(String)}
	 */
	@Test
	public void testOf_when42_thenReturnValueIs42() {
		// Arrange and Act
		InstanceId actualOfResult = InstanceIdMixin.of("42");

		// Assert
		assertEquals("42", actualOfResult.getValue());
		assertEquals("42", actualOfResult.toString());
	}

}
