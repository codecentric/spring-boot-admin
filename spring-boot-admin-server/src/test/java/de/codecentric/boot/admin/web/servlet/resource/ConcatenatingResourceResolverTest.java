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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.StreamUtils.copyToByteArray;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.ResourceResolverChain;

public class ConcatenatingResourceResolverTest {

	@Test
	public void test_concatenation() throws IOException {
		Resource testResource = new ClassPathResource("/testResource.txt");
		List<Resource> resources = asList(testResource, testResource, testResource);

		Resource resolvedResource = new ConcatenatingResourceResolver(";".getBytes())
				.resolveResource(null, "/foo.txt", resources, null);

		assertThat(resolvedResource.getFilename(), is("foo.txt"));
		assertThat(resolvedResource.lastModified(), is(testResource.lastModified()));
		assertThat(resolvedResource.getDescription(), is(
				"Byte array resource [(class path resource [testResource.txt], class path resource [testResource.txt], class path resource [testResource.txt])]"));
		assertThat(copyToByteArray(resolvedResource.getInputStream()), is("Foobar;Foobar;Foobar".getBytes()));
	}

	@Test(expected = ResourceAccessException.class)
	public void test_concatenation_fails() throws IOException {
		List<? extends Resource> resources = asList(new ClassPathResource("/testResource.txt"),
				new ClassPathResource("/not.foud"));

		new ConcatenatingResourceResolver(";".getBytes()).resolveResource(null, "/foo.txt",
				resources, null);
	}

	@Test
	public void test_resolveUrl() {
		ResourceResolverChain chain = mock(ResourceResolverChain.class);
		when(chain.resolveUrlPath(null, null)).thenReturn("/resources/resource.txt");
		String url = new ConcatenatingResourceResolver(";".getBytes()).resolveUrlPath(null, null,
				chain);
		assertThat(url, is("/resources/resource.txt"));
	}
}