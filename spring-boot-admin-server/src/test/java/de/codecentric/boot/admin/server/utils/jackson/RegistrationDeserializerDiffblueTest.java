package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.junit.Test;

public class RegistrationDeserializerDiffblueTest {

	/**
	 * Test new {@link RegistrationDeserializer} (default constructor).
	 * <p>
	 * Method under test: default or parameterless constructor of
	 * {@link RegistrationDeserializer}
	 */
	@Test
	public void testNewRegistrationDeserializer() {
		// Arrange and Act
		RegistrationDeserializer actualRegistrationDeserializer = new RegistrationDeserializer();

		// Assert
		assertNull(actualRegistrationDeserializer.getValueType());
		Class<Registration> expectedValueClass = Registration.class;
		assertEquals(expectedValueClass, actualRegistrationDeserializer.getValueClass());
	}

}
