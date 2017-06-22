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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ApplicationOperationsTest {
    private WebClient webClient = mock(WebClient.class, withSettings().defaultAnswer(a -> {
        if (a.getMethod().getReturnType().equals(Mono.class)) {
            return Mono.empty();
        }
        return a.getMock();
    }).extraInterfaces(WebClient.RequestHeadersSpec.class, WebClient.UriSpec.class, WebClient.ResponseSpec.class));

    private HttpHeadersProvider headersProvider = mock(HttpHeadersProvider.class);
    private ApplicationOperations ops = new ApplicationOperations(webClient, headersProvider);
    private final Application application = Application.create(ApplicationId.of("id"))
                                                       .register(Registration.create("test", "http://health")
                                                                             .managementUrl("http://mgmt")
                                                                             .build());

    @Test
    @Ignore("Needs resolving of https://jira.spring.io/browse/SPR-15286")
    public void test_getInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth", "foo:bar");
        when(headersProvider.getHeaders(eq(application))).thenReturn(headers);

        StepVerifier.create(ops.getInfo(application)).verifyComplete();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
        verify(webClient, times(1)).get().uri(uriCaptor.capture())
                                   //  .headers(headersCaptor.capture())
                                   .accept(eq(MediaType.APPLICATION_JSON)).retrieve();

        assertThat(uriCaptor.getValue()).isEqualTo(URI.create("http://mgmt/info"));
        assertThat(headersCaptor.getValue()).containsEntry("auth", singletonList("foo:bar"));
        assertThat(headersCaptor.getValue()).containsEntry("Accept", singletonList(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Ignore("Needs resolving of https://jira.spring.io/browse/SPR-15286")
    public void test_getHealth() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth", "foo:bar");
        when(headersProvider.getHeaders(eq(application))).thenReturn(headers);

        StepVerifier.create(ops.getHealth(application)).verifyComplete();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpHeaders> headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);

        verify(webClient, times(1)).get().uri(uriCaptor.capture())
                                   //    .headers(headersCaptor.capture())
                                   .accept(eq(MediaType.APPLICATION_JSON)).retrieve();

        assertThat(uriCaptor.getValue()).isEqualTo(URI.create("http://health"));
        assertThat(headersCaptor.getValue()).containsEntry("auth", singletonList("foo:bar"));
        assertThat(headersCaptor.getValue()).containsEntry("Accept", singletonList(MediaType.APPLICATION_JSON_VALUE));
    }
}
