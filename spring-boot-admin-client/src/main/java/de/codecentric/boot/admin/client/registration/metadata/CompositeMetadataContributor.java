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

package de.codecentric.boot.admin.client.registration.metadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositeMetadataContributor implements MetadataContributor {
    private final List<MetadataContributor> delegates;

    public CompositeMetadataContributor(List<MetadataContributor> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        delegates.forEach(delegate -> metadata.putAll(delegate.getMetadata()));
        return metadata;
    }
}
