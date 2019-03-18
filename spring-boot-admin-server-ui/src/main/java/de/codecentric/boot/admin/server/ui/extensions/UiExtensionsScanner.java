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

package de.codecentric.boot.admin.server.ui.extensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class UiExtensionsScanner {
    private static final Logger log = LoggerFactory.getLogger(UiExtensionsScanner.class);
    private final ResourcePatternResolver resolver;

    public UiExtensionsScanner(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    public List<UiExtension> scan(String... locations) throws IOException {
        List<UiExtension> extensions = new ArrayList<>();
        for (String location : locations) {
            for (Resource resource : resolveAssets(location)) {
                String resourcePath = this.getResourcePath(location, resource);
                if (resourcePath != null && resource.isReadable()) {
                    UiExtension extension = new UiExtension(resourcePath, location + resourcePath);
                    log.debug("Found UiExtension {}", extension);
                    extensions.add(extension);
                }
            }
        }
        return extensions;
    }

    private List<Resource> resolveAssets(String location) throws IOException {
        String widerLocation = toPattern(location);
        return Stream.concat(
            Arrays.stream(this.resolver.getResources(widerLocation + "**/*.js")),
            Arrays.stream(this.resolver.getResources(widerLocation + "**/*.css"))
        ).collect(Collectors.toList());
    }

    private String toPattern(String location) {
        //replace the classpath pattern to search all locations and not just the first
        return location.replace("classpath:", "classpath*:");
    }

    @Nullable
    private String getResourcePath(String location, Resource resource) throws IOException {
        String locationWithouPrefix = location.replaceFirst("^[^:]+:", "");
        Matcher m = Pattern.compile(Pattern.quote(locationWithouPrefix) + "(.+)$")
                           .matcher(resource.getURI().toString());
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }
}
