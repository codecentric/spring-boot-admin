/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.ui;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractAdminUiApplicationTest {
    private WebTestClient webClient;

    protected void setUp(int port) {
        this.webClient = createWebClient(port);
    }

    @Test
    public void should_return_index() {
        //@formatter:off
        this.webClient.get()
                      .uri("/")
                      .accept(MediaType.TEXT_HTML, MediaType.ALL)
                      .exchange()
                      .expectStatus().isOk()
                      .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                      .expectBody(String.class)
                        .value(body -> assertThat(body).contains("<title>Spring Boot Admin</title>"));
        //@formatter:on
    }

    @Test
    public void should_return_api() {
        //@formatter:off
        this.webClient.get()
                      .uri("/applications")
                      .accept(MediaType.APPLICATION_JSON)
                      .exchange()
                      .expectStatus().isOk()
                      .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                      .expectBody(String.class)
                        .value(body -> assertThat(body).contains("[]"));
        //@formatter:on
    }

    @Test
    public void should_return_index_for_ui_path() {
        //@formatter:off
        this.webClient.get()
                      .uri("/applications")
                      .accept(MediaType.TEXT_HTML)
                      .exchange()
                      .expectStatus().isOk()
                      .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                      .expectBody(String.class)
                        .value(body -> assertThat(body).contains("<title>Spring Boot Admin</title>"));
        //@formatter:on
    }

    @Test
    public void should_return_404_for_unknown_path() {
        //@formatter:off
        this.webClient.get()
                      .uri("/unknown")
                      .accept(MediaType.TEXT_HTML)
                      .exchange()
                      .expectStatus().isNotFound();
        //@formatter:on
    }

    protected WebTestClient createWebClient(int port) {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }
}
