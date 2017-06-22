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

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicAuthHttpHeaderProviderTest {
    private BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

    @Test
    public void test_auth_header() {
        Registration registration = Registration.create("foo", "http://health")
                                                .metadata("user.name", "test")
                                                .metadata("user.password", "drowssap")
                                                .build();
        Application application = Application.create(ApplicationId.of("id")).register(registration);
        assertThat(headersProvider.getHeaders(application).get(HttpHeaders.AUTHORIZATION)).containsOnly(
                "Basic dGVzdDpkcm93c3NhcA==");
    }

    @Test
    public void test_no_header() {
        Registration registration = Registration.create("foo", "http://health").build();
        Application application = Application.create(ApplicationId.of("id")).register(registration);
        assertThat(headersProvider.getHeaders(application)).isEmpty();
    }
}
