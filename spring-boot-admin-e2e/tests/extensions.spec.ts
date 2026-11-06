import { EXTERNAL_STUB_TEXT, expect, sampleInstanceId, test } from './fixtures';

/*
 * The UI extension API and external views are a public contract that regressed several times:
 * #5724 (extension views without route), #4259 (children of external iframe views),
 * #2968 (external view dropdown), #2921 (iframe styling), #4650 (custom view not visible).
 *
 * External pages are stubbed by the `context` fixture, so iframes show EXTERNAL_STUB_TEXT.
 */
test.describe('UI extensions', () => {
  test('top-level custom view from the custom-ui sample is rendered', async ({
    adminPage: page,
    api,
  }) => {
    await sampleInstanceId(api);
    await page.goto('applications');

    const nav = page.getByRole('navigation');
    await nav.getByRole('button', { name: /My Extensions/ }).click();
    await nav.getByRole('menuitem', { name: 'Custom Sub' }).click();

    await expect(page).toHaveURL(/\/customSub$/);
    // The child is rendered within its parent "custom" view, which lists the registered instances.
    await expect(
      page.getByText('This application has the following instances:').first(),
    ).toBeVisible();

    // The "My Extensions" view itself is registered with path /custom.
    await page.goto('custom');
    await expect(
      page.getByText('This application has the following instances:').first(),
    ).toBeVisible();
  });

  test('instance custom view is rendered', async ({ adminPage: page, api }) => {
    const id = await sampleInstanceId(api);
    await page.goto(`instances/${id}/details`);

    await page
      .getByRole('complementary', { name: 'Instance navigation' })
      .getByRole('link', { name: 'Custom', exact: true })
      .click();
    await expect(page).toHaveURL(new RegExp(`/instances/${id}/custom$`));
    await expect(page.getByRole('main').getByText('Output: Hello World!')).toBeVisible();
  });

  test('external view dropdown lists its children', async ({ adminPage: page }) => {
    await page.goto('applications');
    const nav = page.getByRole('navigation');

    await nav.getByRole('button', { name: 'Show entries of Link w children' }).click();
    await expect(nav.getByRole('menuitem', { name: '📖 Docs' })).toBeVisible();
    await expect(nav.getByRole('menuitem', { name: '🐙 GitHub' })).toBeVisible();
  });

  test('external iframe view is embedded', async ({ adminPage: page }) => {
    await page.goto('applications');
    await page.getByRole('navigation').getByRole('link', { name: '🎅 Is it christmas' }).click();

    const iframe = page.getByTitle('🎅 Is it christmas');
    await expect(iframe).toBeVisible();
    await expect(iframe.contentFrame().getByText(EXTERNAL_STUB_TEXT)).toBeVisible();
  });

  test('sidebar link with iframe opens inside SBA', async ({ adminPage: page, api }) => {
    const id = await sampleInstanceId(api);
    await page.goto(`instances/${id}/details`);

    await page
      .getByRole('complementary', { name: 'Instance navigation' })
      .getByRole('link', { name: /very long iframe sidebar link/ })
      .click();
    await expect(page).toHaveURL(new RegExp(`/instances/${id}/custom-link/`));

    const iframe = page.getByTitle('https://docs.spring-boot-admin.com');
    await expect(iframe).toBeVisible();
    await expect(iframe.contentFrame().getByText(EXTERNAL_STUB_TEXT)).toBeVisible();
  });
});
