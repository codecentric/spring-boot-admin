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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.codecentric.boot.admin.server.config.AdminServerProperties.SsrfProtectionProperties;
import de.codecentric.boot.admin.server.web.client.exception.SsrfProtectionException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SsrfUrlValidatorTest {

	private SsrfProtectionProperties properties;

	private SsrfUrlValidator validator;

	@BeforeEach
	void setUp() {
		properties = new SsrfProtectionProperties();
		properties.setEnabled(true);
		validator = new SsrfUrlValidator(properties);
	}

	@Test
	void doesNothing_whenDisabled() {
		properties.setEnabled(false);
		assertThatCode(() -> validator.validate("http://169.254.169.254/latest/meta-data")).doesNotThrowAnyException();
		assertThatCode(() -> validator.validate("http://127.0.0.1/")).doesNotThrowAnyException();
		assertThatCode(() -> validator.validate("file:///etc/passwd")).doesNotThrowAnyException();
	}

	@Test
	void ignores_nullAndBlankUrls() {
		assertThatCode(() -> validator.validate(null)).doesNotThrowAnyException();
		assertThatCode(() -> validator.validate("")).doesNotThrowAnyException();
		assertThatCode(() -> validator.validate("   ")).doesNotThrowAnyException();
	}

	@Test
	void blocks_unspecifiedAddress() {
		assertThatThrownBy(() -> validator.validate("http://0.0.0.0/")).isInstanceOf(SsrfProtectionException.class)
			.hasMessageContaining("unspecified address");
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://example.com/actuator/health", "https://api.example.com/admin/health",
			"https://93.184.216.34/health", // example.com IP
			"http://172.15.0.1/health", // just outside 172.16-31 range
			"http://172.32.0.1/health" // just above 172.16-31 range
	})
	void allows_publicUrls(String url) {
		assertThatCode(() -> validator.validate(url)).doesNotThrowAnyException();
	}

	// -------------------------------------------------------------------------
	// Scheme checks
	// -------------------------------------------------------------------------

	@Nested
	class SchemeValidation {

		@Test
		void allows_http() {
			assertThatCode(() -> validator.validate("http://example.com/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_https() {
			assertThatCode(() -> validator.validate("https://example.com/health")).doesNotThrowAnyException();
		}

		@Test
		void blocks_fileScheme() {
			assertThatThrownBy(() -> validator.validate("file:///etc/passwd"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("disallowed scheme");
		}

		@Test
		void blocks_ftpScheme() {
			assertThatThrownBy(() -> validator.validate("ftp://internal-host/"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("disallowed scheme");
		}

		@Test
		void allows_customScheme_whenConfigured() {
			properties.getAllowedSchemes().add("ftp");
			assertThatCode(() -> validator.validate("ftp://public-ftp.example.com/")).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// Loopback addresses
	// -------------------------------------------------------------------------

	@Nested
	class LoopbackAddresses {

		@ParameterizedTest
		@ValueSource(strings = { "http://localhost/health", "http://127.0.0.1/health", "http://127.0.0.2/health",
				"http://127.255.255.255/health" })
		void blocks_loopbackIpv4(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("loopback");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://[::1]/health", "http://[0:0:0:0:0:0:0:1]/health" })
		void blocks_loopbackIpv6(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("loopback");
		}

	}

	// -------------------------------------------------------------------------
	// Link-local (cloud metadata endpoints)
	// -------------------------------------------------------------------------

	@Nested
	class LinkLocalAddresses {

		@ParameterizedTest
		@ValueSource(strings = { "http://169.254.169.254/latest/meta-data",
				"http://169.254.169.254/latest/meta-data/iam/security-credentials/", "http://169.254.0.1/anything" })
		void blocks_awsMetadataEndpoint(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("link-local");
		}

		@Test
		void blocks_ipv6LinkLocal() {
			assertThatThrownBy(() -> validator.validate("http://[fe80::1]/path"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("link-local");
		}

	}

	// -------------------------------------------------------------------------
	// RFC 1918 private ranges
	// -------------------------------------------------------------------------

	@Nested
	class PrivateRanges {

		@ParameterizedTest
		@ValueSource(strings = { "http://10.0.0.1/", "http://10.255.255.255/health" })
		void blocks_classA(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("10.0.0.0/8");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://192.168.0.1/", "http://192.168.255.254/" })
		void blocks_classC(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("192.168.0.0/16");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://172.16.0.1/", "http://172.20.1.1/", "http://172.31.255.255/" })
		void blocks_classB(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("172.16.0.0/12");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://172.15.0.1/", "http://172.32.0.1/" })
		void doesNotBlock_outsideClassBRange(String url) {
			// 172.15.x and 172.32.x are NOT in 172.16-31.x
			assertThatCode(() -> validator.validate(url)).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// IPv6 unique-local and IPv4-mapped
	// -------------------------------------------------------------------------

	@Nested
	class Ipv6SpecialRanges {

		@ParameterizedTest
		@ValueSource(strings = { "http://[fc00::1]/", "http://[fd12:3456:789a::1]/" })
		void blocks_uniqueLocal(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("unique-local");
		}

		@Test
		void blocks_ipv4MappedPrivate() {
			// ::ffff:192.168.1.1 embeds a private IPv4 address
			assertThatThrownBy(() -> validator.validate("http://[::ffff:192.168.1.1]/"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("192.168.0.0/16");
		}

		@Test
		void allows_ipv4MappedPublic() {
			// ::ffff:93.184.216.34 embeds a public IPv4 address (example.com)
			assertThatCode(() -> validator.validate("http://[::ffff:93.184.216.34]/")).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// Allowlist overrides
	// -------------------------------------------------------------------------

	@Nested
	class AllowlistOverrides {

		@Test
		void allowedHost_exactMatch_overridesBlockedRange() {
			properties.setAllowedHosts(List.of("192.168.1.100"));
			assertThatCode(() -> validator.validate("http://192.168.1.100/actuator/health")).doesNotThrowAnyException();
		}

		@Test
		void allowedHost_globSuffix_overridesBlockedPatterns() {
			properties.setAllowedHosts(List.of("*.internal.corp"));
			assertThatCode(() -> validator.validate("http://svc.internal.corp/actuator/health"))
				.doesNotThrowAnyException();
		}

		@Test
		void allowedHost_globSuffix_doesNotMatchUnrelatedHost() {
			properties.setAllowedHosts(List.of("*.internal.corp"));
			properties.setBlockedHostPatterns(List.of(".*\\.evil\\.corp$"));
			assertThatThrownBy(() -> validator.validate("http://svc.evil.corp/actuator/health"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("blocked pattern");
		}

		@Test
		void allowedHost_exactMatch_isCaseInsensitive() {
			properties.setAllowedHosts(List.of("MY-INTERNAL-HOST"));
			assertThatCode(() -> validator.validate("http://my-internal-host/health")).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// User-supplied blocked patterns
	// -------------------------------------------------------------------------

	@Nested
	class BlockedHostPatterns {

		@Test
		void blocks_hostMatchingCustomPattern() {
			properties.setBlockedHostPatterns(List.of(".*\\.internal\\.corp$"));
			assertThatThrownBy(() -> validator.validate("http://svc.internal.corp/health"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("blocked pattern");
		}

		@Test
		void ignores_invalidRegexPattern_withoutThrowing() {
			properties.setBlockedHostPatterns(List.of("[invalid-regex"));
			// Should log a warning but not throw
			assertThatCode(() -> validator.validate("http://example.com/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_hostNotMatchingCustomPattern() {
			properties.setBlockedHostPatterns(List.of(".*\\.internal\\.corp$"));
			assertThatCode(() -> validator.validate("http://example.com/health")).doesNotThrowAnyException();
		}

	}

}
