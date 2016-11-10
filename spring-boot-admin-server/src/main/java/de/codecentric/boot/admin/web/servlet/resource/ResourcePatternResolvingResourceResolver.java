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
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * {@link ResourceResolver} that resolves all resources for the given pattern and delegates them to
 * the chain.
 *
 * @author Johannes Edmeier
 */
public class ResourcePatternResolvingResourceResolver extends AbstractResourceResolver {
	private final ResourcePatternResolver resourcePatternResolver;
	private final String pattern;

	public ResourcePatternResolvingResourceResolver(ResourcePatternResolver resourcePatternResolver,
			String pattern) {
		this.resourcePatternResolver = resourcePatternResolver;
		this.pattern = pattern;
	}

	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		try {
			Resource[] resources = resourcePatternResolver.getResources(pattern);
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Resolved Resources for '" + pattern + "': " + Arrays.toString(resources));
			}
			return chain.resolveResource(request, requestPath, Arrays.asList(resources));
		} catch (IOException ex) {
			throw new ResourceAccessException("Couldn't resolve resources for \"" + pattern + "\"",
					ex);
		}
	}

	@Override
	protected String resolveUrlPathInternal(String resourceUrlPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		return chain.resolveUrlPath(resourceUrlPath, locations);
	}

}
