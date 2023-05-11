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

package de.codecentric.boot.admin.server.ui.extensions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

public class UiRoutesScanner {

	private static final Logger log = LoggerFactory.getLogger(UiRoutesScanner.class);

	private final ResourcePatternResolver resolver;

	public UiRoutesScanner(ResourcePatternResolver resolver) {
		this.resolver = resolver;
	}

	public List<String> scan(String... locations) throws IOException {
		List<String> routes = new ArrayList<>();
		for (String location : locations) {
			for (Resource resource : this.resolver.getResources(toPattern(location) + "**/routes.txt")) {
				if (resource.isReadable()) {
					routes.addAll(readLines(resource.getInputStream()));
				}
			}
		}
		return routes;
	}

	private List<String> readLines(InputStream input) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			return buffer.lines().map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
		}
		catch (IOException ex) {
			log.warn("Couldn't read routes from", ex);
			return Collections.emptyList();
		}
	}

	private String toPattern(String location) {
		// replace the classpath pattern to search all locations and not just the first
		return location.replace("classpath:", "classpath*:");
	}

}
