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

package de.codecentric.boot.admin.server.ui.web;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.codecentric.boot.admin.server.ui.extensions.UiExtensions;
import de.codecentric.boot.admin.server.web.servlet.AdminControllerHandlerMapping;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.util.CollectionUtils.isEmpty;

public class UiControllerTest {

	@Test
	public void should_use_default_url() throws Exception {
		MockMvc mockMvc = setupController("", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://example/"));
	}

	@Test
	public void should_use_path_from_public_url() throws Exception {
		MockMvc mockMvc = setupController("/public", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://example/public/"));
	}

	@Test
	public void should_use_host_and_path_from_public_url() throws Exception {
		MockMvc mockMvc = setupController("http://public/public", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://public/public/"));
	}

	@Test
	public void should_use_scheme_host_and_path_from_public_url() throws Exception {
		MockMvc mockMvc = setupController("https://public/public", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "https://public/public/"));
	}

	@ParameterizedTest
	@CsvSource(value = { //
			"link without children with url, https://codecentric.de, false, false", //
			"link without children without url, null, false, true", //
			"link with children, null, true, false" }, //
			nullValues = { "null" })
	public void should_validate_external_views(String label, String url, boolean hasChildren, boolean shouldFail) {
		try {
			UiController.ExternalView externalView = new UiController.ExternalView(label, url, 1, false,
					hasChildren
							? List.of(new UiController.ExternalView("child", "https://urli.com", 1, false, List.of()))
							: List.of());

			setupController("http://mysba.com", List.of(externalView));
		}
		catch (Exception ex) {
			if (!shouldFail) {
				fail("Initialization of External View should have failed");
			}
		}
	}

	private MockMvc setupController(String publicUrl, List<UiController.ExternalView> externalViews) {
		var uiControllerSettings = UiController.Settings.builder();
		if (!isEmpty(externalViews)) {
			uiControllerSettings.externalViews(externalViews);
		}
		return MockMvcBuilders
			.standaloneSetup(new UiController(publicUrl, UiExtensions.EMPTY, uiControllerSettings.build()))
			.setCustomHandlerMapping(() -> new AdminControllerHandlerMapping(""))
			.build();
	}

}
