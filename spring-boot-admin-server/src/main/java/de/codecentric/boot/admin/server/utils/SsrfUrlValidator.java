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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <li><b>Private / internal IP ranges</b> – the raw hostname is compared against
 * well-known private and special-purpose address literals without performing DNS
 * resolution. Blocked by default: loopback ({@code 127.x}, {@code ::1},
 * {@code localhost}), link-local ({@code 169.254.x} / {@code fe80:}), RFC 1918
 * ({@code 10.x}, {@code 172.16-31.x}, {@code 192.168.x}), unique-local IPv6
 * ({@code fc}/{@code fd}), and the unspecified address ({@code 0.0.0.0}).</li>
 * <li><b>User-supplied block patterns</b> – additional regex patterns provided via
 * {@code spring.boot.admin.ssrf-protection.blocked-host-patterns} are matched against the
 * hostname.</li>
 * <li><b>Allowlist override</b> – hosts listed in
 * {@code spring.boot.admin.ssrf-protection.allowed-hosts} bypass all block checks.
 * Supports exact host names and glob-style suffix patterns (e.g.
 * {@code *.internal.corp}).</li>
 * </ol>
 *
 * <p>
 * <b>Limitation:</b> Only literal hostname strings are inspected; no DNS resolution is
 * performed. An attacker who controls a public DNS record that resolves to a private IP
 * (DNS rebinding) is not blocked by this validator alone. Use IMDSv2 or network-level
 * egress controls as additional layers of defence.
 *
 * <p>
 * When SSRF protection is disabled
 * ({@code spring.boot.admin.ssrf-protection.enabled=false}, which is the default) all
 * URLs are accepted without further inspection.
 */
public class SsrfUrlValidator {

	private static final Logger log = LoggerFactory.getLogger(SsrfUrlValidator.class);

	private final SsrfProtectionProperties properties;

	public SsrfUrlValidator(SsrfProtectionProperties properties) {
		this.properties = properties;
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

		String scheme = uri.getScheme();
		String host = uri.getHost();

		checkScheme(url, scheme);
		if (host != null && !isAllowed(host)) {
			checkPrivateHost(url, host);
			checkBlockedPatterns(url, host);
		}
	}

	// -------------------------------------------------------------------------
	// Scheme check
	// -------------------------------------------------------------------------

	private void checkScheme(String url, @Nullable String scheme) {
		if (scheme == null) {
			return;
		}
		if (!properties.getAllowedSchemes().contains(scheme.toLowerCase())) {
			throw new SsrfProtectionException("URL '" + url + "' uses disallowed scheme '" + scheme
					+ "'. Allowed schemes: " + properties.getAllowedSchemes()
					+ ". Configure 'spring.boot.admin.ssrf-protection" + ".allowed-schemes' to adjust.");
		}
	}

