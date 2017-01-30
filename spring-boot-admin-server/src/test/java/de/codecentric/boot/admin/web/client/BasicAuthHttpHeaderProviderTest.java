package de.codecentric.boot.admin.web.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.codecentric.boot.admin.model.Application;
import org.springframework.http.HttpHeaders;

public class BasicAuthHttpHeaderProviderTest {
	private BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

	@Test
	public void test_auth_header() {
		Application app = Application.create("test").withHealthUrl("/health")
				.addMetadata("user.name", "test").addMetadata("user.password", "drowssap").build();
		assertThat(headersProvider.getHeaders(app).get(HttpHeaders.AUTHORIZATION))
				.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	public void test_no_header() {
		Application app = Application.create("test").withHealthUrl("/health").build();
		assertThat(headersProvider.getHeaders(app)).isEmpty();
	}
}
