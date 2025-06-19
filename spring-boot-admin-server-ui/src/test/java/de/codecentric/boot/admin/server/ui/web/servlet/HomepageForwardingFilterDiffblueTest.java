package de.codecentric.boot.admin.server.ui.web.servlet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

class HomepageForwardingFilterDiffblueTest {
  /**
   * Test {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
   * <ul>
   *   <li>Given {@link IOException#IOException(String)} with {@code foo}.</li>
   *   <li>Then throw {@link IOException}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
   */
  @Test
  @DisplayName("Test doFilter(ServletRequest, ServletResponse, FilterChain); given IOException(String) with 'foo'; then throw IOException")
  @Tag("MaintainedByDiffblue")
  void testDoFilter_givenIOExceptionWithFoo_thenThrowIOException() throws ServletException, IOException {
    // Arrange
    ArrayList<String> routesIncludes = new ArrayList<>();
    HomepageForwardingFilter homepageForwardingFilter = new HomepageForwardingFilter(
        new HomepageForwardingFilterConfig("Homepage", routesIncludes, new ArrayList<>()));
    MockHttpServletRequest request = new MockHttpServletRequest();
    Response response = new Response();
    FilterChain chain = mock(FilterChain.class);
    doThrow(new IOException("foo")).when(chain).doFilter(Mockito.<ServletRequest>any(), Mockito.<ServletResponse>any());

    // Act and Assert
    assertThrows(IOException.class, () -> homepageForwardingFilter.doFilter(request, response, chain));
    verify(chain).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
  }

  /**
   * Test {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
   * <ul>
   *   <li>When {@link MockHttpServletRequest#MockHttpServletRequest()}.</li>
   *   <li>Then calls {@link FilterChain#doFilter(ServletRequest, ServletResponse)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
   */
  @Test
  @DisplayName("Test doFilter(ServletRequest, ServletResponse, FilterChain); when MockHttpServletRequest(); then calls doFilter(ServletRequest, ServletResponse)")
  @Tag("MaintainedByDiffblue")
  void testDoFilter_whenMockHttpServletRequest_thenCallsDoFilter() throws ServletException, IOException {
    // Arrange
    ArrayList<String> routesIncludes = new ArrayList<>();
    HomepageForwardingFilter homepageForwardingFilter = new HomepageForwardingFilter(
        new HomepageForwardingFilterConfig("Homepage", routesIncludes, new ArrayList<>()));
    MockHttpServletRequest request = new MockHttpServletRequest();
    Response response = new Response();
    FilterChain chain = mock(FilterChain.class);
    doNothing().when(chain).doFilter(Mockito.<ServletRequest>any(), Mockito.<ServletResponse>any());

    // Act
    homepageForwardingFilter.doFilter(request, response, chain);

    // Assert
    verify(chain).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
  }

  /**
   * Test {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
   * <ul>
   *   <li>When {@link ServletRequest}.</li>
   *   <li>Then calls {@link FilterChain#doFilter(ServletRequest, ServletResponse)}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
   */
  @Test
  @DisplayName("Test doFilter(ServletRequest, ServletResponse, FilterChain); when ServletRequest; then calls doFilter(ServletRequest, ServletResponse)")
  @Tag("MaintainedByDiffblue")
  void testDoFilter_whenServletRequest_thenCallsDoFilter() throws ServletException, IOException {
    // Arrange
    ArrayList<String> routesIncludes = new ArrayList<>();
    HomepageForwardingFilter homepageForwardingFilter = new HomepageForwardingFilter(
        new HomepageForwardingFilterConfig("Homepage", routesIncludes, new ArrayList<>()));
    ServletRequest request = mock(ServletRequest.class);
    Response response = new Response();
    FilterChain chain = mock(FilterChain.class);
    doNothing().when(chain).doFilter(Mockito.<ServletRequest>any(), Mockito.<ServletResponse>any());

    // Act
    homepageForwardingFilter.doFilter(request, response, chain);

    // Assert
    verify(chain).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
  }
}
