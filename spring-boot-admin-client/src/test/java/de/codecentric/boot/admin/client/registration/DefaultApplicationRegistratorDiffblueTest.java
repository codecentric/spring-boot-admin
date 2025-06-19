package de.codecentric.boot.admin.client.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class DefaultApplicationRegistratorDiffblueTest {
  /**
   * Test {@link DefaultApplicationRegistrator#DefaultApplicationRegistrator(ApplicationFactory, RegistrationClient, String[], boolean)}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#DefaultApplicationRegistrator(ApplicationFactory, RegistrationClient, String[], boolean)}
   */
  @Test
  @DisplayName("Test new DefaultApplicationRegistrator(ApplicationFactory, RegistrationClient, String[], boolean)")
  @Tag("MaintainedByDiffblue")
  void testNewDefaultApplicationRegistrator() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);

    // Act and Assert
    assertNull(
        new DefaultApplicationRegistrator(applicationFactory, new BlockingRegistrationClient(mock(RestTemplate.class)),
            new String[]{"https://example.org/example"}, true).getRegisteredId());
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register()}
   */
  @Test
  @DisplayName("Test register()")
  @Tag("MaintainedByDiffblue")
  void testRegister() throws RestClientException {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    Application buildResult = Application.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .build();
    when(applicationFactory.createApplication()).thenReturn(buildResult);
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(Mockito.<String>any(), Mockito.<HttpMethod>any(), Mockito.<HttpEntity<?>>any(),
        Mockito.<ParameterizedTypeReference<Object>>any(), isA(Object[].class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(applicationFactory,
        new BlockingRegistrationClient(restTemplate), new String[]{"https://example.org/example"}, true);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register();

    // Assert
    verify(applicationFactory).createApplication();
    verify(restTemplate).exchange(eq("https://example.org/example"), isA(HttpMethod.class), isA(HttpEntity.class),
        isA(ParameterizedTypeReference.class), isA(Object[].class));
    assertNull(defaultApplicationRegistrator.getRegisteredId());
    assertFalse(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register()}
   */
  @Test
  @DisplayName("Test register()")
  @Tag("MaintainedByDiffblue")
  void testRegister2() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    Application buildResult = Application.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .build();
    when(applicationFactory.createApplication()).thenReturn(buildResult);
    RegistrationClient registrationClient = mock(RegistrationClient.class);
    when(registrationClient.register(Mockito.<String>any(), Mockito.<Application>any())).thenReturn("Register");
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(applicationFactory,
        registrationClient, new String[]{"https://example.org/example"}, true);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register();

    // Assert
    verify(applicationFactory).createApplication();
    verify(registrationClient).register(eq("https://example.org/example"), isA(Application.class));
    assertEquals("Register", defaultApplicationRegistrator.getRegisteredId());
    assertTrue(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register()}
   */
  @Test
  @DisplayName("Test register()")
  @Tag("MaintainedByDiffblue")
  void testRegister3() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    Application buildResult = Application.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .build();
    when(applicationFactory.createApplication()).thenReturn(buildResult);
    RegistrationClient registrationClient = mock(RegistrationClient.class);
    when(registrationClient.register(Mockito.<String>any(), Mockito.<Application>any())).thenReturn("Register");
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(applicationFactory,
        registrationClient, new String[]{"https://example.org/example"}, false);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register();

    // Assert
    verify(applicationFactory).createApplication();
    verify(registrationClient).register(eq("https://example.org/example"), isA(Application.class));
    assertEquals("Register", defaultApplicationRegistrator.getRegisteredId());
    assertTrue(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register()}
   */
  @Test
  @DisplayName("Test register()")
  @Tag("MaintainedByDiffblue")
  void testRegister4() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    Application buildResult = Application.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .build();
    when(applicationFactory.createApplication()).thenReturn(buildResult);
    RegistrationClient registrationClient = mock(RegistrationClient.class);
    when(registrationClient.register(Mockito.<String>any(), Mockito.<Application>any())).thenReturn("Register");
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(applicationFactory,
        registrationClient, new String[]{"https://example.org/example", "Application registered itself as {}"}, false);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register();

    // Assert
    verify(applicationFactory).createApplication();
    verify(registrationClient, atLeast(1)).register(Mockito.<String>any(), isA(Application.class));
    assertEquals("Register", defaultApplicationRegistrator.getRegisteredId());
    assertTrue(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register()}
   */
  @Test
  @DisplayName("Test register()")
  @Tag("MaintainedByDiffblue")
  void testRegister5() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    Application buildResult = Application.builder()
        .healthUrl("https://example.org/example")
        .managementUrl("https://example.org/example")
        .name("Name")
        .serviceUrl("https://example.org/example")
        .build();
    when(applicationFactory.createApplication()).thenReturn(buildResult);
    RegistrationClient registrationClient = mock(RegistrationClient.class);
    when(registrationClient.register(Mockito.<String>any(), Mockito.<Application>any())).thenReturn("Register");
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(applicationFactory,
        registrationClient, new String[]{"https://example.org/example", "Application registered itself as {}",
            "https://example.org/example"},
        false);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register();

    // Assert
    verify(applicationFactory).createApplication();
    verify(registrationClient, atLeast(1)).register(Mockito.<String>any(), isA(Application.class));
    assertEquals("Register", defaultApplicationRegistrator.getRegisteredId());
    assertTrue(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register(Application, String, boolean)} with {@code Application}, {@code String}, {@code boolean}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register(Application, String, boolean)}
   */
  @Test
  @DisplayName("Test register(Application, String, boolean) with 'Application', 'String', 'boolean'")
  @Tag("MaintainedByDiffblue")
  void testRegisterWithApplicationStringBoolean() throws RestClientException {
    // Arrange
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(Mockito.<String>any(), Mockito.<HttpMethod>any(), Mockito.<HttpEntity<?>>any(),
        Mockito.<ParameterizedTypeReference<Object>>any(), isA(Object[].class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(
        mock(ApplicationFactory.class), new BlockingRegistrationClient(restTemplate),
        new String[]{"https://example.org/example"}, true);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register(new Application("Name",
        "https://example.org/example", "https://example.org/example", "https://example.org/example", new HashMap<>()),
        "https://example.org/example", true);

    // Assert
    verify(restTemplate).exchange(eq("https://example.org/example"), isA(HttpMethod.class), isA(HttpEntity.class),
        isA(ParameterizedTypeReference.class), isA(Object[].class));
    assertNull(defaultApplicationRegistrator.getRegisteredId());
    assertFalse(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register(Application, String, boolean)} with {@code Application}, {@code String}, {@code boolean}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register(Application, String, boolean)}
   */
  @Test
  @DisplayName("Test register(Application, String, boolean) with 'Application', 'String', 'boolean'")
  @Tag("MaintainedByDiffblue")
  void testRegisterWithApplicationStringBoolean2() {
    // Arrange
    RegistrationClient registrationClient = mock(RegistrationClient.class);
    when(registrationClient.register(Mockito.<String>any(), Mockito.<Application>any())).thenReturn("Register");
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(
        mock(ApplicationFactory.class), registrationClient, new String[]{"https://example.org/example"}, true);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register(new Application("Name",
        "https://example.org/example", "https://example.org/example", "https://example.org/example", new HashMap<>()),
        "https://example.org/example", true);

    // Assert
    verify(registrationClient).register(eq("https://example.org/example"), isA(Application.class));
    assertEquals("Register", defaultApplicationRegistrator.getRegisteredId());
    assertTrue(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#register(Application, String, boolean)} with {@code Application}, {@code String}, {@code boolean}.
   * <ul>
   *   <li>When {@code false}.</li>
   * </ul>
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#register(Application, String, boolean)}
   */
  @Test
  @DisplayName("Test register(Application, String, boolean) with 'Application', 'String', 'boolean'; when 'false'")
  @Tag("MaintainedByDiffblue")
  void testRegisterWithApplicationStringBoolean_whenFalse() throws RestClientException {
    // Arrange
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.exchange(Mockito.<String>any(), Mockito.<HttpMethod>any(), Mockito.<HttpEntity<?>>any(),
        Mockito.<ParameterizedTypeReference<Object>>any(), isA(Object[].class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    DefaultApplicationRegistrator defaultApplicationRegistrator = new DefaultApplicationRegistrator(
        mock(ApplicationFactory.class), new BlockingRegistrationClient(restTemplate),
        new String[]{"https://example.org/example"}, true);

    // Act
    boolean actualRegisterResult = defaultApplicationRegistrator.register(new Application("Name",
        "https://example.org/example", "https://example.org/example", "https://example.org/example", new HashMap<>()),
        "https://example.org/example", false);

    // Assert
    verify(restTemplate).exchange(eq("https://example.org/example"), isA(HttpMethod.class), isA(HttpEntity.class),
        isA(ParameterizedTypeReference.class), isA(Object[].class));
    assertNull(defaultApplicationRegistrator.getRegisteredId());
    assertFalse(actualRegisterResult);
  }

  /**
   * Test {@link DefaultApplicationRegistrator#getRegisteredId()}.
   * <p>
   * Method under test: {@link DefaultApplicationRegistrator#getRegisteredId()}
   */
  @Test
  @DisplayName("Test getRegisteredId()")
  @Tag("MaintainedByDiffblue")
  void testGetRegisteredId() {
    // Arrange
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);

    // Act and Assert
    assertNull(
        new DefaultApplicationRegistrator(applicationFactory, new BlockingRegistrationClient(mock(RestTemplate.class)),
            new String[]{"https://example.org/example"}, true).getRegisteredId());
  }
}
