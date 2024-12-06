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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.server.domain.entities.Instance;

/**
 * Provides Basic Auth headers for the {@link Instance} using the metadata for "user.name"
 * and "user.password".
 *
 * Other allowed key names: - "user-name" / "user-password" - "username" / "userpassword"
 *
 * @author Johannes Edmeier
 */
public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

	private static final String[] USERNAME_KEYS = { "user.name", "user-name", "username" };

	private static final String[] PASSWORD_KEYS = { "user.password", "user-password", "userpassword" };

	@Nullable
	private final String defaultUserName;

	@Nullable
	private final String defaultPassword;

	private final Map<String, InstanceCredentials> serviceMap;

	public BasicAuthHttpHeaderProvider(@Nullable String defaultUserName, @Nullable String defaultPassword,
			Map<String, InstanceCredentials> serviceMap) {
		this.defaultUserName = defaultUserName;
		this.defaultPassword = defaultPassword;
		this.serviceMap = serviceMap;
	}

	public BasicAuthHttpHeaderProvider() {
		this(null, null, Collections.emptyMap());
	}

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		String username = getMetadataValue(instance, USERNAME_KEYS);
		String password = getMetadataValue(instance, PASSWORD_KEYS);

		if (!(StringUtils.hasText(username) && StringUtils.hasText(password))) {
			String registeredName = instance.getRegistration().getName();
			InstanceCredentials credentials = this.serviceMap.get(registeredName);
			if (credentials != null) {
				username = credentials.getUserName();
				password = credentials.getUserPassword();
			}
			else {
				username = this.defaultUserName;
				password = this.defaultPassword;
			}
		}

		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			headers.set(HttpHeaders.AUTHORIZATION, encode(username, password));
		}
		return headers;
	}

	protected String encode(String username, String password) {
		String token = base64Encode((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
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

	}

}
