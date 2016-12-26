package de.codecentric.boot.admin.web.client;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.model.Application;

public class ApplicationOperationsTest {

	private RestTemplate restTemplate = mock(RestTemplate.class);
	private HttpHeadersProvider headersProvider = mock(HttpHeadersProvider.class);
	private ApplicationOperations ops = new ApplicationOperations(restTemplate, headersProvider);

	@Test
	@SuppressWarnings("rawtypes")
	public void test_getInfo() {
		Application app = Application.create("test").withHealthUrl("http://health")
				.withManagementUrl("http://mgmt").build();
		ArgumentCaptor<HttpEntity> requestEntity = ArgumentCaptor.forClass(HttpEntity.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add("auth", "foo:bar");
		when(headersProvider.getHeaders(eq(app))).thenReturn(headers);

		when(restTemplate.exchange(eq(URI.create("http://mgmt/info")), eq(HttpMethod.GET),
				requestEntity.capture(), eq(Map.class)))
						.thenReturn(ResponseEntity.ok().body((Map) singletonMap("foo", "bar")));

		ResponseEntity<Map<String, Serializable>> response = ops.getInfo(app);

		assertThat(response.getBody()).containsEntry("foo", "bar");
		assertThat(requestEntity.getValue().getHeaders()).containsEntry("auth", asList("foo:bar"));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_getHealth() {
		Application app = Application.create("test").withHealthUrl("http://health")
				.withManagementUrl("http://mgmt").build();
		ArgumentCaptor<HttpEntity> requestEntity = ArgumentCaptor.forClass(HttpEntity.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add("auth", "foo:bar");
		when(headersProvider.getHeaders(eq(app))).thenReturn(headers);

		when(restTemplate.exchange(eq(URI.create("http://health")), eq(HttpMethod.GET),
				requestEntity.capture(), eq(Map.class)))
						.thenReturn(ResponseEntity.ok().body((Map) singletonMap("foo", "bar")));

		ResponseEntity<Map<String, Serializable>> response = ops.getHealth(app);

		assertThat(response.getBody()).containsEntry("foo", "bar");
		assertThat(requestEntity.getValue().getHeaders()).containsEntry("auth", asList("foo:bar"));
	}

}
