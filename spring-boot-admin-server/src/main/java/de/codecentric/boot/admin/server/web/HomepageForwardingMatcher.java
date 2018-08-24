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

package de.codecentric.boot.admin.server.web;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the matching conditions for when a request is redirected to the home page.
 */
public class HomepageForwardingMatcher {
    private static final List<String> DEFAULT_IGNORED_PATHS = Arrays.asList("/login", "/**/actuator/**");

    private final String homepagePath;
    private final List<String> ignoredPaths;
    private PathMatcher pathMatcher = new AntPathMatcher();

    public HomepageForwardingMatcher(String adminContextPath) {
        this.homepagePath = StringUtils.hasText(adminContextPath) ? PathUtils.normalizePath(adminContextPath) : "/";
        this.ignoredPaths = DEFAULT_IGNORED_PATHS.stream().map(path -> (homepagePath + path).replaceAll("/+", "/"))
            .collect(Collectors.toList());
    }

    public String getHomepagePath() {
        return homepagePath;
    }

    public boolean match(HttpMethod method, String path, List<MediaType> acceptHeaderTypes) {
        if (!HttpMethod.GET.equals(method) ||
            homepagePath.equalsIgnoreCase(path) ||
            ignoredPaths.stream().anyMatch(ignoredPath -> pathMatcher.match(ignoredPath, path))) {
            return false;
        }

        return acceptHeaderTypes.contains(MediaType.TEXT_HTML);
    }
}
