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

package de.codecentric.boot.admin.server.web.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.utils.HostPatternMatcher;

/**
 * Provides Basic Auth headers for the {@link Instance} using the metadata for "user.name"
 * and "user.password".
 *
 * Other allowed key names: - "user-name" / "user-password" - "username" / "userpassword"
 *
 * <p>
 * Server-managed credentials (per-service credentials from the service map and the
 * default credentials) can be bound to a set of allowed hosts. Use
 * {@link #isAllowedFor(Instance, URI)} to check whether the credentials selected for an
 * instance may be sent to a given URL.
 *
 * @author Johannes Edmeier
 */
public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

	private static final String[] USERNAME_KEYS = { "user.name", "user-name", "username" };

	private static final String[] PASSWORD_KEYS = { "user.password", "user-password", "userpassword" };

	@Nullable private final String defaultUserName;

	@Nullable private final String defaultPassword;

	private final Map<String, InstanceCredentials> serviceMap;

	private final HostPatternMatcher defaultAllowedHosts;

	private final Map<String, HostPatternMatcher> serviceAllowedHosts;

	public BasicAuthHttpHeaderProvider(@Nullable String defaultUserName, @Nullable String defaultPassword,
			Map<String, InstanceCredentials> serviceMap) {
		this(defaultUserName, defaultPassword, serviceMap, Collections.emptyList());
	}

	public BasicAuthHttpHeaderProvider(@Nullable String defaultUserName, @Nullable String defaultPassword,
			Map<String, InstanceCredentials> serviceMap, Collection<String> defaultAllowedHosts) {
		this.defaultUserName = defaultUserName;
		this.defaultPassword = defaultPassword;
		this.serviceMap = serviceMap;
		this.defaultAllowedHosts = HostPatternMatcher.of(defaultAllowedHosts);
		Map<String, HostPatternMatcher> allowedHosts = new HashMap<>();
		serviceMap.forEach(
				(name, credentials) -> allowedHosts.put(name, HostPatternMatcher.of(credentials.getAllowedHosts())));
		this.serviceAllowedHosts = allowedHosts;
	}

	public BasicAuthHttpHeaderProvider() {
		this(null, null, Collections.emptyMap());
	}

	private static @Nullable String getMetadataValue(Instance instance, String[] keys) {
		Map<String, String> metadata = instance.getRegistration().getMetadata();
		for (String key : keys) {
			String value = metadata.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private static String base64Encode(byte[] src) {
		if (src.length == 0) {
			return "";
		}
		byte[] dest = Base64.getEncoder().encode(src);
		return new String(dest, StandardCharsets.UTF_8);
	}

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		HttpHeaders headers = new HttpHeaders();
		Credentials credentials = resolveCredentials(instance);
		if (credentials != null) {
			headers.set(HttpHeaders.AUTHORIZATION, encode(credentials.userName(), credentials.password()));
		}
		return headers;
	}

	/**
	 * Checks whether the credentials selected for the given instance may be sent to the
	 * given URL.
	 * <p>
	 * Credentials supplied by the instance itself via metadata are always permitted.
	 * Server-managed credentials (service map or default credentials) are only permitted
	 * if the host of the URL matches the allowed hosts configured for them. If no allowed
	 * hosts are configured for the selected credentials, they are permitted for any host
	 * to retain backwards compatibility.
	 * @param instance the instance the request is sent for
	 * @param url the final outbound URL
	 * @return {@code true} if the credentials may be sent to the URL
	 */
	public boolean isAllowedFor(Instance instance, URI url) {
		Credentials credentials = resolveCredentials(instance);
		if (credentials == null || credentials.allowedHosts() == null || credentials.allowedHosts().isEmpty()) {
			return true;
		}
		return credentials.allowedHosts().matches(url.getHost());
	}

	/**
	 * Returns the names of the server-managed credentials that are not bound to any
	 * allowed hosts. The special name {@code "default"} denotes the default credentials.
	 * @return the names of the unbound credentials
	 */
	public List<String> getCredentialsWithoutAllowedHosts() {
		List<String> unbound = new ArrayList<>();
		if (StringUtils.hasText(this.defaultUserName) && StringUtils.hasText(this.defaultPassword)
				&& this.defaultAllowedHosts.isEmpty()) {
			unbound.add("default");
		}
		this.serviceAllowedHosts.forEach((name, allowedHosts) -> {
			if (allowedHosts.isEmpty()) {
				unbound.add(name);
			}
		});
		Collections.sort(unbound);
		return unbound;
	}

	private @Nullable Credentials resolveCredentials(Instance instance) {
		String username = getMetadataValue(instance, USERNAME_KEYS);
		String password = getMetadataValue(instance, PASSWORD_KEYS);
		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			return new Credentials(username, password, null);
		}

		String registeredName = instance.getRegistration().getName();
		InstanceCredentials serviceCredentials = this.serviceMap.get(registeredName);
		if (serviceCredentials != null) {
			username = serviceCredentials.getUserName();
			password = serviceCredentials.getUserPassword();
			HostPatternMatcher allowedHosts = this.serviceAllowedHosts.get(registeredName);
			if (allowedHosts == null) {
				allowedHosts = HostPatternMatcher.of(serviceCredentials.getAllowedHosts());
			}
			return hasText(username, password) ? new Credentials(username, password, allowedHosts) : null;
		}

		username = this.defaultUserName;
		password = this.defaultPassword;
		return hasText(username, password) ? new Credentials(username, password, this.defaultAllowedHosts) : null;
	}

	private static boolean hasText(@Nullable String username, @Nullable String password) {
		return StringUtils.hasText(username) && StringUtils.hasText(password);
	}

	protected String encode(String username, String password) {
		String token = base64Encode((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}

	private record Credentials(String userName, String password, @Nullable HostPatternMatcher allowedHosts) {
	}

	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	public static class InstanceCredentials {

		/**
		 * user name for this instance
		 */
		@lombok.NonNull
		private String userName;

		/**
		 * user password for this instance
		 */
		@lombok.NonNull
		private String userPassword;

		/**
		 * Hosts these credentials may be sent to. Supports exact hostnames, exact IP
		 * addresses and leading wildcards (e.g. {@code *.payment.svc.cluster.local}). The
		 * host of the final outbound request URL must match one of these patterns,
		 * otherwise the credentials are not sent. When empty, the credentials are sent to
		 * every instance registered with this name (not recommended).
		 */
		private List<String> allowedHosts = new ArrayList<>();

		public InstanceCredentials(@lombok.NonNull String userName, @lombok.NonNull String userPassword) {
			this(userName, userPassword, new ArrayList<>());
		}

	}

}
