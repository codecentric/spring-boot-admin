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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import de.codecentric.boot.admin.server.web.AdminController;

@AdminController
public class UiController {
    private final String adminContextPath;

    private final String customLabel;

    private final String customImageSrc;

    public UiController(String adminContextPath, String customLabel, String customImageSrc) {
        this.adminContextPath = adminContextPath;
        this.customLabel = customLabel;
        this.customImageSrc = customImageSrc;
    }

    @ModelAttribute(value = "adminContextPath", binding = false)
    public String getAdminContextPath() {
        return adminContextPath;
    }

    @ModelAttribute(value = "customLabel", binding = false)
    public String getCustomLabel() {
        return customLabel;
    }

    @ModelAttribute(value = "customImageSrc", binding = false)
    public String getCustomImageSrc() {
        return customImageSrc;
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping(path = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login() {
        return "login";
    }

    @GetMapping(path = "/customization", produces = MediaType.TEXT_HTML_VALUE)
    public String customization() {
        return "customization";
    }

}
