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

package de.codecentric.boot.admin.server.ui.web;

import de.codecentric.boot.admin.server.ui.extensions.UiExtension;
import de.codecentric.boot.admin.server.web.AdminController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

@AdminController
public class UiController {
    private final String adminContextPath;
    private final List<UiExtension> cssExtensions;
    private final List<UiExtension> jsExtensions;
    private final Map<String, Object> uiSettings;

    public UiController(String adminContextPath, String title, String brand, List<UiExtension> uiExtensions) {
        this.adminContextPath = adminContextPath;
        this.uiSettings = new HashMap<>();
        this.uiSettings.put("title", title);
        this.uiSettings.put("brand", brand);
        this.cssExtensions = uiExtensions.stream()
                                         .filter(e -> e.getResourcePath().endsWith(".css"))
                                         .collect(Collectors.toList());
        this.jsExtensions = uiExtensions.stream()
                                        .filter(e -> e.getResourcePath().endsWith(".js"))
                                        .collect(Collectors.toList());
    }

    @ModelAttribute(value = "adminContextPath", binding = false)
    public String getAdminContextPath() {
        return adminContextPath;
    }

    @ModelAttribute(value = "uiSettings", binding = false)
    public Map<String, Object> getUiSettings() {
        return uiSettings;
    }

    @ModelAttribute(value = "cssExtensions", binding = false)
    public List<UiExtension> getCssExtensions() {
        return cssExtensions;
    }

    @ModelAttribute(value = "jsExtensions", binding = false)
    public List<UiExtension> getJsExtensions() {
        return jsExtensions;
    }

    @ModelAttribute(value = "user", binding = false)
    public Map<String, Object> getUiSettings(Principal principal) {
        if (principal != null) {
            return singletonMap("name", principal.getName());
        }
        return emptyMap();
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping(path = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login() {
        return "login";
    }
}
