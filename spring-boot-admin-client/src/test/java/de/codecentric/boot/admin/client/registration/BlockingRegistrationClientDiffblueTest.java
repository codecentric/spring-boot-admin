package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = { BlockingRegistrationClient.class })
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class BlockingRegistrationClientDiffblueTest {

	@Autowired
	private BlockingRegistrationClient blockingRegistrationClient;

	@MockitoBean
	private RestTemplate restTemplate;

	/**
	 * Test {@link BlockingRegistrationClient#deregister(String, String)}.
	 * <p>
	 * Method under test: {@link BlockingRegistrationClient#deregister(String, String)}
	 */
	@Test
	@DisplayName("Test deregister(String, String)")
	@Tag("MaintainedByDiffblue")
	void testDeregister() throws RestClientException {
		// Arrange
		doNothing().when(restTemplate).delete(Mockito.<String>any(), isA(Object[].class));

		// Act
		blockingRegistrationClient.deregister("https://example.org/example", "42");

		// Assert
		verify(restTemplate).delete(eq("https://example.org/example/42"), isA(Object[].class));
	}

	/**
	 * Test {@link BlockingRegistrationClient#createRequestHeaders()}.
	 * <p>
	 * Method under test: {@link BlockingRegistrationClient#createRequestHeaders()}
	 */
	@Test
	@DisplayName("Test createRequestHeaders()")
	@Tag("MaintainedByDiffblue")
	void testCreateRequestHeaders() {
		// Arrange and Act
		HttpHeaders actualCreateRequestHeadersResult = blockingRegistrationClient.createRequestHeaders();

		// Assert
		assertEquals(2, actualCreateRequestHeadersResult.size());
		List<String> getResult = actualCreateRequestHeadersResult.get(HttpHeaders.CONTENT_TYPE);
		assertEquals(1, getResult.size());
		assertEquals("application/json", getResult.get(0));
		assertEquals(getResult, actualCreateRequestHeadersResult.get(HttpHeaders.ACCEPT));
	}

}
