package de.codecentric.boot.admin.web.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.net.HttpHeaders;

import de.codecentric.boot.admin.model.Application;

public class BasicAuthHttpHeaderProviderTest {
	private BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

	@Test
	public void test_auth_header() {
		Application app = Application.create("test").withHealthUrl("/health")
				.addMetadata("user.name", "test").addMetadata("user.password", "drowssap")
				.build();
		assertThat(headersProvider.getHeaders(app).get(HttpHeaders.AUTHORIZATION).get(0),
				is("Basic dGVzdDpkcm93c3NhcA=="));
	}

	@Test
	public void test_no_header() {
		Application app = Application.create("test").withHealthUrl("/health").build();
		assertTrue(headersProvider.getHeaders(app).isEmpty());
	}
}
