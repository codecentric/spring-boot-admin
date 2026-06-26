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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.server.config.AdminServerProperties.SsrfProtectionProperties;
import de.codecentric.boot.admin.server.web.client.exception.SsrfProtectionException;

/**
 * Validates URLs submitted for instance registration and proxying against SSRF (Server-
 * Side Request Forgery) attack patterns.
 *
 * <p>
 * When SSRF protection is enabled
 * ({@code spring.boot.admin.ssrf-protection.enabled=true}), this validator checks:
 * <ol>
 * <li><b>Scheme</b> – only schemes listed in
 * {@code spring.boot.admin.ssrf-protection.allowed-schemes} are accepted (default:
 * {@code http}, {@code https}).</li>
 * <li><b>Address filtering</b> – the hostname is resolved and the resulting
 * {@link InetAddress} is tested against a {@link InetAddressFilter}. The default filter
 * (auto-configured via {@code AdminServerAutoConfiguration}) blocks all private,
 * loopback, and link-local addresses via
 * {@link InetAddressFilter#externalAddresses()}.</li>
 * </ol>
 *
 * <p>
 * To allow access to internal/private hosts (e.g. for intranet SBA deployments), expose a
 * custom {@link InetAddressFilter} {@code @Bean} in your application context. The bean
 * replaces the default filter for all SSRF checks.
 *
 * <p>
 * When SSRF protection is disabled
 * ({@code spring.boot.admin.ssrf-protection.enabled=false}, which is the default) all
 * URLs are accepted without further inspection.
 */
public class SsrfUrlValidator {

	private final SsrfProtectionProperties properties;

	private final InetAddressFilter inetAddressFilter;

	public SsrfUrlValidator(SsrfProtectionProperties properties, InetAddressFilter inetAddressFilter) {
		this.properties = properties;
		this.inetAddressFilter = inetAddressFilter;
	}

	/**
	 * Validates the given URL against the configured SSRF protection rules.
	 * @param url the URL to validate (may be {@code null} or blank, which is skipped)
	 * @throws SsrfProtectionException if the URL violates a protection rule and
	 * protection is enabled
	 */
	public void validate(@Nullable String url) {
		if (!properties.isEnabled()) {
			return;
		}
		if (!StringUtils.hasText(url)) {
			return;
		}

		URI uri;
		try {
			uri = new URI(url);
		}
		catch (URISyntaxException ex) {
			return;
		}

		checkScheme(url, uri.getScheme());
		checkAddress(url, uri.getHost());
	}

	private void checkScheme(String url, @Nullable String scheme) {
		if (scheme == null) {
			return;
		}
		if (!properties.getAllowedSchemes().contains(scheme.toLowerCase())) {
			throw new SsrfProtectionException("URL '" + url + "' uses disallowed scheme '" + scheme
					+ "'. Allowed schemes: " + properties.getAllowedSchemes()
					+ ". Configure 'spring.boot.admin.ssrf-protection.allowed-schemes' to adjust.");
		}
	}

	private void checkAddress(String url, @Nullable String host) {
		if (host == null || host.isBlank()) {
			return;
		}
		InetAddress[] addresses;
		try {
			addresses = InetAddress.getAllByName(host);
		}
		catch (UnknownHostException ex) {
			// Host cannot be resolved at registration time (service may not be running
			// yet). Allow it through — the InetAddressFilter on the HTTP client will
			// enforce the policy when an actual connection is attempted.
			return;
		}
		for (InetAddress address : addresses) {
			if (!inetAddressFilter.matches(address)) {
				throw new SsrfProtectionException("URL '" + url + "' is blocked: address '" + address.getHostAddress()
						+ "' for host '" + host + "' is not permitted by the configured InetAddressFilter. "
						+ "Provide a custom InetAddressFilter @Bean to allow internal addresses, "
						+ "or disable protection with 'spring.boot.admin.ssrf-protection.enabled=false'.");
			}
		}
	}

}
