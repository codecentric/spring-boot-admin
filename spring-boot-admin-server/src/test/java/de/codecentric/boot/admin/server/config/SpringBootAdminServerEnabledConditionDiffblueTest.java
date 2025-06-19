package de.codecentric.boot.admin.server.config;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SpringBootAdminServerEnabledConditionDiffblueTest {
  /**
   * Test {@link SpringBootAdminServerEnabledCondition#getMatchOutcome(ConditionContext, AnnotatedTypeMetadata)}.
   * <ul>
   *   <li>Given {@link StandardReactiveWebEnvironment#StandardReactiveWebEnvironment()}.</li>
   *   <li>Then return Message is {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link SpringBootAdminServerEnabledCondition#getMatchOutcome(ConditionContext, AnnotatedTypeMetadata)}
   */
  @Test
  public void testGetMatchOutcome_givenStandardReactiveWebEnvironment_thenReturnMessageIsNull() {
    // Arrange
    SpringBootAdminServerEnabledCondition springBootAdminServerEnabledCondition = new SpringBootAdminServerEnabledCondition();
    ConditionContext context = mock(ConditionContext.class);
    when(context.getEnvironment()).thenReturn(new StandardReactiveWebEnvironment());

    // Act
    ConditionOutcome actualMatchOutcome = springBootAdminServerEnabledCondition.getMatchOutcome(context,
        mock(AnnotatedTypeMetadata.class));

    // Assert
    verify(context).getEnvironment();
    assertNull(actualMatchOutcome.getMessage());
    assertTrue(actualMatchOutcome.getConditionMessage().isEmpty());
    assertTrue(actualMatchOutcome.isMatch());
  }
}
