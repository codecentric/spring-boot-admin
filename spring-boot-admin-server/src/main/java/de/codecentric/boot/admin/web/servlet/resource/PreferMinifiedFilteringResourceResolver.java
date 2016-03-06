/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.web.servlet.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * {@link ResourceResolver} which is looking for minified version of resources.
 *
 * @author Johannes Edmeier
 */
public class PreferMinifiedFilteringResourceResolver extends AbstractResourceResolver {

	private final String extensionPrefix;

	public PreferMinifiedFilteringResourceResolver(String extensionPrefix) {
		this.extensionPrefix = extensionPrefix;
	}

	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		List<Resource> newLocations = new ArrayList<Resource>(locations.size());

		for (Resource location : locations) {
			Resource minified = findMinified(location);
			newLocations.add(minified != null ? minified : location);
		}

		return chain.resolveResource(request, requestPath, newLocations);
	}

	private Resource findMinified(Resource resource) {
		try {
			String basename = StringUtils.stripFilenameExtension(resource.getFilename());
			String extension = StringUtils.getFilenameExtension(resource.getFilename());
			Resource minified = resource
					.createRelative(basename + extensionPrefix + '.' + extension);
			if (minified.exists()) {
				return minified;
			}
		} catch (IOException ex) {
			logger.trace("No minified resource for [" + resource.getFilename() + "]", ex);
		}
		return null;
	}

	@Override
	protected String resolveUrlPathInternal(String resourceUrlPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		return chain.resolveUrlPath(resourceUrlPath, locations);
	}

}
