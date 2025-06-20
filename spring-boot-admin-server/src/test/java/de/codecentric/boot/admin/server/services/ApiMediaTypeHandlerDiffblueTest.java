package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.springframework.http.MediaType;

public class ApiMediaTypeHandlerDiffblueTest {

	/**
	 * Test {@link ApiMediaTypeHandler#isApiMediaType(MediaType)}.
	 * <ul>
	 * <li>When {@link MediaType#MediaType(String)} with {@code Type}.</li>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link ApiMediaTypeHandler#isApiMediaType(MediaType)}
	 */
	@Test
	public void testIsApiMediaType_whenMediaTypeWithType_thenReturnFalse() {
		// Arrange
		ApiMediaTypeHandler apiMediaTypeHandler = new ApiMediaTypeHandler();

		// Act and Assert
		assertFalse(apiMediaTypeHandler.isApiMediaType(new MediaType("Type")));
	}

}
