# Spring Boot Admin – E2E Smoke Tests

Playwright tests that start the real, packaged Spring Boot Admin (servlet sample including the
custom UI extension) and drive it in a browser.

They deliberately cover only what unit tests (Vitest, JUnit) cannot: the assembled application
— server-rendered `index.html`/`login.html`, the built JS bundle, Spring Security, context paths
and UI extensions. Business logic belongs in the unit tests of the respective module.

## Scenarios

Every test runs against each scenario (Playwright project). Both servers are started automatically.

| Project    | Port  | Configuration                                        |
| ---------- | ----- | ---------------------------------------------------- |
| `insecure` | 18765 | profile `insecure`, no context path                  |
| `secure`   | 18766 | profile `secure` (form login), context path `/admin` |

Secured scenarios have a `<name>-setup` project (`tests/auth.setup.ts`) that signs in once and
stores the session in `target/.auth/`. All tests of the scenario reuse that session. Only
`security.spec.ts` starts signed out, because it tests the login itself.

## Tests

| File                     | Covers                                                                                              | Related issues                    |
| ------------------------ | --------------------------------------------------------------------------------------------------- | --------------------------------- |
| `shell.spec.ts`          | UI loads, applications list, wallboard, about page, deep link reload                                | #4646, #4649, #4272, #4400, #5662 |
| `instance-views.spec.ts` | sidebar lists exactly the expected views; one test per view, each waiting for view-specific content | #5195, #4071, #4383, #4239, #4831 |
| `security.spec.ts`       | redirect to login, wrong credentials, login/logout/login, deep link                                 | #5438, #3828, #3829, #5188        |
| `extensions.spec.ts`     | custom views, external views (dropdown, iframe), sidebar links                                      | #5724, #4259, #2968, #4650        |

Every test that uses the `adminPage` fixture fails on uncaught exceptions, `console.error` output
and scripts/stylesheets that fail to load. Known, accepted messages are listed in
`tests/fixtures.ts` (`ignoredConsoleErrors`).

The tests never reach the internet: every request outside the server under test is answered with
a stub page (`context` fixture in `tests/fixtures.ts`). Iframes of external views show that stub.

When a view is added to or removed from the sample, update the `views` map in
`tests/instance-views.spec.ts`. The sidebar test fails until it matches.

## Running locally

```shell
# 1. Build the sample (once, or after changes to server/UI)
./mvnw install -DskipTests -pl spring-boot-admin-samples/spring-boot-admin-sample-servlet -am

# 2. Run the tests
cd spring-boot-admin-e2e
npm install
npx playwright install chromium
npm test
```

Useful variants:

```shell
npm run test:ui                         # interactive Playwright UI
npx playwright test --project=secure    # one scenario only
npm run report                          # open the last HTML report
npm run format:fix && npm run typecheck # before committing
```

Locally, already running servers on the scenario ports are reused. A different jar can be
tested with `SBA_SAMPLE_JAR=path/to/app.jar npm test`.

Reports, traces and server logs are written to `target/`.