	// -------------------------------------------------------------------------
	// Allowlist check
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the host matches any entry in the configured
	 * {@code allowedHosts} list. Supports exact matches and glob-style suffix wildcards
	 * (e.g. {@code *.internal.corp} matches {@code svc.internal.corp}).
	 * @param host the raw hostname from the URL (not null)
	 * @return true if the host is explicitly allowed
	 */
	private boolean isAllowed(String host) {
		String lowerHost = host.toLowerCase();
		for (String allowed : properties.getAllowedHosts()) {
			String lowerAllowed = allowed.toLowerCase();
			if (lowerAllowed.startsWith("*.")) {
				// Glob suffix: *.example.com matches foo.example.com
				String suffix = lowerAllowed.substring(1); // -> .example.com
				if (lowerHost.endsWith(suffix) || lowerHost.equals(suffix.substring(1))) {
					return true;
				}
			}
			else if (lowerHost.equals(lowerAllowed)) {
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------------------------------------
	// Private / internal host check (no DNS resolution — literal strings only)
	// -------------------------------------------------------------------------

	private void checkPrivateHost(String url, String host) {
		String lowerHost = host.toLowerCase();

		// Loopback
		if (lowerHost.equals("localhost") || lowerHost.equals("0:0:0:0:0:0:0:1") || lowerHost.equals("::1")) {
			rejectPrivate(url, host, "loopback");
		}

		// Unspecified
		if (lowerHost.equals("0.0.0.0")) {
			rejectPrivate(url, host, "unspecified address");
		}

		// IPv4 — parse only if it looks like a dotted-decimal address
		if (looksLikeIpv4(lowerHost)) {
			checkPrivateIpv4(url, host, lowerHost);
			return; // don't run IPv6 checks on IPv4 addresses
		}

		// IPv6
		checkPrivateIpv6(url, host, lowerHost);
	}

	private boolean looksLikeIpv4(String host) {
		// Simple heuristic: all chars are digits or dots, at least one dot
		return host.chars().allMatch((c) -> Character.isDigit(c) || c == '.') && host.contains(".");
	}

	private void checkPrivateIpv4(String url, String host, String lowerHost) {
		// Loopback: 127.0.0.0/8
		if (lowerHost.startsWith("127.")) {
			rejectPrivate(url, host, "loopback (127.0.0.0/8)");
		}
		// Link-local / AWS metadata: 169.254.0.0/16
		if (lowerHost.startsWith("169.254.")) {
			rejectPrivate(url, host, "link-local (169.254.0.0/16, includes cloud metadata endpoints)");
		}
		// RFC 1918 — Class A: 10.0.0.0/8
		if (lowerHost.startsWith("10.")) {
			rejectPrivate(url, host, "private (10.0.0.0/8)");
		}
		// RFC 1918 — Class C: 192.168.0.0/16
		if (lowerHost.startsWith("192.168.")) {
			rejectPrivate(url, host, "private (192.168.0.0/16)");
		}
		// RFC 1918 — Class B: 172.16.0.0/12 (172.16.x.x – 172.31.x.x)
		if (lowerHost.startsWith("172.")) {
			String[] parts = lowerHost.split("\\.");
			if (parts.length >= 2) {
				try {
					int second = Integer.parseInt(parts[1]);
					if (second >= 16 && second <= 31) {
						rejectPrivate(url, host, "private (172.16.0.0/12)");
					}
				}
				catch (NumberFormatException ignored) {
					// not a valid IPv4 — fall through
				}
			}
		}
	}

	private void checkPrivateIpv6(String url, String host, String lowerHost) {
		// Strip surrounding brackets that URI.getHost() may leave on IPv6 literals
		String stripped = (lowerHost.startsWith("[") && lowerHost.endsWith("]"))
				? lowerHost.substring(1, lowerHost.length() - 1) : lowerHost;

		// Loopback ::1
		if (stripped.equals("::1") || stripped.equals("0:0:0:0:0:0:0:1")) {
			rejectPrivate(url, host, "loopback (::1)");
		}
		// Link-local fe80::/10
		if (stripped.startsWith("fe80:") || stripped.startsWith("fe8") || stripped.startsWith("fe9")
				|| stripped.startsWith("fea") || stripped.startsWith("feb")) {
			rejectPrivate(url, host, "link-local (fe80::/10)");
		}
		// Unique-local fc00::/7 — covers fc and fd prefixes
		if (stripped.startsWith("fc") || stripped.startsWith("fd")) {
			rejectPrivate(url, host, "unique-local (fc00::/7)");
		}
		// IPv4-mapped IPv6: ::ffff:0:0/96 — e.g. ::ffff:192.168.1.1
		if (stripped.startsWith("::ffff:")) {
			String embedded = stripped.substring("::ffff:".length());
			// Recursively validate the embedded IPv4 part
			checkPrivateIpv4(url, embedded, embedded.toLowerCase());
		}
	}

	// -------------------------------------------------------------------------
	// User-supplied blocked patterns
	// -------------------------------------------------------------------------

	private void checkBlockedPatterns(String url, String host) {
		for (String rawPattern : properties.getBlockedHostPatterns()) {
			if (!StringUtils.hasText(rawPattern)) {
				continue;
			}
			try {
				if (Pattern.compile(rawPattern).matcher(host).matches()) {
					throw new SsrfProtectionException("URL '" + url + "' is blocked: host '" + host
							+ "' matches configured blocked pattern '" + rawPattern
							+ "'. Configure 'spring.boot.admin.ssrf-protection.blocked-host-patterns' to adjust.");
				}
			}
			catch (PatternSyntaxException ex) {
				log.warn("Invalid SSRF blocked-host-pattern '{}': {}", rawPattern, ex.getMessage());
			}
		}
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void rejectPrivate(String url, String host, String rangeDescription) {
		throw new SsrfProtectionException(
				"URL '" + url + "' is blocked: host '" + host + "' resolves to a " + rangeDescription + " address. "
						+ "Add this host to 'spring.boot.admin.ssrf-protection.allowed-hosts' to permit it, "
						+ "or disable protection with 'spring.boot.admin.ssrf-protection.enabled=false'.");
	}

}
