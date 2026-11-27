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

import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * Matches hostnames against a list of allowed host patterns.
 *
 * <p>
 * Supported patterns:
 * <ul>
 * <li>Exact hostnames, e.g. {@code payment.internal.example.com}</li>
 * <li>Wildcard subdomains, e.g. {@code *.example.com}. The wildcard matches one or more
 * labels, so {@code a.example.com} and {@code a.b.example.com} match, but
 * {@code example.com} itself does not.</li>
 * <li>Exact IP literals, e.g. {@code 10.0.0.5} or {@code [fd00::1]}. Wildcards never
 * match IP literals.</li>
 * </ul>
 *
 * <p>
 * Hosts and patterns are normalized before comparison: lower-cased, trailing dots
 * removed, internationalized domain names converted to their ASCII (punycode) form and IP
 * literals converted to their canonical representation.
 */
public final class HostPatternMatcher {

	private static final Pattern IPV4_LITERAL = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

	private static final HostPatternMatcher EMPTY = new HostPatternMatcher(Collections.emptySet(),
			Collections.emptyList());

	private final Set<String> exactHosts;

	private final List<String> wildcardSuffixes;

	private HostPatternMatcher(Set<String> exactHosts, List<String> wildcardSuffixes) {
		this.exactHosts = exactHosts;
		this.wildcardSuffixes = wildcardSuffixes;
	}

	/**
	 * Creates a matcher for the given patterns.
	 * @param patterns the host patterns (may be {@code null} or empty)
	 * @return the matcher
	 * @throws IllegalArgumentException if a pattern is invalid
	 */
	public static HostPatternMatcher of(@Nullable Collection<String> patterns) {
		if (patterns == null || patterns.isEmpty()) {
			return EMPTY;
		}
		Set<String> exactHosts = new HashSet<>();
		List<String> wildcardSuffixes = new ArrayList<>();
		for (String rawPattern : patterns) {
			if (!StringUtils.hasText(rawPattern)) {
				continue;
			}
			String pattern = rawPattern.trim();
			if (pattern.startsWith("*.")) {
				String domain = normalize(pattern.substring(2));
				if (domain == null || domain.contains("*") || isIpLiteral(domain)) {
					throw new IllegalArgumentException("Invalid host pattern '" + rawPattern + "'");
				}
				wildcardSuffixes.add("." + domain);
			}
			else {
				String host = normalize(pattern);
				if (host == null || host.contains("*")) {
					throw new IllegalArgumentException("Invalid host pattern '" + rawPattern
							+ "'. Only exact hosts and leading wildcards ('*.example.com') are supported.");
				}
				exactHosts.add(host);
			}
		}
		if (exactHosts.isEmpty() && wildcardSuffixes.isEmpty()) {
			return EMPTY;
		}
		return new HostPatternMatcher(exactHosts, wildcardSuffixes);
	}

	/**
	 * Returns whether no patterns are configured.
	 * @return {@code true} if no patterns are configured
	 */
	public boolean isEmpty() {
		return this.exactHosts.isEmpty() && this.wildcardSuffixes.isEmpty();
	}

	/**
	 * Returns whether the given host matches one of the configured patterns.
	 * @param host the host to check, as returned by {@link java.net.URI#getHost()}
	 * @return {@code true} if the host matches
	 */
	public boolean matches(@Nullable String host) {
		String normalized = normalize(host);
		if (normalized == null) {
			return false;
		}
		if (this.exactHosts.contains(normalized)) {
			return true;
		}
		if (isIpLiteral(normalized)) {
			return false;
		}
		for (String suffix : this.wildcardSuffixes) {
			if (normalized.length() > suffix.length() && normalized.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> patterns = new ArrayList<>(this.exactHosts);
		this.wildcardSuffixes.forEach((suffix) -> patterns.add("*" + suffix));
		return patterns.toString();
	}

	private static @Nullable String normalize(@Nullable String host) {
		if (!StringUtils.hasText(host)) {
			return null;
		}
		String value = host.trim();
		if (value.startsWith("[") && value.endsWith("]")) {
			value = value.substring(1, value.length() - 1);
		}
		while (value.endsWith(".")) {
			value = value.substring(0, value.length() - 1);
		}
		if (value.isEmpty()) {
			return null;
		}
		value = value.toLowerCase(Locale.ROOT);
		if (isIpLiteral(value)) {
			return canonicalizeIpLiteral(value);
		}
		try {
			return IDN.toASCII(value, IDN.ALLOW_UNASSIGNED).toLowerCase(Locale.ROOT);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static boolean isIpLiteral(String value) {
		return value.indexOf(':') >= 0 || IPV4_LITERAL.matcher(value).matches();
	}

	private static @Nullable String canonicalizeIpLiteral(String value) {
		if (value.indexOf(':') < 0) {
			String[] octets = value.split("\\.");
			StringBuilder canonical = new StringBuilder();
			for (String octet : octets) {
				if (octet.length() > 1 && octet.charAt(0) == '0') {
					// Leading zeros are interpreted as octal by some resolvers; reject
					// them.
					return null;
				}
				int number = Integer.parseInt(octet);
				if (number > 255) {
					return null;
				}
				if (!canonical.isEmpty()) {
					canonical.append('.');
				}
				canonical.append(number);
			}
			return canonical.toString();
		}
		try {
			// IPv6 literals are parsed without a DNS lookup.
			return InetAddress.getByName(value).getHostAddress();
		}
		catch (UnknownHostException | IllegalArgumentException ex) {
			return null;
		}
	}

}
