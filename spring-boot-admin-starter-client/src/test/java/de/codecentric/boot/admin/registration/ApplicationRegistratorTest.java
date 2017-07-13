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
package de.codecentric.boot.admin.registration;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationRegistratorTest {

    private ClientProperties client;
    private ApplicationRegistrator registrator;
    private RestTemplate restTemplate;
    private HttpHeaders headers;

    @Before
    public void setup() {
        restTemplate = mock(RestTemplate.class);

        client = new ClientProperties();
        client.setUrl(new String[]{"http://sba:8080", "http://sba2:8080"});

        ApplicationFactory factory = mock(ApplicationFactory.class);
        when(factory.createApplication()).thenReturn(Application.create("AppName")
                                                                .managementUrl("http://localhost:8080/mgmt")
                                                                .healthUrl("http://localhost:8080/health")
                                                                .serviceUrl("http://localhost:8080")
                                                                .build());

        registrator = new ApplicationRegistrator(restTemplate, client, factory);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void register_successful() {
        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenReturn(
                new ResponseEntity<>(Collections.singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).postForEntity("http://sba:8080/api/applications",
                new HttpEntity<>(applicationRef, headers), Map.class);
    }

    @Test
    public void register_failed() {
        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Application.class))).thenThrow(
                new RestClientException("Error"));

        assertThat(registrator.register()).isFalse();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void register_retry() {
        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Application.class))).thenThrow(
                new RestClientException("Error"));
        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenReturn(
                new ResponseEntity<>(Collections.singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void deregister() {
        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenReturn(
                new ResponseEntity<>(Collections.singletonMap("id", "-id-"), HttpStatus.CREATED));
        registrator.register();
        assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
        registrator.deregister();
        assertThat(registrator.getRegisteredId()).isNull();

        verify(restTemplate).delete("http://sba:8080/api/applications/-id-");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void register_multiple() {
        client.setRegisterOnce(false);

        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenReturn(
                new ResponseEntity<>(Collections.singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).postForEntity("http://sba:8080/api/applications",
                new HttpEntity<>(applicationRef, headers), Map.class);
        verify(restTemplate).postForEntity("http://sba2:8080/api/applications",
                new HttpEntity<>(applicationRef, headers), Map.class);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void register_multiple_one_failure() {
        client.setRegisterOnce(false);

        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenReturn(
                new ResponseEntity<>(Collections.singletonMap("id", "-id-"), HttpStatus.CREATED))
                                                                                                 .thenThrow(
                                                                                                         new RestClientException(
                                                                                                                 "Error"));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).postForEntity("http://sba:8080/api/applications",
                new HttpEntity<>(applicationRef, headers), Map.class);
        verify(restTemplate).postForEntity("http://sba2:8080/api/applications",
                new HttpEntity<>(applicationRef, headers), Map.class);
    }

    @Test
    public void register_multiple_all_failures() {
        client.setRegisterOnce(false);

        when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class))).thenThrow(
                new RestClientException("Error"));

        assertThat(registrator.register()).isFalse();
    }
}
