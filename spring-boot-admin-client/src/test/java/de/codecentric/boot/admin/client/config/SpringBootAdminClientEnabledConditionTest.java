package de.codecentric.boot.admin.client.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import de.codecentric.boot.admin.client.config.SpringBootAdminClientEnabledCondition;

public class SpringBootAdminClientEnabledConditionTest {

	private SpringBootAdminClientEnabledCondition condition;
	private AnnotatedTypeMetadata annotatedTypeMetadata;
	private ConditionContext conditionContext;

	@Before
	public void setUp() {
		condition = new SpringBootAdminClientEnabledCondition();
		annotatedTypeMetadata = mock(AnnotatedTypeMetadata.class);
		conditionContext = mock(ConditionContext.class);
	}

	@Test
	public void test_emptyUrl_enabled() {
		MockEnvironment environment = new MockEnvironment();
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertFalse(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void test_emptyUrl_disabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.client.enabled", "false");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertFalse(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void test_nonEmptyUrl_disabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.client.enabled", "false");
		environment.setProperty("spring.boot.admin.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertFalse(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}

	@Test
	public void test_nonEmptyUrl_enabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertTrue(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch());
	}
}
