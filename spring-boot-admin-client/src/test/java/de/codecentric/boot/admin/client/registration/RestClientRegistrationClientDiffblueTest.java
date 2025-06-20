package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@ContextConfiguration(classes = { RestClientRegistrationClient.class })
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class RestClientRegistrationClientDiffblueTest {

	@MockitoBean
	private RestClient restClient;

	@Autowired
	private RestClientRegistrationClient restClientRegistrationClient;

	/**
	 * Test {@link RestClientRegistrationClient#register(String, Application)}.
	 * <ul>
	 * <li>Given {@link HashMap#HashMap()} {@code id} is {@code 42}.</li>
	 * <li>Then return {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link RestClientRegistrationClient#register(String, Application)}
	 */
	@Test
	@DisplayName("Test register(String, Application); given HashMap() 'id' is '42'; then return '42'")
	@Tag("MaintainedByDiffblue")
	void testRegister_givenHashMapIdIs42_thenReturn42() {
		// Arrange
		HashMap<String, Object> stringObjectMap = new HashMap<>();
		stringObjectMap.put("id", "42");
		ResponseSpec responseSpec = mock(ResponseSpec.class);
		when(responseSpec.body(Mockito.<ParameterizedTypeReference<Map<String, Object>>>any()))
			.thenReturn(stringObjectMap);
		RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
		when(requestBodySpec.retrieve()).thenReturn(responseSpec);
		RequestBodySpec requestBodySpec2 = mock(RequestBodySpec.class);
		when(requestBodySpec2.body(Mockito.<Object>any())).thenReturn(requestBodySpec);
		RequestBodySpec requestBodySpec3 = mock(RequestBodySpec.class);
		when(requestBodySpec3.headers(Mockito.<Consumer<HttpHeaders>>any())).thenReturn(requestBodySpec2);
		RequestBodyUriSpec requestBodyUriSpec = mock(RequestBodyUriSpec.class);
		when(requestBodyUriSpec.uri(Mockito.<String>any(), isA(Object[].class))).thenReturn(requestBodySpec3);
		when(restClient.post()).thenReturn(requestBodyUriSpec);

		// Act
		String actualRegisterResult = restClientRegistrationClient.register("https://example.org/example",
				new Application("Name", "https://example.org/example", "https://example.org/example",
						"https://example.org/example", new HashMap<>()));

		// Assert
		verify(restClient).post();
		verify(requestBodySpec2).body(isA(Object.class));
		verify(requestBodySpec3).headers(isA(Consumer.class));
		verify(requestBodySpec).retrieve();
		verify(responseSpec).body(isA(ParameterizedTypeReference.class));
		verify(requestBodyUriSpec).uri(eq("https://example.org/example"), isA(Object[].class));
		assertEquals("42", actualRegisterResult);
	}

	/**
	 * Test {@link RestClientRegistrationClient#setRequestHeaders(HttpHeaders)}.
	 * <p>
	 * Method under test:
	 * {@link RestClientRegistrationClient#setRequestHeaders(HttpHeaders)}
	 */
	@Test
	@DisplayName("Test setRequestHeaders(HttpHeaders)")
	@Tag("MaintainedByDiffblue")
	void testSetRequestHeaders() {
		// Arrange
		HttpHeaders headers = new HttpHeaders();

		// Act
		restClientRegistrationClient.setRequestHeaders(headers);

		// Assert
		assertEquals(2, headers.size());
		List<String> getResult = headers.get(HttpHeaders.CONTENT_TYPE);
		assertEquals(1, getResult.size());
		assertEquals("application/json", getResult.get(0));
		assertEquals(getResult, headers.get(HttpHeaders.ACCEPT));
	}

}
