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

package de.codecentric.boot.admin.server.ui;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
					.value((body) -> assertThat(body).contains("<title>Spring Boot Admin</title>"));
		//@formatter:on
	}

	@Test
	public void should_not_return_index_for_logfile() {
		//@formatter:off
		this.webClient.get()
						.uri("/instances/a973ff14be49/actuator/logfile")
						.accept(MediaType.TEXT_HTML, MediaType.ALL)
						.exchange()
						.expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
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
					.value((body) -> assertThat(body).contains("[]"));
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
					.value((body) -> assertThat(body).contains("<title>Spring Boot Admin</title>"));
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

	@Test
	public void should_return_correct_content_type_for_js_extensions() {
		//@formatter:off
		this.webClient.get()
					.uri("/extensions/custom/custom.abcdef.js")
					.header("Accept", "*/*")
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentType(getExpectedMediaTypeForJavaScript());
		//@formatter:on
	}

	abstract MediaType getExpectedMediaTypeForJavaScript();

	@Test
	public void should_return_correct_content_type_for_css_extensions() {
		//@formatter:off
		this.webClient.get()
					.uri("/extensions/custom/custom.abcdef.css")
					.header("Accept", "text/css,*/*;q=0.1")
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentType(MediaType.parseMediaType("text/css"));
		//@formatter:on
	}

	@Test
	public void should_contain_only_one_language() {
		//@formatter:off
		this.webClient.get()
					.uri("/sba-settings.js")
					.accept(MediaType.ALL)
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentTypeCompatibleWith("application/javascript")
					.expectBody(String.class)
					.value((body) -> assertThat(body).contains("\"availableLanguages\":[\"de\"]"));
		//@formatter:on
	}

	@Test
	public void should_return_defaults_for_pollTimers() {
		//@formatter:off
		this.webClient.get()
			.uri("/sba-settings.js")
			.accept(MediaType.ALL)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith("application/javascript")
			.expectBody(String.class)
			.value((body) -> assertThat(body).contains("\"pollTimer\""))
			.value((body) -> assertThat(body).contains("\"cache\":2500"));
		//@formatter:on
	}

	@Test
	public void should_render_palette_colors_in_variables_css() {
		//@formatter:off
		this.webClient.get()
					.uri("/variables.css")
					.accept(MediaType.parseMediaType("text/css"), MediaType.ALL)
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentTypeCompatibleWith(MediaType.parseMediaType("text/css"))
					.expectBody(String.class)
					.value((body) -> assertThat(body).contains("--main-50: rgb(238, 252, 250)"))
					.value((body) -> assertThat(body).contains("--main-100: rgb(217, 247, 244)"))
					.value((body) -> assertThat(body).contains("--main-200: rgb(183, 240, 234)"))
					.value((body) -> assertThat(body).contains("--main-300: rgb(145, 232, 224)"))
					.value((body) -> assertThat(body).contains("--main-400: rgb(107, 224, 213)"))
					.value((body) -> assertThat(body).contains("--main-500: rgb(71, 217, 203)"))
					.value((body) -> assertThat(body).contains("--main-600: rgb(39, 190, 175)"))
					.value((body) -> assertThat(body).contains("--main-700: rgb(30, 144, 132)"))
					.value((body) -> assertThat(body).contains("--main-800: rgb(20, 97, 90)"))
					.value((body) -> assertThat(body).contains("--main-900: rgb(10, 47, 43)"))
					.value((body) -> assertThat(body).contains("--bg-color-start: #91E8E0"))
					.value((body) -> assertThat(body).contains("--bg-color-stop: #1E9084"));
		//@formatter:on
	}

	protected WebTestClient createWebClient(int port) {
		return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

}
