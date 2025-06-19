package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import org.junit.Test;

public class EndpointMixinDiffblueTest {
  /**
   * Test {@link EndpointMixin#of(String, String)}.
   * <ul>
   *   <li>When {@code 42}.</li>
   *   <li>Then return Id is {@code 42}.</li>
   * </ul>
   * <p>
   * Method under test: {@link EndpointMixin#of(String, String)}
   */
  @Test
  public void testOf_when42_thenReturnIdIs42() {
    // Arrange and Act
    Endpoint actualOfResult = EndpointMixin.of("42", "https://example.org/example");

    // Assert
    assertEquals("42", actualOfResult.getId());
    assertEquals("https://example.org/example", actualOfResult.getUrl());
  }
}
