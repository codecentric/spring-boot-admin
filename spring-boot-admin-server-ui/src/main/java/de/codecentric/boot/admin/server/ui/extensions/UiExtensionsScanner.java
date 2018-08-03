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

package de.codecentric.boot.admin.server.ui.extensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class UiExtensionsScanner {
    private final ResourcePatternResolver resolver;

    public UiExtensionsScanner(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    public List<UiExtension> scan(String... locations) throws IOException {
        List<UiExtension> extensions = new ArrayList<>();
        for (String location : locations) {
            for (Resource resource : this.resolver.getResources(toPattern(location))) {
                String resourcePath = this.getResourcePath(location, resource);
                if (resourcePath != null && isExtension(resource)) {
                    extensions.add(new UiExtension(resourcePath, location + resourcePath));
                }
            }
        }
        return extensions;
    }

    private String toPattern(String location) {
        //replace the classpath pattern to search all locations and not just the first
        return location.replace("classpath:", "classpath*:") + "**";
    }

    private boolean isExtension(Resource resource) {
        return resource.isReadable() && resource.getFilename().endsWith(".css") || resource.getFilename().endsWith(".js");
    }

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
