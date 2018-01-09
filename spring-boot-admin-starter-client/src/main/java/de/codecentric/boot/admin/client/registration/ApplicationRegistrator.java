/*
 * Copyright 2014-2018 the original author or authors.
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
package de.codecentric.boot.admin.client.registration;

import de.codecentric.boot.admin.client.config.ClientProperties;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Registers the client application at spring-boot-admin-server
 */
public class ApplicationRegistrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistrator.class);
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private static final HttpHeaders HTTP_HEADERS = createHttpHeaders();
    private final ConcurrentHashMap<String, LongAdder> attempts = new ConcurrentHashMap<>();
    private final AtomicReference<String> registeredId = new AtomicReference<>();
    private final ClientProperties client;
    private final RestTemplate template;
    private final ApplicationFactory applicationFactory;

    public ApplicationRegistrator(RestTemplate template,
                                  ClientProperties client,
                                  ApplicationFactory applicationFactory) {
        this.client = client;
        this.template = template;
        this.applicationFactory = applicationFactory;
    }

    private static HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return HttpHeaders.readOnlyHttpHeaders(headers);
    }

    /**
     * Registers the client application at spring-boot-admin-server.
     *
     * @return true if successful registration on at least one admin server
     */
    public boolean register() {
        Application self = createApplication();
        boolean isRegistrationSuccessful = false;
        for (String adminUrl : client.getAdminUrl()) {
            LongAdder attempt = this.attempts.computeIfAbsent(adminUrl, k -> new LongAdder());
            boolean successful = register(self, adminUrl, attempt.intValue() == 0);

            if (!successful) {
                attempt.increment();
            } else {
                attempt.reset();
                isRegistrationSuccessful = true;
                if (client.isRegisterOnce()) {
                    break;
                }
            }
        }

        return isRegistrationSuccessful;
    }

    protected boolean register(Application self, String adminUrl, boolean firstAttempt) {
        try {
            ResponseEntity<Map<String, Object>> response = template.exchange(adminUrl, HttpMethod.POST,
                    new HttpEntity<>(self, HTTP_HEADERS), RESPONSE_TYPE);

            if (response.getStatusCode().is2xxSuccessful()) {
                if (registeredId.compareAndSet(null, response.getBody().get("id").toString())) {
                    LOGGER.info("Application registered itself as {}", response.getBody().get("id").toString());
                } else {
                    LOGGER.debug("Application refreshed itself as {}", response.getBody().get("id").toString());
                }
                return true;
            } else {
                if (firstAttempt) {
                    LOGGER.warn(
                            "Application failed to registered itself as {}. Response: {}. Further attempts are logged on DEBUG level",
                            self, response.toString());
                } else {
                    LOGGER.debug("Application failed to registered itself as {}. Response: {}", self,
                            response.toString());
                }
            }
        } catch (Exception ex) {
            if (firstAttempt) {
                LOGGER.warn(
                        "Failed to register application as {} at spring-boot-admin ({}): {}. Further attempts are logged on DEBUG level",
                        self, client.getAdminUrl(), ex.getMessage());
            } else {
                LOGGER.debug("Failed to register application as {} at spring-boot-admin ({}): {}", self,
                        client.getAdminUrl(), ex.getMessage());
            }

        }
        return false;
    }

    public void deregister() {
        String id = registeredId.get();
        if (id != null) {
            for (String adminUrl : client.getAdminUrl()) {
                try {
                    template.delete(adminUrl + "/" + id);
                    registeredId.compareAndSet(id, null);
                    if (client.isRegisterOnce()) {
                        break;
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Failed to deregister application (id={}) at spring-boot-admin ({}): {}", id, adminUrl,
                            ex.getMessage());
                }
            }
        }
    }

    /**
     * @return the id of this client as given by the admin server.
     * Returns null if the client has not registered against the admin server yet.
     */
    public String getRegisteredId() {
        return registeredId.get();
    }

    protected Application createApplication() {
        return applicationFactory.createApplication();
    }
}
