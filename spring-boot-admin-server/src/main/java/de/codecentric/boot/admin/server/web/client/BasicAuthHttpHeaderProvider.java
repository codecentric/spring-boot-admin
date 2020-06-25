/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.client;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.server.domain.entities.Instance;

/**
 * Provides Basic Auth headers for the {@link Instance} using the metadata for "user.name"
 * and "user.password".
 *
 * Other allowed key names:
 * 	- "user-name" / "user-password"
 * 	- "username" / "userpassword"
 *
 * @author Johannes Edmeier
 */
public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

	private static final String USERNAME_KEY = "user{separator}name";
	private static final String PASSWORD_KEY = "user{separator}password";
	private static final String[] ALLOWED_SEPARATORS = {".", "-", ""};

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		HttpHeaders headers = new HttpHeaders();
		// add basic auth header if credentials are found in the instance's metadata
		Optional<String> basicAuth = buildEncodedBasicAuth(instance);
		basicAuth.ifPresent(auth -> headers.set(HttpHeaders.AUTHORIZATION, auth));
		return headers;
	}

	protected String encode(String username, String password) {
		String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}

	/**
	 * Return B64 encoded auth if any valid credential keys are found in the instance metadata.
	 * The leniency on the keys is due to Consul not allowing dots (".") in a key name.
	 */
	private Optional<String> buildEncodedBasicAuth(Instance instance) {
		for (String separator : ALLOWED_SEPARATORS) {
			String username = getUsername(instance, separator);
			String password = getPassword(instance, separator);
			if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
				return Optional.of(encode(username, password));
			}
		}
		return Optional.empty();
	}

	private static String getUsername(Instance instance, String separator) {
		return instance.getRegistration().getMetadata().get(USERNAME_KEY.replace("{separator}", separator));
	}

	private static String getPassword(Instance instance, String separator) {
		return instance.getRegistration().getMetadata().get(PASSWORD_KEY.replace("{separator}", separator));
	}

}
