package de.codecentric.boot.admin.web.servlet.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

public class ResourcePatternResolvingResourceResolverTest {

	@Test
	public void test_resolveResource() {
		ResourceResolver resolver = new ResourcePatternResolvingResourceResolver(
				new PathMatchingResourcePatternResolver(), "classpath:/t*Resource.txt");

		resolver.resolveResource(null, null, null, new ResourceResolverChain() {
			@Override
			public Resource resolveResource(HttpServletRequest request, String requestPath,
					List<? extends Resource> locations) {
				assertThat(locations.size(), is(1));
				assertThat(locations.get(0).getFilename(), is("testResource.txt"));
				return null;
			}

			@Override
			public String resolveUrlPath(String resourcePath, List<? extends Resource> locations) {
				return null;
			}
		});
	}

	@Test
	public void test_resolveUrl() {
		ResourceResolverChain chain = mock(ResourceResolverChain.class);
		when(chain.resolveUrlPath(null, null)).thenReturn("/resources/resource.txt");
		String url = new ResourcePatternResolvingResourceResolver(null, null).resolveUrlPath(null,
				null, chain);
		assertThat(url, is("/resources/resource.txt"));
	}

}
