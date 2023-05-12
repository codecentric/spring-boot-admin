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

package de.codecentric.boot.admin.client.registration;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class ReactiveRegistrationClient implements RegistrationClient {

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private final WebClient webclient;

	private final Duration timeout;

	public ReactiveRegistrationClient(WebClient webclient, Duration timeout) {
		this.webclient = webclient;
		this.timeout = timeout;
	}

	@Override
	public String register(String adminUrl, Application application) {
		Map<String, Object> response = this.webclient.post()
			.uri(adminUrl)
			.headers(this::setRequestHeaders)
			.bodyValue(application)
			.retrieve()
			.bodyToMono(RESPONSE_TYPE)
			.timeout(this.timeout)
			.block();
		return response.get("id").toString();
	}

	@Override
	public void deregister(String adminUrl, String id) {
		this.webclient.delete().uri(adminUrl + '/' + id).retrieve().toBodilessEntity().timeout(this.timeout).block();
	}

	protected void setRequestHeaders(HttpHeaders headers) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	}

}
