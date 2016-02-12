package de.codecentric.boot.admin.config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SpringBootAdminClientEnablerConditionTest {

	private SpringBootAdminClientEnablerCondition springBootAdminClientEnablerCondition;
	private AnnotatedTypeMetadata annotatedTypeMetadata;
	private ConditionContext conditionContext;
	private MockEnvironment mockEnvironment;

	@Before
	public void setUp() {
		springBootAdminClientEnablerCondition = new SpringBootAdminClientEnablerCondition();
		annotatedTypeMetadata = mock(AnnotatedTypeMetadata.class);
		conditionContext = mock(ConditionContext.class);
		mockEnvironment = new MockEnvironment();
	}

	@Test
	public void testEmptyAdminUrlWithClientEnabled() {
		// Default is spring.boot.admin.client.enabled = true
		// spring.boot.admin.url is not set
		mockEnvironment.setProperty("spring.boot.admin.client.enabled", "true");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(mockEnvironment);
		assertFalse(springBootAdminClientEnablerCondition
				.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void testEmptyAdminUrlWithClientDisabled() {
		// Default is spring.boot.admin.client.enabled = true
		// spring.boot.admin.url is not set
		mockEnvironment.setProperty("spring.boot.admin.client.enabled", "false");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(mockEnvironment);
		assertFalse(springBootAdminClientEnablerCondition
				.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void testNonEmptyAdminUrlWithClientDisabled() {
		// Default is spring.boot.admin.client.enabled = false
		// spring.boot.admin.url is set
		mockEnvironment.setProperty("spring.boot.admin.client.enabled", "false");
		mockEnvironment.setProperty("spring.boot.admin.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(mockEnvironment);
		assertFalse(springBootAdminClientEnablerCondition
				.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void testNonEmptyAdminUrlWithClientEnabled() {
		// Default is spring.boot.admin.client.enabled = true
		// spring.boot.admin.url is set
		mockEnvironment.setProperty("spring.boot.admin.client.enabled", "true");
		mockEnvironment.setProperty("spring.boot.admin.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(mockEnvironment);
		assertTrue(springBootAdminClientEnablerCondition
				.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}
}
