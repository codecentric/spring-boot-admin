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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BlockingRegistrationClient implements RegistrationClient {

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private final RestTemplate restTemplate;

	public BlockingRegistrationClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public Optional<String> register(String adminUrl, Application application) {
		ResponseEntity<Map<String, Object>> response = this.restTemplate.exchange(adminUrl, HttpMethod.POST,
				new HttpEntity<>(application, this.createRequestHeaders()), RESPONSE_TYPE);
		return Optional.ofNullable(response.getBody()).map(body -> body.get("id")).map(Object::toString);
	}

	@Override
	public void deregister(String adminUrl, String id) {
		this.restTemplate.delete(adminUrl + '/' + id);
	}

	protected HttpHeaders createRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		return HttpHeaders.readOnlyHttpHeaders(headers);
	}

}
