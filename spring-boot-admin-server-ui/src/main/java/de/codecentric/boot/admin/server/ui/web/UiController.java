/*
 * Copyright 2014-2024 the original author or authors.
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

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.PollTimer;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties.UiTheme;
import de.codecentric.boot.admin.server.ui.extensions.UiExtension;
import de.codecentric.boot.admin.server.ui.extensions.UiExtensions;
import de.codecentric.boot.admin.server.web.AdminController;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.springframework.util.CollectionUtils.isEmpty;

@AdminController
public class UiController {

	private final String publicUrl;

	private final UiExtensions uiExtensions;

	private final Settings uiSettings;

	public UiController(String publicUrl, UiExtensions uiExtensions, Settings uiSettings) {
		this.publicUrl = publicUrl;
		this.uiExtensions = uiExtensions;
		this.uiSettings = uiSettings;
	}

	@ModelAttribute(value = "baseUrl", binding = false)
	public String getBaseUrl(UriComponentsBuilder uriBuilder) {
		UriComponents publicComponents = UriComponentsBuilder.fromUriString(this.publicUrl).build();
		if (publicComponents.getScheme() != null) {
			uriBuilder.scheme(publicComponents.getScheme());
		}
		if (publicComponents.getHost() != null) {
			uriBuilder.host(publicComponents.getHost());
		}
		if (publicComponents.getPort() != -1) {
			uriBuilder.port(publicComponents.getPort());
		}
		if (publicComponents.getPath() != null) {
			uriBuilder.path(publicComponents.getPath());
		}
		return uriBuilder.path("/").toUriString();
	}

	@ModelAttribute(value = "uiSettings", binding = false)
	public Settings getUiSettings() {
		return this.uiSettings;
	}

	@ModelAttribute(value = "cssExtensions", binding = false)
	public List<UiExtension> getCssExtensions() {
		return this.uiExtensions.getCssExtensions();
	}

	@ModelAttribute(value = "jsExtensions", binding = false)
	public List<UiExtension> getJsExtensions() {
		return this.uiExtensions.getJsExtensions();
	}

	@ModelAttribute(value = "user", binding = false)
	public Map<String, Object> getUser(@Nullable Principal principal) {
		if (principal != null) {
			return singletonMap("name", principal.getName());
		}
		return emptyMap();
	}

	@GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
	@RegisterReflectionForBinding(String.class)
	public String index() {
		return "index";
	}

	@GetMapping(path = "/sba-settings.js", produces = "application/javascript")
	public String sbaSettings() {
		return "sba-settings.js";
	}

	@GetMapping(path = "/variables.css", produces = "text/css")
	public String variablesCss() {
		return "variables.css";
	}

	@GetMapping(path = "/login", produces = MediaType.TEXT_HTML_VALUE)
	public String login() {
		return "login";
	}

	@lombok.Data
	@lombok.Builder
	public static class Settings {

		private final String title;

		private final String brand;

		private final String loginIcon;

		private final String favicon;

		private final String faviconDanger;

		private final PollTimer pollTimer;

		private final UiTheme theme;

		private final boolean notificationFilterEnabled;

		private final boolean rememberMeEnabled;

		private final List<String> availableLanguages;

		private final List<String> routes;

		private final List<ExternalView> externalViews;

		private final List<ViewSettings> viewSettings;

		private final Boolean enableToasts;

		private final Boolean hideInstanceUrl;

	}

	@lombok.Data
	@JsonInclude(Include.NON_EMPTY)
	public static class ExternalView {

		/**
		 * Label to be shown in the navbar.
		 */
		private final String label;

		/**
		 * Url for the external view to be linked
		 */
		private final String url;

		/**
		 * Order in the navbar.
		 */
		private final Integer order;

		/**
		 * Should the page shown as an iframe or open in a new window.
		 */
		private final boolean iframe;

		/**
		 * A list of child views.
		 */
		private final List<ExternalView> children;

		public ExternalView(String label, String url, Integer order, boolean iframe, List<ExternalView> children) {
			Assert.hasText(label, "'label' must not be empty");
			if (isEmpty(children)) {
				Assert.hasText(url, "'url' must not be empty");
			}
			this.label = label;
			this.url = url;
			this.order = order;
			this.iframe = iframe;
			this.children = children;
		}

	}

	@lombok.Data
	@JsonInclude(Include.NON_EMPTY)
	public static class ViewSettings {

		/**
		 * Name of the view to address.
		 */
		private final String name;

		/**
		 * Set view enabled.
		 */
		private boolean enabled;

		public ViewSettings(String name, boolean enabled) {
			Assert.hasText(name, "'name' must not be empty");
			this.name = name;
			this.enabled = enabled;
		}

	}

}
