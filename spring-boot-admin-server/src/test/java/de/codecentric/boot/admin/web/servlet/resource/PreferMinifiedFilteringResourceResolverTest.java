package de.codecentric.boot.admin.web.servlet.resource;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolverChain;

public class PreferMinifiedFilteringResourceResolverTest {

	@Test
	public void test_resolveResource() {
		List<? extends Resource> resources = asList(
				new ClassPathResource("testResource.txt"),
				new ClassPathResource("application.properties"));

		new PreferMinifiedFilteringResourceResolver(".min").resolveResource(null, null,
				resources, new ResourceResolverChain() {
					@Override
					public Resource resolveResource(HttpServletRequest request, String requestPath,
							List<? extends Resource> locations) {
						assertThat(locations.size(), is(2));
						assertThat(locations, contains(
								(Resource) new ClassPathResource("testResource.min.txt"),
								(Resource) new ClassPathResource("application.properties")));
						return null;
					}
					@Override
					public String resolveUrlPath(String resourcePath,
							List<? extends Resource> locations) {
						return null;
					}
				});
	}

	@Test
	public void test_resolveUrl() {
		ResourceResolverChain chain = mock(ResourceResolverChain.class);
		when(chain.resolveUrlPath(null, null)).thenReturn("/resources/resource.txt");
		String url = new PreferMinifiedFilteringResourceResolver("").resolveUrlPath(null, null,
				chain);
		assertThat(url, is("/resources/resource.txt"));
	}

}
