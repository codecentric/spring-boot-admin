package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class InstanceIdDiffblueTest {

	/**
	 * Test {@link InstanceId#of(String)}.
	 * <ul>
	 * <li>When {@code 42}.</li>
	 * <li>Then return Value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InstanceId#of(String)}
	 */
	@Test
	public void testOf_when42_thenReturnValueIs42() {
		// Arrange and Act
		InstanceId actualOfResult = InstanceId.of("42");

		// Assert
		assertEquals("42", actualOfResult.getValue());
		assertEquals("42", actualOfResult.toString());
	}

	/**
	 * Test {@link InstanceId#toString()}.
	 * <p>
	 * Method under test: {@link InstanceId#toString()}
	 */
	@Test
	public void testToString() {
		// Arrange, Act and Assert
		assertEquals("42", InstanceId.of("42").toString());
	}

	/**
	 * Test {@link InstanceId#compareTo(InstanceId)} with {@code InstanceId}.
	 * <p>
	 * Method under test: {@link InstanceId#compareTo(InstanceId)}
	 */
	@Test
	public void testCompareToWithInstanceId() {
		// Arrange
		InstanceId ofResult = InstanceId.of("42");

		// Act and Assert
		assertEquals(0, ofResult.compareTo(InstanceId.of("42")));
	}

}
