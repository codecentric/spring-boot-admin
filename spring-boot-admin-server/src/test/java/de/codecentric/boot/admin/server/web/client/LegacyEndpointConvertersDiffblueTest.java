package de.codecentric.boot.admin.server.web.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class LegacyEndpointConvertersDiffblueTest {
  /**
   * Test {@link LegacyEndpointConverters#health()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#health()}
   */
  @Test
  public void testHealth() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualHealthResult = LegacyEndpointConverters.health();

    // Assert
    assertFalse(actualHealthResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualHealthResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#env()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#env()}
   */
  @Test
  public void testEnv() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualEnvResult = LegacyEndpointConverters.env();

    // Assert
    assertFalse(actualEnvResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualEnvResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#httptrace()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#httptrace()}
   */
  @Test
  public void testHttptrace() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualHttptraceResult = LegacyEndpointConverters.httptrace();

    // Assert
    assertFalse(actualHttptraceResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualHttptraceResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#threaddump()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#threaddump()}
   */
  @Test
  public void testThreaddump() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualThreaddumpResult = LegacyEndpointConverters.threaddump();

    // Assert
    assertFalse(actualThreaddumpResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualThreaddumpResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#liquibase()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#liquibase()}
   */
  @Test
  public void testLiquibase() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualLiquibaseResult = LegacyEndpointConverters.liquibase();

    // Assert
    assertFalse(actualLiquibaseResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualLiquibaseResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#flyway()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#flyway()}
   */
  @Test
  public void testFlyway() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualFlywayResult = LegacyEndpointConverters.flyway();

    // Assert
    assertFalse(actualFlywayResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualFlywayResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#info()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#info()}
   */
  @Test
  public void testInfo() {
    // Arrange and Act
    LegacyEndpointConverter actualInfoResult = LegacyEndpointConverters.info();

    // Assert
    assertNull(actualInfoResult.convert(null));
    assertFalse(actualInfoResult.canConvert("Endpoint Id"));
  }

  /**
   * Test {@link LegacyEndpointConverters#beans()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#beans()}
   */
  @Test
  public void testBeans() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualBeansResult = LegacyEndpointConverters.beans();

    // Assert
    assertFalse(actualBeansResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualBeansResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#configprops()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#configprops()}
   */
  @Test
  public void testConfigprops() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualConfigpropsResult = LegacyEndpointConverters.configprops();

    // Assert
    assertFalse(actualConfigpropsResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualConfigpropsResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#mappings()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#mappings()}
   */
  @Test
  public void testMappings() throws AssertionError {
    // Arrange and Act
    LegacyEndpointConverter actualMappingsResult = LegacyEndpointConverters.mappings();

    // Assert
    assertFalse(actualMappingsResult.canConvert("Endpoint Id"));
    FirstStep<DataBuffer> createResult = StepVerifier.create(actualMappingsResult.convert(null));
    createResult.expectError().verify();
  }

  /**
   * Test {@link LegacyEndpointConverters#startup()}.
   * <p>
   * Method under test: {@link LegacyEndpointConverters#startup()}
   */
  @Test
  public void testStartup() {
    // Arrange and Act
    LegacyEndpointConverter actualStartupResult = LegacyEndpointConverters.startup();

    // Assert
    assertNull(actualStartupResult.convert(null));
    assertFalse(actualStartupResult.canConvert("Endpoint Id"));
  }
}
