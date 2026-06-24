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

import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import de.codecentric.boot.admin.server.config.AdminServerProperties.MonitorProperties.StatusInfoMismatchStrategy;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

public class StatusInfoMismatchStrategyTest {

	@ParameterizedTest
	@ArgumentsSource(FullStrategyArgumentsProvider.class)
	public void shouldConsiderStatusesDifferentByFullEquality(StatusInfo s1, StatusInfo s2, boolean expectedResult) {
		// given -> test setup

		// when
		boolean result = StatusInfoMismatchStrategy.FULL.mismatch(s1, s2);

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@ArgumentsSource(StatusOnlyStrategyArgumentsProvider.class)
	public void shouldConsiderStatusesDifferentOnlyByStatusCode(StatusInfo s1, StatusInfo s2, boolean expectedResult) {
		// given -> test setup

		// when
		boolean result = StatusInfoMismatchStrategy.STATUS_ONLY.mismatch(s1, s2);

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	private static final class FullStrategyArgumentsProvider implements ArgumentsProvider {

		@Override
		public @NonNull Stream<? extends Arguments> provideArguments(@NonNull ParameterDeclarations parameters,
				@NonNull ExtensionContext context) {
			String statusCode1 = "S1";
			String statusCode2 = "S2";
			Map<String, String> details = singletonMap("test", "junit");
			StatusInfo s1 = StatusInfo.valueOf(statusCode1);
			StatusInfo s2 = StatusInfo.valueOf(statusCode1);

			return Stream.of(of(s1, s2, false), of(s1.withDetails(details), s2.withDetails(details), false),
					of(s1, s2.withStatus(statusCode2), true), of(s1, s2.withDetails(details), true),
					of(s1.withDetails(details), StatusInfo.valueOf(statusCode2, emptyMap()), true));
		}

	}

	private static final class StatusOnlyStrategyArgumentsProvider implements ArgumentsProvider {

		@Override
		public @NonNull Stream<? extends Arguments> provideArguments(@NonNull ParameterDeclarations parameters,
				@NonNull ExtensionContext context) {
			String statusCode1 = "JUNIT#1";
			String statusCode2 = "JUNIT#2";
			Map<String, String> details = singletonMap("test", "junit");
			StatusInfo s1 = StatusInfo.valueOf(statusCode1);
			StatusInfo s2 = StatusInfo.valueOf(statusCode1);

			return Stream.of(of(s1, s2, false), of(s1.withDetails(details), s2.withDetails(details), false),
					of(s1, s2.withStatus(statusCode2), true), of(s1, s2.withDetails(details), false),
					of(s1.withDetails(details), StatusInfo.valueOf(statusCode2, emptyMap()), true));
		}

	}

}
