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
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.AbstractView;

import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import de.codecentric.boot.admin.server.ui.config.CssColorUtils;
import de.codecentric.boot.admin.server.ui.extensions.UiExtension;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensions;
import de.codecentric.boot.admin.server.web.servlet.AdminControllerHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.util.CollectionUtils.isEmpty;

class UiControllerTest {

	@Test
	void should_use_default_url() throws Exception {
		MockMvc mockMvc = setupController("", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://example/"));
	}

	@Test
	void should_use_path_from_public_url() throws Exception {
		MockMvc mockMvc = setupController("/public", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://example/public/"));
	}

	@Test
	void should_use_host_and_path_from_public_url() throws Exception {
		MockMvc mockMvc = setupController("http://public/public", List.of());

		mockMvc.perform(get("http://example/"))
			.andExpect(status().isOk())
			.andExpect(view().name("index"))
			.andExpect(model().attribute("baseUrl", "http://public/public/"));
	}

	@Test
	void should_use_scheme_host_and_path_from_public_url() throws Exception {
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
	void should_validate_external_views(String label, String url, boolean hasChildren, boolean shouldFail) {
		try {
			UiController.ExternalView externalView = new UiController.ExternalView(label, url, 1, false,
					hasChildren
							? List.of(new UiController.ExternalView("child", "https://urli.com", 1, false, List.of()))
							: List.of());

			setupController("https://mysba.com", List.of(externalView));
		}
		catch (Exception ex) {
			if (!shouldFail) {
				fail("Initialization of External View should have failed");
			}
		}
	}

	@Test
	void should_populate_palette_with_matching_rgb_colors_for_all_grades() {
		AdminServerUiProperties.Palette palette = new AdminServerUiProperties.Palette();
		palette.set50("#010203");
		palette.set100("#111213");
		palette.set200("#212223");
		palette.set300("#313233");
		palette.set400("#414243");
		palette.set500("#515253");
		palette.set600("#616263");
		palette.set700("#717273");
		palette.set800("#818283");
		palette.set900("#919293");

		AdminServerUiProperties.UiTheme theme = new AdminServerUiProperties.UiTheme();
		theme.setPalette(palette);

		UiController.Settings settings = UiController.Settings.builder().theme(theme).build();
		UiController controller = new UiController("", UiExtensions.EMPTY, settings);

		Map<String, String> actualPalette = controller.getPalette();

		assertThat(actualPalette)
			.containsExactlyInAnyOrderEntriesOf(Map.of("rgbColor50", CssColorUtils.hexToRgb(palette.getShade50()),
					"rgbColor100", CssColorUtils.hexToRgb(palette.getShade100()), "rgbColor200",
					CssColorUtils.hexToRgb(palette.getShade200()), "rgbColor300",
					CssColorUtils.hexToRgb(palette.getShade300()), "rgbColor400",
					CssColorUtils.hexToRgb(palette.getShade400()), "rgbColor500",
					CssColorUtils.hexToRgb(palette.getShade500()), "rgbColor600",
					CssColorUtils.hexToRgb(palette.getShade600()), "rgbColor700",
					CssColorUtils.hexToRgb(palette.getShade700()), "rgbColor800",
					CssColorUtils.hexToRgb(palette.getShade800()), "rgbColor900",
					CssColorUtils.hexToRgb(palette.getShade900())));
	}

	@Test
	void should_render_login_view_with_anonymous_user_model() throws Exception {
		UiController.Settings uiSettings = UiController.Settings.builder()
			.theme(new AdminServerUiProperties.UiTheme())
			.build();
		MockMvc mockMvc = setupControllerWithView("", UiExtensions.EMPTY, uiSettings);

		mockMvc.perform(get("http://example/login"))
			.andExpect(status().isOk())
			.andExpect(view().name("login"))
			.andExpect(model().attribute("baseUrl", "http://example/"))
			.andExpect(model().attribute("uiSettings", uiSettings))
			.andExpect(model().attribute("cssExtensions", List.of()))
			.andExpect(model().attribute("jsExtensions", List.of()))
			.andExpect(model().attribute("user", Map.of()));
	}

	@Test
	void should_render_variables_css_with_public_url_and_ui_extensions() throws Exception {
		UiController.Settings uiSettings = UiController.Settings.builder()
			.title("Spring Boot Admin")
			.brand("codecentric")
			.routes(List.of("/applications/**"))
			.rememberMeEnabled(true)
			.theme(new AdminServerUiProperties.UiTheme())
			.build();
		UiExtension cssExtension = new UiExtension("custom/custom.css", "classpath:/META-INF/custom/custom.css");
		UiExtension jsExtension = new UiExtension("custom/custom.js", "classpath:/META-INF/custom/custom.js");
		UiExtension ignoredExtension = new UiExtension("custom/readme.txt", "classpath:/META-INF/custom/readme.txt");
		MockMvc mockMvc = setupControllerWithView("https://public:8443/admin",
				new UiExtensions(List.of(cssExtension, jsExtension, ignoredExtension)), uiSettings);

		mockMvc.perform(get("http://example/variables.css").principal(() -> "jane"))
			.andExpect(status().isOk())
			.andExpect(view().name("variables.css"))
			.andExpect(model().attribute("baseUrl", "https://public:8443/admin/"))
			.andExpect(model().attribute("uiSettings", uiSettings))
			.andExpect(model().attribute("cssExtensions", List.of(cssExtension)))
			.andExpect(model().attribute("jsExtensions", List.of(jsExtension)))
			.andExpect(model().attribute("user", Map.of("name", "jane")));
	}

	private MockMvc setupController(String publicUrl, List<UiController.ExternalView> externalViews) {
		var uiControllerSettings = UiController.Settings.builder().theme(new AdminServerUiProperties.UiTheme());
		if (!isEmpty(externalViews)) {
			uiControllerSettings.externalViews(externalViews);
		}
		return MockMvcBuilders
			.standaloneSetup(new UiController(publicUrl, UiExtensions.EMPTY, uiControllerSettings.build()))
			.setCustomHandlerMapping(() -> new AdminControllerHandlerMapping(""))
			.build();
	}

	private MockMvc setupControllerWithView(String publicUrl, UiExtensions uiExtensions,
			UiController.Settings uiSettings) {
		return MockMvcBuilders.standaloneSetup(new UiController(publicUrl, uiExtensions, uiSettings))
			.setCustomHandlerMapping(() -> new AdminControllerHandlerMapping(""))
			.setSingleView(new NoOpView())
			.build();
	}

	private static final class NoOpView extends AbstractView {

		@Override
		protected void renderMergedOutputModel(@NonNull Map<String, Object> model, @NonNull HttpServletRequest request,
				@NonNull HttpServletResponse response) {
			// no-op: model attribute assertions don't need rendered output
		}

	}

}
