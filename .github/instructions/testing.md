# Testing Conventions

## Structure: Given / When / Then

**All tests must use Given / When / Then structure.** Use inline comments to delimit the three phases clearly.

```java
// Java (JUnit 5)
@Test
void should_change_status_when_health_endpoint_returns_up() {
    // Given
    Instance instance = Instance.create(InstanceId.of("id"))
        .register(Registration.create("foo", wireMock.url("/health")).build());
    wireMock.stubFor(get("/health").willReturn(okForContentType(APPLICATION_JSON_VALUE, "{\"status\":\"UP\"}")));

    // When
    updater.updateStatus(instance.getId()).block();

    // Then
    StepVerifier.create(repository.find(instance.getId()))
        .assertNext((saved) -> assertThat(saved.getStatusInfo().getStatus()).isEqualTo("UP"))
        .verifyComplete();
}
```

```typescript
// TypeScript (Vitest)
it('should display error message when login fails', async () => {
  // Given
  server.use(http.post('/api/login', () => HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })));

  // When
  render(LoginForm);
  await userEvent.click(screen.getByRole('button', { name: /login/i }));

  // Then
  expect(screen.getByText('Unauthorized')).toBeVisible();
});
```

## Rules

- Every test method / `it()` block **must** contain exactly the three comments `// Given`, `// When`, `// Then` in that order.
- Setup shared across multiple tests goes in `@BeforeEach` / `beforeEach` — still add a `// Given` comment inside the test for test-specific state.
- Do **not** mix assertions into the When phase.
- If there is no meaningful setup, write `// Given` with no code below it (e.g. testing invariants / pure functions), do not omit the comment.

## Meaningful Tests Only

Only test **behaviour and logic**. Do not write tests for code that has no logic to verify.

**Do not test:**
- Plain getters / setters with no logic
- Constructors that only assign fields
- `toString()`, `equals()`, `hashCode()` unless the implementation contains custom logic

**Do test:**
- State transitions and side effects
- Conditional logic and branching
- Error handling and boundary conditions
- Integration between components

> If a test only proves that Java/the framework works as designed, delete it.

## DRY Test Setup — Factory Functions

Apply the DRY principle to test data creation. **Never instantiate domain objects (e.g. `Application`, `Instance`, `Registration`) inline in multiple places.** Extract a shared factory function instead.

**Wrong — duplicated construction scattered across tests:**

```java
// in TestA
Instance instance = Instance.create(InstanceId.of("abc"))
    .register(Registration.create("my-app", "http://localhost/health").build());

// in TestB — same pattern, different values wired differently
Instance instance = Instance.create(InstanceId.of("xyz"))
    .register(Registration.create("other-app", "http://other/health").build());
```

**Right — one factory, called from every test that needs it:**

```java
// TestFixtures.java (shared across test classes)
static Instance anInstance(String id, String name, String healthUrl) {
    return Instance.create(InstanceId.of(id))
        .register(Registration.create(name, healthUrl).build());
}

// Sensible defaults for the common case
static Instance anInstance() {
    return anInstance("test-id", "test-app", "http://localhost/health");
}
```

```typescript
// fixtures.ts (shared across spec files)
export function anApplication(overrides?: Partial<ApplicationOptions>): Application {
  return new Application({
    name: 'test-app',
    instances: [anInstance()],
    ...overrides,
  });
}

export function anInstance(overrides?: Partial<InstanceOptions>): Instance {
  return new Instance({ id: 'test-id', status: 'UP', ...overrides });
}
```

**Rules:**
- One canonical factory per domain type — never create a second construction pattern for the same type.
- Factories live in a shared file (`TestFixtures.java`, `fixtures.ts`, or equivalent) visible to all tests in the module.
- Factories provide sensible defaults; accept overrides for the values that vary per test.
- When an existing factory does not cover a new variation, **extend the factory** — do not inline a new construction.

## Naming

| Layer | Convention | Example |
|---|---|---|
| Java | `should_<outcome>_when_<condition>()` | `should_return_down_when_health_check_fails()` |
| TypeScript | `'should <outcome> when <condition>'` | `'should show error when request times out'` |

## Java (JUnit 5)

- Test classes are package-private (no `public` modifier).
- Use AssertJ (`assertThat`, `assertThatThrownBy`) — never JUnit built-in assertions.
- Use `StepVerifier` for reactive (Mono / Flux) assertions.
- Use `Mockito.mock()` for unit-level mocking; WireMock for HTTP-level mocking.
- Parameterized tests: use `@ParameterizedTest` with `@MethodSource` or `@CsvSource`; apply Given/When/Then inside the test body.

## TypeScript (Vitest)

- Use `describe` + `it('should ...')` nesting.
- Use `vi.mock()` / `vi.fn()` / `vi.spyOn()` for mocking.
- Use `@testing-library/vue` (`render`, `screen`) for component tests.
- Use `it.each` tagged templates for parameterized tests; apply Given/When/Then inside the callback.
- MSW (`server.use(http.get(...))`) for HTTP-level mocking.

## Running Tests

See [build-commands.md](build-commands.md) for test execution commands and timing.
