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
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationRegistratorTest {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
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

    @Test
    public void register_successful() {
        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenReturn(new ResponseEntity<>(singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).exchange(eq("http://sba:8080/instances"), eq(HttpMethod.POST),
            eq(new HttpEntity<>(applicationRef, headers)), eq(RESPONSE_TYPE));
    }


    @Test
    public void register_failed() {
        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenThrow(new RestClientException("Error"));

        assertThat(registrator.register()).isFalse();
    }

    @Test
    public void register_retry() {
        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenThrow(new RestClientException("Error"));
        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenReturn(new ResponseEntity<>(singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();
    }

    @Test
    public void deregister() {
        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenReturn(new ResponseEntity<>(singletonMap("id", "-id-"), HttpStatus.CREATED));
        registrator.register();
        assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
        registrator.deregister();
        assertThat(registrator.getRegisteredId()).isNull();

        verify(restTemplate).delete("http://sba:8080/instances/-id-");
    }

    @Test
    public void register_multiple() {
        client.setRegisterOnce(false);

        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenReturn(new ResponseEntity<>(singletonMap("id", "-id-"), HttpStatus.CREATED));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).exchange(eq("http://sba:8080/instances"), eq(HttpMethod.POST),
            eq(new HttpEntity<>(applicationRef, headers)), eq(RESPONSE_TYPE));
        verify(restTemplate).exchange(eq("http://sba2:8080/instances"), eq(HttpMethod.POST),
            eq(new HttpEntity<>(applicationRef, headers)), eq(RESPONSE_TYPE));
    }

    @Test
    public void register_multiple_one_failure() {
        client.setRegisterOnce(false);

        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenReturn(new ResponseEntity<>(singletonMap("id", "-id-"), HttpStatus.CREATED))
                               .thenThrow(new RestClientException("Error"));

        assertThat(registrator.register()).isTrue();

        Application applicationRef = Application.create("AppName")
                                                .healthUrl("http://localhost:8080/health")
                                                .managementUrl("http://localhost:8080/mgmt")
                                                .serviceUrl("http://localhost:8080")
                                                .build();
        verify(restTemplate).exchange(eq("http://sba:8080/instances"), eq(HttpMethod.POST),
            eq(new HttpEntity<>(applicationRef, headers)), eq(RESPONSE_TYPE));
        verify(restTemplate).exchange(eq("http://sba2:8080/instances"), eq(HttpMethod.POST),
            eq(new HttpEntity<>(applicationRef, headers)), eq(RESPONSE_TYPE));
    }

    @Test
    public void register_multiple_all_failures() {
        client.setRegisterOnce(false);

        when(restTemplate.exchange(isA(String.class), eq(HttpMethod.POST), isA(HttpEntity.class),
            eq(RESPONSE_TYPE))).thenThrow(new RestClientException("Error"));

        assertThat(registrator.register()).isFalse();
    }
}
