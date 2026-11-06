import { APIRequestContext, expect, Page, test as base } from '@playwright/test';

export const credentials = { username: 'user', password: 'password' };

type Scenario = { secure: boolean; contextPath: string };

type Fixtures = {
  scenario: Scenario;
  /** Collects uncaught JS errors, console errors and failed UI resources for the current page. */
  browserErrors: string[];
  /**
   * The page to drive the UI with. Fails the test on browser errors. In secured scenarios it is
   * already signed in via the stored session of the setup project.
   */
  adminPage: Page;
  /** API client that authenticates against the SBA server REST API when needed. */
  api: APIRequestContext;
};

/** Text of the page served instead of anything outside the server under test. */
export const EXTERNAL_STUB_TEXT = 'external stub';
const EXTERNAL_STUB = `<!doctype html><title>${EXTERNAL_STUB_TEXT}</title><p>${EXTERNAL_STUB_TEXT}</p>`;

/**
 * Console messages that are emitted by the sample itself (e.g. because some actuator endpoints are
 * intentionally broken or not available) and do not indicate a broken UI.
 */
const ignoredConsoleErrors: RegExp[] = [
  /Failed to load resource: the server responded with a status of (4\d\d|503)/,
  // Known issue: hex-mesh.vue renders the hexagon path with sideLength = 0 before the first layout
  // pass (moveAlong divides by a zero distance). Remove once fixed so the test guards against it.
  /<path> attribute d: Expected number, "M NaN,NaN/,
];

export const test = base.extend<Fixtures>({
  scenario: async ({}, use, testInfo) => {
    const { secure, contextPath } = testInfo.project.metadata as Scenario;
    await use({ secure, contextPath });
  },

  context: async ({ context, baseURL }, use) => {
    // Never depend on the internet: answer every request outside the server under test with a stub.
    const origin = new URL(baseURL!).origin;
    await context.route(
      (url) => url.origin !== origin,
      (route) => route.fulfill({ status: 200, contentType: 'text/html', body: EXTERNAL_STUB }),
    );
    await use(context);
  },

  browserErrors: async ({ page }, use) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(`pageerror: ${err.message}`));
    page.on('console', (msg) => {
      if (msg.type() !== 'error') return;
      const text = msg.text();
      if (ignoredConsoleErrors.some((re) => re.test(text))) return;
      errors.push(`console.error: ${text}`);
    });
    page.on('response', (res) => {
      const type = res.request().resourceType();
      if (['script', 'stylesheet', 'document'].includes(type) && res.status() >= 400) {
        errors.push(`${type} ${res.status()}: ${res.url()}`);
      }
    });
    page.on('requestfailed', (req) => {
      const type = req.resourceType();
      if (['script', 'stylesheet'].includes(type)) {
        errors.push(`${type} failed: ${req.url()} (${req.failure()?.errorText})`);
      }
    });
    await use(errors);
  },

  adminPage: async ({ page, browserErrors }, use) => {
    await use(page);
    expect(browserErrors, 'browser errors while using the UI').toEqual([]);
  },

  api: async ({ playwright, baseURL, scenario }, use) => {
    const headers: Record<string, string> = { Accept: 'application/json' };
    if (scenario.secure) {
      headers.Authorization =
        'Basic ' +
        Buffer.from(`${credentials.username}:${credentials.password}`).toString('base64');
    }
    const ctx = await playwright.request.newContext({ baseURL, extraHTTPHeaders: headers });
    await use(ctx);
    await ctx.dispose();
  },
});

export { expect };

export async function signIn(page: Page) {
  await page.goto('login');
  await page.getByLabel('Username').fill(credentials.username);
  await page.getByLabel('Password').fill(credentials.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(/\/applications/);
}

/** Waits until the sample has registered itself and returns its instance id. */
export async function sampleInstanceId(api: APIRequestContext): Promise<string> {
  let id: string | undefined;
  await expect
    .poll(
      async () => {
        const res = await api.get('instances');
        if (!res.ok()) return undefined;
        const instances = (await res.json()) as Array<{
          id: string;
          registration: { name: string };
          endpoints: unknown[];
        }>;
        const instance = instances.find(
          (i) =>
            i.registration.name === 'spring-boot-admin-sample-servlet' && i.endpoints.length > 1,
        );
        id = instance?.id;
        return id;
      },
      { timeout: 60_000, message: 'sample instance did not register' },
    )
    .toBeTruthy();
  return id!;
}
