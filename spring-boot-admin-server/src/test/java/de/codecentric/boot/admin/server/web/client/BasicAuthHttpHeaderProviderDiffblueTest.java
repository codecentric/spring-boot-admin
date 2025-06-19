package de.codecentric.boot.admin.server.web.client;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {BasicAuthHttpHeaderProvider.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class BasicAuthHttpHeaderProviderDiffblueTest {
  @Autowired
  private BasicAuthHttpHeaderProvider basicAuthHttpHeaderProvider;

  /**
   * Test {@link BasicAuthHttpHeaderProvider#encode(String, String)}.
   * <p>
   * Method under test: {@link BasicAuthHttpHeaderProvider#encode(String, String)}
   */
  @Test
  public void testEncode() {
    // Arrange, Act and Assert
    assertEquals("Basic amFuZWRvZTpodHRwczovL2V4YW1wbGUub3JnL2V4YW1wbGU=",
        basicAuthHttpHeaderProvider.encode("janedoe", "https://example.org/example"));
  }
}
