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

package de.codecentric.boot.admin.server.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SpringBootAdminServerEnabledConditionTest {

	private SpringBootAdminServerEnabledCondition condition;

	private AnnotatedTypeMetadata annotatedTypeMetadata;

	private ConditionContext conditionContext;

	@BeforeEach
	public void setUp() {
		condition = new SpringBootAdminServerEnabledCondition();
		annotatedTypeMetadata = mock(AnnotatedTypeMetadata.class);
		conditionContext = mock(ConditionContext.class);
	}

	@Test
	public void test_server_enabled() {
		MockEnvironment environment = new MockEnvironment();
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isTrue();
	}

	@Test
	public void test_server_disabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.boot.admin.server.enabled", "false");
		BDDMockito.given(conditionContext.getEnvironment()).willReturn(environment);
		assertThat(condition.getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch()).isFalse();
	}

}
