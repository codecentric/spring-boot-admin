package de.codecentric.boot.admin.web.client;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.model.Application;

/**
 * Provides Basic Auth headers for the {@link Application} using the metadata for "user.name" and
 * "user.password".
 *
 * @author Johannes Edmeier
 */
public class BasicAuthHttpHeaderProvider implements HttpHeadersProvider {

	@Override
	public HttpHeaders getHeaders(Application application) {
		String username = application.getMetadata().get("user.name");
		String password = application.getMetadata().get("user.password");

		HttpHeaders headers = new HttpHeaders();

		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			headers.set(HttpHeaders.AUTHORIZATION, encode(username, password));
		}

		return headers;
	}

	protected String encode(String username, String password) {
		String token = Base64Utils
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}

}
