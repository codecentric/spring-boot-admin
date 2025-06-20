package de.codecentric.boot.admin.server.web.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

public class CompositeHttpHeadersProviderDiffblueTest {

	/**
	 * Test {@link CompositeHttpHeadersProvider#getHeaders(Instance)}.
	 * <p>
	 * Method under test: {@link CompositeHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders() {
		// Arrange, Act and Assert
		assertTrue(new CompositeHttpHeadersProvider(new ArrayList<>()).getHeaders(null).isEmpty());
	}

	/**
	 * Test {@link CompositeHttpHeadersProvider#getHeaders(Instance)}.
	 * <ul>
	 * <li>Then calls {@link HttpHeadersProvider#getHeaders(Instance)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders_thenCallsGetHeaders() {
		// Arrange
		HttpHeadersProvider httpHeadersProvider = mock(HttpHeadersProvider.class);
		when(httpHeadersProvider.getHeaders(Mockito.<Instance>any())).thenReturn(new HttpHeaders());

		ArrayList<HttpHeadersProvider> delegates = new ArrayList<>();
		delegates.add(httpHeadersProvider);

		// Act
		HttpHeaders actualHeaders = new CompositeHttpHeadersProvider(delegates).getHeaders(null);

		// Assert
		verify(httpHeadersProvider).getHeaders(isNull());
		assertTrue(actualHeaders.isEmpty());
	}

	/**
	 * Test {@link CompositeHttpHeadersProvider#getHeaders(Instance)}.
	 * <ul>
	 * <li>Then calls {@link HttpHeadersProvider#getHeaders(Instance)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link CompositeHttpHeadersProvider#getHeaders(Instance)}
	 */
	@Test
	public void testGetHeaders_thenCallsGetHeaders2() {
		// Arrange
		HttpHeadersProvider httpHeadersProvider = mock(HttpHeadersProvider.class);
		when(httpHeadersProvider.getHeaders(Mockito.<Instance>any())).thenReturn(new HttpHeaders());
		HttpHeadersProvider httpHeadersProvider2 = mock(HttpHeadersProvider.class);
		when(httpHeadersProvider2.getHeaders(Mockito.<Instance>any())).thenReturn(new HttpHeaders());

		ArrayList<HttpHeadersProvider> delegates = new ArrayList<>();
		delegates.add(httpHeadersProvider2);
		delegates.add(httpHeadersProvider);

		// Act
		HttpHeaders actualHeaders = new CompositeHttpHeadersProvider(delegates).getHeaders(null);

		// Assert
		verify(httpHeadersProvider2).getHeaders(isNull());
		verify(httpHeadersProvider).getHeaders(isNull());
		assertTrue(actualHeaders.isEmpty());
	}

}
