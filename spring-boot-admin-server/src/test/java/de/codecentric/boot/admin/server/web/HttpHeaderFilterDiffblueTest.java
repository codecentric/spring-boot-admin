package de.codecentric.boot.admin.server.web;

import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class HttpHeaderFilterDiffblueTest {
  /**
   * Test {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}.
   * <ul>
   *   <li>Given {@code Header Name}.</li>
   *   <li>Then return size is two.</li>
   * </ul>
   * <p>
   * Method under test: {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}
   */
  @Test
  public void testFilterHeaders_givenHeaderName_thenReturnSizeIsTwo() {
    // Arrange
    HttpHeaderFilter httpHeaderFilter = new HttpHeaderFilter(new HashSet<>());

    HttpHeaders headers = new HttpHeaders();
    headers.add("Header Name", "42");
    headers.add("https://example.org/example", "https://example.org/example");

    // Act
    HttpHeaders actualFilterHeadersResult = httpHeaderFilter.filterHeaders(headers);

    // Assert
    assertEquals(2, actualFilterHeadersResult.size());
    List<String> getResult = actualFilterHeadersResult.get("Header Name");
    assertEquals(1, getResult.size());
    assertEquals("42", getResult.get(0));
    List<String> getResult2 = actualFilterHeadersResult.get("https://example.org/example");
    assertEquals(1, getResult2.size());
    assertEquals("https://example.org/example", getResult2.get(0));
  }

  /**
   * Test {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}.
   * <ul>
   *   <li>Given {@code https://example.org/example}.</li>
   *   <li>Then return size is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}
   */
  @Test
  public void testFilterHeaders_givenHttpsExampleOrgExample_thenReturnSizeIsOne() {
    // Arrange
    HttpHeaderFilter httpHeaderFilter = new HttpHeaderFilter(new HashSet<>());

    HttpHeaders headers = new HttpHeaders();
    headers.add("https://example.org/example", "https://example.org/example");

    // Act
    HttpHeaders actualFilterHeadersResult = httpHeaderFilter.filterHeaders(headers);

    // Assert
    assertEquals(1, actualFilterHeadersResult.size());
    List<String> getResult = actualFilterHeadersResult.get("https://example.org/example");
    assertEquals(1, getResult.size());
    assertEquals("https://example.org/example", getResult.get(0));
  }

  /**
   * Test {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}.
   * <ul>
   *   <li>When {@link HttpHeaders#HttpHeaders()}.</li>
   *   <li>Then return {@link HttpHeaders#HttpHeaders()}.</li>
   * </ul>
   * <p>
   * Method under test: {@link HttpHeaderFilter#filterHeaders(HttpHeaders)}
   */
  @Test
  public void testFilterHeaders_whenHttpHeaders_thenReturnHttpHeaders() {
    // Arrange
    HttpHeaderFilter httpHeaderFilter = new HttpHeaderFilter(new HashSet<>());
    HttpHeaders headers = new HttpHeaders();

    // Act and Assert
    assertEquals(headers, httpHeaderFilter.filterHeaders(headers));
  }
}
