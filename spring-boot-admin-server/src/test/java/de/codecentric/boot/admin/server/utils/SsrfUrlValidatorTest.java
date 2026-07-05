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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.http.client.InetAddressFilter;

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
		validator = new SsrfUrlValidator(properties, InetAddressFilter.externalAddresses());
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
	void rejects_malformedUrl() {
		// A syntactically invalid URI must be rejected, not silently accepted.
		// Otherwise a crafted string that new URI() cannot parse but an HTTP client
		// would normalise to a private address bypasses all checks.
		assertThatThrownBy(() -> validator.validate("http://169.254.169.254\\ @evil.com/"))
			.isInstanceOf(SsrfProtectionException.class)
			.hasMessageContaining("not a valid URI");
	}

	@Test
	void ignores_unresolvableHost() {
		// Unknown hosts cannot be resolved at registration time; the HTTP client's
		// InetAddressFilter will enforce the policy when a connection is attempted.
		assertThatCode(() -> validator.validate("http://this-host-does-not-exist.invalid/health"))
			.doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://example.com/actuator/health", "https://api.example.com/admin/health",
			"https://93.184.216.34/health" // example.com IP
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
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://[::1]/health", "http://[0:0:0:0:0:0:0:1]/health" })
		void blocks_loopbackIpv6(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
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
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void blocks_ipv6LinkLocal() {
			assertThatThrownBy(() -> validator.validate("http://[fe80::1]/path"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
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
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://192.168.0.1/", "http://192.168.255.254/" })
		void blocks_classC(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://172.16.0.1/", "http://172.20.1.1/", "http://172.31.255.255/" })
		void blocks_classB(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@ParameterizedTest
		@ValueSource(strings = { "http://172.15.0.1/", "http://172.32.0.1/" })
		void doesNotBlock_outsideClassBRange(String url) {
			// 172.15.x and 172.32.x are NOT in 172.16-31.x
			assertThatCode(() -> validator.validate(url)).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// IPv6 special ranges
	// -------------------------------------------------------------------------

	@Nested
	class Ipv6SpecialRanges {

		@ParameterizedTest
		@ValueSource(strings = { "http://[fc00::1]/", "http://[fd12:3456:789a::1]/" })
		void blocks_uniqueLocal(String url) {
			assertThatThrownBy(() -> validator.validate(url)).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

	}

	// -------------------------------------------------------------------------
	// Allowed CIDRs (property-driven filter extension)
	// -------------------------------------------------------------------------

	@Nested
	class AllowedCidrs {

		private SsrfUrlValidator cidrValidator(String... cidrs) {
			properties.setAllowedCidrs(java.util.List.of(cidrs));
			InetAddressFilter filter = buildFilterFromProperties(properties);
			return new SsrfUrlValidator(properties, filter);
		}

		// Mirrors the logic in AdminServerAutoConfiguration.ssrfInetAddressFilter()
		private InetAddressFilter buildFilterFromProperties(
				de.codecentric.boot.admin.server.config.AdminServerProperties.SsrfProtectionProperties props) {
			InetAddressFilter filter = InetAddressFilter.externalAddresses();
			if (!props.getAllowedCidrs().isEmpty()) {
				filter = filter.or(props.getAllowedCidrs().toArray(String[]::new));
			}
			return filter;
		}

		@Test
		void allows_exactIpInAllowedCidr() {
			SsrfUrlValidator v = cidrValidator("192.168.1.0/24");
			assertThatCode(() -> v.validate("http://192.168.1.100/actuator/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_firstAddressInCidr() {
			SsrfUrlValidator v = cidrValidator("10.0.0.0/8");
			assertThatCode(() -> v.validate("http://10.0.0.1/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_lastAddressInCidr() {
			SsrfUrlValidator v = cidrValidator("192.168.1.0/24");
			assertThatCode(() -> v.validate("http://192.168.1.254/health")).doesNotThrowAnyException();
		}

		@Test
		void blocks_addressOutsideAllowedCidr() {
			SsrfUrlValidator v = cidrValidator("192.168.1.0/24");
			// 192.168.2.1 is outside 192.168.1.0/24
			assertThatThrownBy(() -> v.validate("http://192.168.2.1/health"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void allows_singleHostCidr() {
			SsrfUrlValidator v = cidrValidator("10.1.2.3/32");
			assertThatCode(() -> v.validate("http://10.1.2.3/health")).doesNotThrowAnyException();
		}

		@Test
		void blocks_neighbourOfSingleHostCidr() {
			SsrfUrlValidator v = cidrValidator("10.1.2.3/32");
			assertThatThrownBy(() -> v.validate("http://10.1.2.4/health")).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void allows_ipv6Cidr() {
			SsrfUrlValidator v = cidrValidator("fd00::/8");
			assertThatCode(() -> v.validate("http://[fd12:3456:789a::1]/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_multipleCidrs() {
			SsrfUrlValidator v = cidrValidator("10.0.0.0/8", "192.168.1.0/24");
			assertThatCode(() -> v.validate("http://10.0.0.1/health")).doesNotThrowAnyException();
			assertThatCode(() -> v.validate("http://192.168.1.50/health")).doesNotThrowAnyException();
		}

		@Test
		void allows_externalAddressWithCidrConfigured() {
			// Configuring a CIDR allowlist must not break external address access
			SsrfUrlValidator v = cidrValidator("192.168.1.0/24");
			assertThatCode(() -> v.validate("http://example.com/health")).doesNotThrowAnyException();
		}

	}

	// -------------------------------------------------------------------------
	// Custom InetAddressFilter
	// -------------------------------------------------------------------------

	@Nested
	class CustomInetAddressFilter {

		@Test
		void allowsPrivateAddress_whenFilterPermitsIt() {
			// A custom filter that also allows 192.168.1.0/24
			InetAddressFilter customFilter = InetAddressFilter.externalAddresses().or("192.168.1.0/24");
			SsrfUrlValidator customValidator = new SsrfUrlValidator(properties, customFilter);
			assertThatCode(() -> customValidator.validate("http://192.168.1.100/actuator/health"))
				.doesNotThrowAnyException();
		}

		@Test
		void blocksAddress_whenFilterRejectsIt() {
			// A filter that only allows a specific public IP
			InetAddressFilter customFilter = InetAddressFilter.of("93.184.216.34/32");
			SsrfUrlValidator customValidator = new SsrfUrlValidator(properties, customFilter);
			assertThatThrownBy(() -> customValidator.validate("http://10.0.0.1/health"))
				.isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

	}

}
