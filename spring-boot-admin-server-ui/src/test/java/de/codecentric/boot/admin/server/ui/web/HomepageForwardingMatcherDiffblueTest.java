package de.codecentric.boot.admin.server.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HomepageForwardingMatcherDiffblueTest {
  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>Given {@code 42}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} size is two.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); given '42'; then ArrayList() size is two")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_given42_thenArrayListSizeIsTwo() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();
    includeRoutes.add("42");
    includeRoutes.add("foo");
    ArrayList<String> excludeRoutes = new ArrayList<>();
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        excludeRoutes, methodAccessor, mock(Function.class), mock(Function.class));
    boolean actualTestResult = actualHomepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertEquals(2, includeRoutes.size());
    assertEquals("42", includeRoutes.get(0));
    assertEquals("foo", includeRoutes.get(1));
    assertFalse(actualTestResult);
  }

  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>Given {@code 42}.</li>
   *   <li>When {@link ArrayList#ArrayList()} add {@code 42}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); given '42'; when ArrayList() add '42'; then ArrayList() Empty")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_given42_whenArrayListAdd42_thenArrayListEmpty() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();

    ArrayList<String> excludeRoutes = new ArrayList<>();
    excludeRoutes.add("42");
    excludeRoutes.add("foo");
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        excludeRoutes, methodAccessor, mock(Function.class), mock(Function.class));
    boolean actualTestResult = actualHomepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertFalse(actualTestResult);
    assertTrue(includeRoutes.isEmpty());
  }

  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>Given {@code Apply}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); given 'Apply'; then ArrayList() Empty")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_givenApply_thenArrayListEmpty() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();
    ArrayList<String> excludeRoutes = new ArrayList<>();
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        excludeRoutes, methodAccessor, mock(Function.class), mock(Function.class));
    boolean actualTestResult = actualHomepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertFalse(actualTestResult);
    assertTrue(includeRoutes.isEmpty());
  }

  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>Given {@code foo}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} size is one.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); given 'foo'; then ArrayList() size is one")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_givenFoo_thenArrayListSizeIsOne() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();
    includeRoutes.add("foo");
    ArrayList<String> excludeRoutes = new ArrayList<>();
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        excludeRoutes, methodAccessor, mock(Function.class), mock(Function.class));
    boolean actualTestResult = actualHomepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertEquals(1, includeRoutes.size());
    assertEquals("foo", includeRoutes.get(0));
    assertFalse(actualTestResult);
  }

  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>Given {@code foo}.</li>
   *   <li>When {@link ArrayList#ArrayList()} add {@code foo}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); given 'foo'; when ArrayList() add 'foo'; then ArrayList() Empty")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_givenFoo_whenArrayListAddFoo_thenArrayListEmpty() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();

    ArrayList<String> excludeRoutes = new ArrayList<>();
    excludeRoutes.add("foo");
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        excludeRoutes, methodAccessor, mock(Function.class), mock(Function.class));
    boolean actualTestResult = actualHomepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertFalse(actualTestResult);
    assertTrue(includeRoutes.isEmpty());
  }

  /**
   * Test {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}.
   * <ul>
   *   <li>When {@link ArrayList#ArrayList()}.</li>
   *   <li>Then {@link ArrayList#ArrayList()} Empty.</li>
   * </ul>
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#HomepageForwardingMatcher(List, List, Function, Function, Function)}
   */
  @Test
  @DisplayName("Test new HomepageForwardingMatcher(List, List, Function, Function, Function); when ArrayList(); then ArrayList() Empty")
  @Tag("MaintainedByDiffblue")
  void testNewHomepageForwardingMatcher_whenArrayList_thenArrayListEmpty() {
    // Arrange
    ArrayList<String> includeRoutes = new ArrayList<>();

    // Act
    HomepageForwardingMatcher<Object> actualHomepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        new ArrayList<>(), mock(Function.class), mock(Function.class), mock(Function.class));

    // Assert
    assertFalse(actualHomepageForwardingMatcher.test("Request"));
    assertTrue(includeRoutes.isEmpty());
  }

  /**
   * Test {@link HomepageForwardingMatcher#test(Object)}.
   * <p>
   * Method under test: {@link HomepageForwardingMatcher#test(Object)}
   */
  @Test
  @DisplayName("Test test(Object)")
  @Tag("MaintainedByDiffblue")
  void testTest() {
    // Arrange
    Function<Object, String> methodAccessor = mock(Function.class);
    when(methodAccessor.apply(Mockito.<Object>any())).thenReturn("Apply");
    ArrayList<String> includeRoutes = new ArrayList<>();
    HomepageForwardingMatcher<Object> homepageForwardingMatcher = new HomepageForwardingMatcher<>(includeRoutes,
        new ArrayList<>(), methodAccessor, mock(Function.class), mock(Function.class));

    // Act
    boolean actualTestResult = homepageForwardingMatcher.test("Request");

    // Assert
    verify(methodAccessor).apply(isA(Object.class));
    assertFalse(actualTestResult);
  }
}
