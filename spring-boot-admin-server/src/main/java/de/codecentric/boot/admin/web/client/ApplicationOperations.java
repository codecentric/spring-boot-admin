/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.web.client;

import de.codecentric.boot.admin.model.Application;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles all rest operations invoked on a registered application.
 *
 * @author Johannes Edmeier
 */
public class ApplicationOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOperations.class);
    @SuppressWarnings("unchecked")
    private static final Class<Map<String, Serializable>> RESPONSE_TYPE_MAP = (Class<Map<String, Serializable>>) (Class<?>) Map.class;
    private final RestTemplate restTemplate;
    private final HttpHeadersProvider httpHeadersProvider;

    public ApplicationOperations(RestTemplate restTemplate, HttpHeadersProvider httpHeadersProvider) {
        this.restTemplate = restTemplate;
        this.httpHeadersProvider = httpHeadersProvider;
    }

    public ResponseEntity<Map<String, Serializable>> getInfo(Application application) {
        URI uri = UriComponentsBuilder.fromHttpUrl(application.getManagementUrl()).pathSegment("info").build().toUri();
        return doGet(application, uri, RESPONSE_TYPE_MAP);
    }

    public ResponseEntity<Map<String, Serializable>> getHealth(Application application) {
        URI uri = UriComponentsBuilder.fromHttpUrl(application.getHealthUrl()).build().toUri();
        return doGet(application, uri, RESPONSE_TYPE_MAP);
    }

    protected <T> ResponseEntity<T> doGet(Application application, URI uri, Class<T> responseType) {
        LOGGER.debug("Fetching '{}' for {}", uri, application);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.putAll(httpHeadersProvider.getHeaders(application));

        ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<Void>(headers),
                responseType);

        LOGGER.debug("'{}' responded with {}", uri, response);
        return response;
    }
}
