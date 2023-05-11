/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SpringBootAdminClientEnabledConditionTest {

	private SpringBootAdminClientEnabledCondition condition;

	private AnnotatedTypeMetadata annotatedTypeMetadata;

	private ConditionContext conditionContext;

	@BeforeEach
	public void setUp() {
		condition = new SpringBootAdminClientEnabledCondition();
		annotatedTypeMetadata = mock(AnnotatedTypeMetadata.class);
		conditionContext = mock(ConditionContext.class);
	}

	@Test
	public void test_emptyUrl_enabled() {
		MockEnvironment environment = new MockEnvironment();
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isFalse();
	}

	@Test
	public void test_emptyUrl_disabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.client.enabled", "false");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isFalse();
	}

	@Test
	public void test_nonEmptyUrl_disabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.client.enabled", "false");
		environment.setProperty("spring.boot.admin.client.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isFalse();
	}

	@Test
	public void test_nonEmptyUrl_enabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.client.url", "http://localhost:8080/management");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isTrue();
	}

}
