package de.codecentric.boot.admin.server.ui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.loading.MLet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aot.hint.JavaSerializationHint;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {ServerRuntimeHints.class})
@ExtendWith(SpringExtension.class)
class ServerRuntimeHintsDiffblueTest {
  @Autowired
  private ServerRuntimeHints serverRuntimeHints;

  /**
   * Test {@link ServerRuntimeHints#registerHints(RuntimeHints, ClassLoader)}.
   * <p>
   * Method under test: {@link ServerRuntimeHints#registerHints(RuntimeHints, ClassLoader)}
   */
  @Test
  @DisplayName("Test registerHints(RuntimeHints, ClassLoader)")
  @Tag("MaintainedByDiffblue")
  void testRegisterHints() {
    // Arrange
    RuntimeHints hints = new RuntimeHints();

    // Act
    serverRuntimeHints.registerHints(hints, new MLet());

    // Assert
    Stream<JavaSerializationHint> javaSerializationHintsResult = hints.serialization().javaSerializationHints();
    List<JavaSerializationHint> collectResult = javaSerializationHintsResult.limit(5).collect(Collectors.toList());
    assertEquals(5, collectResult.size());
    assertNull(collectResult.get(0).getReachableType());
    assertNull(collectResult.get(1).getReachableType());
    assertNull(collectResult.get(3).getReachableType());
    assertNull(collectResult.get(4).getReachableType());
  }
}
