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

package de.codecentric.boot.admin.server.ui.web;

import de.codecentric.boot.admin.server.ui.extensions.UiExtension;
import de.codecentric.boot.admin.server.web.AdminController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

@AdminController
public class UiController {
    private final String publicUrl;
    private final List<UiExtension> cssExtensions;
    private final List<UiExtension> jsExtensions;
    private final Settings uiSettings;

    public UiController(String publicUrl, List<UiExtension> uiExtensions, Settings uiSettings) {
        this.publicUrl = publicUrl;
        this.uiSettings = uiSettings;
        this.cssExtensions = uiExtensions.stream()
                                         .filter(e -> e.getResourcePath().endsWith(".css"))
                                         .collect(Collectors.toList());
        this.jsExtensions = uiExtensions.stream()
                                        .filter(e -> e.getResourcePath().endsWith(".js"))
                                        .collect(Collectors.toList());
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
        return this.cssExtensions;
    }

    @ModelAttribute(value = "jsExtensions", binding = false)
    public List<UiExtension> getJsExtensions() {
        return this.jsExtensions;
    }

    @ModelAttribute(value = "user", binding = false)
    public Map<String, Object> getUser(@Nullable Principal principal) {
        if (principal != null) {
            return singletonMap("name", principal.getName());
        }
        return emptyMap();
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping(path = "/sba-settings.js", produces = MediaType.APPLICATION_JSON_VALUE)
    public String sbaSettings() {
        return "sba-settings.js";
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
        private final String favicon;
        private final String faviconDanger;
        private final boolean notificationFilterEnabled;
        private final List<String> routes;
        private final List<ExternalView> externalViews;
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

        public ExternalView(String label, String url, Integer order, boolean iframe) {
            Assert.hasText(label, "'label' must not be empty");
            Assert.hasText(url, "'url' must not be empty");
            this.label = label;
            this.url = url;
            this.order = order;
            this.iframe = iframe;
        }
    }
}
