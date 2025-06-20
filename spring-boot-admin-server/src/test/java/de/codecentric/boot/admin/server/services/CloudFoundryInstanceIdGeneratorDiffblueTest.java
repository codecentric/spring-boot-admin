package de.codecentric.boot.admin.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudFoundryInstanceIdGeneratorDiffblueTest {

	@InjectMocks
	private CloudFoundryInstanceIdGenerator cloudFoundryInstanceIdGenerator;

	@Mock
	private InstanceIdGenerator instanceIdGenerator;

	/**
	 * Test {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}.
	 * <p>
	 * Method under test: {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}
	 */
	@Test
	public void testGenerateId() {
		// Arrange
		InstanceIdGenerator fallbackIdGenerator = mock(InstanceIdGenerator.class);
		InstanceId ofResult = InstanceId.of("42");
		when(fallbackIdGenerator.generateId(Mockito.<Registration>any())).thenReturn(ofResult);
		CloudFoundryInstanceIdGenerator cloudFoundryInstanceIdGenerator = new CloudFoundryInstanceIdGenerator(
				new CloudFoundryInstanceIdGenerator(fallbackIdGenerator));
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act
		InstanceId actualGenerateIdResult = cloudFoundryInstanceIdGenerator.generateId(registration);

		// Assert
		verify(fallbackIdGenerator).generateId(isA(Registration.class));
		assertSame(ofResult, actualGenerateIdResult);
	}

	/**
	 * Test {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}.
	 * <ul>
	 * <li>Then return {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}
	 */
	@Test
	public void testGenerateId_thenReturnInstanceIdWithValueIs42() {
		// Arrange
		InstanceId ofResult = InstanceId.of("42");
		when(instanceIdGenerator.generateId(Mockito.<Registration>any())).thenReturn(ofResult);
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act
		InstanceId actualGenerateIdResult = cloudFoundryInstanceIdGenerator.generateId(registration);

		// Assert
		verify(instanceIdGenerator).generateId(isA(Registration.class));
		assertSame(ofResult, actualGenerateIdResult);
	}

	/**
	 * Test {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}.
	 * <ul>
	 * <li>Then return Value is {@code 504149e8a3fa}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CloudFoundryInstanceIdGenerator#generateId(Registration)}
	 */
	@Test
	public void testGenerateId_thenReturnValueIs504149e8a3fa() {
		// Arrange
		CloudFoundryInstanceIdGenerator cloudFoundryInstanceIdGenerator = new CloudFoundryInstanceIdGenerator(
				new HashingInstanceUrlIdGenerator());
		Registration registration = Registration.builder()
			.healthUrl("https://example.org/example")
			.managementUrl("https://example.org/example")
			.name("Name")
			.serviceUrl("https://example.org/example")
			.source("Source")
			.build();

		// Act
		InstanceId actualGenerateIdResult = cloudFoundryInstanceIdGenerator.generateId(registration);

		// Assert
		assertEquals("504149e8a3fa", actualGenerateIdResult.getValue());
		assertEquals("504149e8a3fa", actualGenerateIdResult.toString());
	}

}
