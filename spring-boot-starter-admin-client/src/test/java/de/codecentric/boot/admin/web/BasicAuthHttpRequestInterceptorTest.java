package de.codecentric.boot.admin.web;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

import de.codecentric.boot.admin.web.BasicAuthHttpRequestInterceptor;

public class BasicAuthHttpRequestInterceptorTest {

	@Test
	public void test() throws IOException {
		BasicAuthHttpRequestInterceptor interceptor = new BasicAuthHttpRequestInterceptor("admin", "secret");

		HttpRequest request = new MockClientHttpRequest();
		interceptor.intercept(request, (byte[]) null, new ClientHttpRequestExecution() {
			@Override
			public ClientHttpResponse execute(HttpRequest paramHttpRequest, byte[] paramArrayOfByte) throws IOException {
				return null;
			}
		});

		assertEquals(Collections.singletonList("Basic YWRtaW46c2VjcmV0"), request.getHeaders().get("Authorization"));
	}

}
