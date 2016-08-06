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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import de.codecentric.boot.admin.io.resource.InMemoryFileResource;

/**
 * {@link ResourceResolver} that returns a resource with the concatenated resources given.
 *
 * @author Johannes Edmeier
 */
public class ConcatenatingResourceResolver extends AbstractResourceResolver {
	private final byte[] delimiter;

	public ConcatenatingResourceResolver(byte[] delimiter) {
		this.delimiter = delimiter.clone();
	}

	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		String filename = StringUtils.getFilename(requestPath);
		try {
			return new InMemoryFileResource(filename, buildDescription(locations),
					getContent(locations), getLastModified(locations));
		} catch (IOException ex) {
			throw new ResourceAccessException("Couldn't concatenate resources [" + locations + "]",
					ex);
		}
	}

	@Override
	protected String resolveUrlPathInternal(String resourceUrlPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		return chain.resolveUrlPath(resourceUrlPath, locations);
	}

	private byte[] getContent(List<? extends Resource> resources) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
		Iterator<? extends Resource> iter = resources.iterator();
		while (iter.hasNext()) {
			StreamUtils.copy(iter.next().getInputStream(), os);
			if (iter.hasNext()) {
				os.write(delimiter);
			}
		}
		return os.toByteArray();
	}

	private long getLastModified(List<? extends Resource> resources) throws IOException {
		long maxLastModified = 0;
		for (Resource resource : resources) {
			maxLastModified = Math.max(maxLastModified, resource.lastModified());
		}
		return maxLastModified;
	}

	private String buildDescription(Collection<? extends Resource> resources) {
		StringBuilder sb = new StringBuilder("(");
		for (Resource resource : resources) {
			sb.append(resource.getDescription()).append(", ");
		}
		sb.replace(sb.length() - 2, sb.length(), ")");
		return sb.toString();
	}

}
