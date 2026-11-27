/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.utils;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HostPatternMatcherTest {

	private final HostPatternMatcher matcher = HostPatternMatcher
		.of(List.of("payment.example.com", "*.svc.cluster.local", "10.0.0.5", "[fd00::1]"));

	@ParameterizedTest
	@ValueSource(
			strings = { "payment.example.com", "PAYMENT.Example.COM", "payment.example.com.", "a.svc.cluster.local",
					"a.b.svc.cluster.local", "10.0.0.5", "[fd00::1]", "fd00:0:0:0:0:0:0:1", "[fd00:0000::1]" })
	void matches(String host) {
		assertThat(this.matcher.matches(host)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "svc.cluster.local", "evilsvc.cluster.local", "a.svc.cluster.local.evil.com",
			"payment.example.com.evil.com", "evil-payment.example.com", "attacker.example", "10.0.0.6", "010.0.0.5",
			"[fd00::2]", "", " " })
	void doesNotMatch(String host) {
		assertThat(this.matcher.matches(host)).isFalse();
	}

	@Test
	void doesNotMatchNull() {
		assertThat(this.matcher.matches(null)).isFalse();
	}

	@Test
	void wildcardNeverMatchesIpLiterals() {
		HostPatternMatcher wildcard = HostPatternMatcher.of(List.of("*.0.0.1"));
		assertThat(wildcard.matches("127.0.0.1")).isFalse();
	}

	@Test
	void matchesInternationalizedDomainNames() {
		HostPatternMatcher idn = HostPatternMatcher.of(List.of("*.bücher.example"));
		assertThat(idn.matches("shop.xn--bcher-kva.example")).isTrue();
		assertThat(idn.matches("shop.bücher.example")).isTrue();
		assertThat(idn.matches("shop.bucher.example")).isFalse();
	}

	@Test
	void isEmptyWithoutPatterns() {
		assertThat(HostPatternMatcher.of(null).isEmpty()).isTrue();
		assertThat(HostPatternMatcher.of(List.of()).isEmpty()).isTrue();
		assertThat(HostPatternMatcher.of(List.of(" ")).isEmpty()).isTrue();
		assertThat(HostPatternMatcher.of(List.of()).matches("example.com")).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "*.", "foo.*.com", "*.*.com", "pay*.example.com", "*.10.0.0.1" })
	void rejectsInvalidPatterns(String pattern) {
		assertThatIllegalArgumentException().isThrownBy(() -> HostPatternMatcher.of(List.of(pattern)));
	}

}
