import { expect, sampleInstanceId, test } from './fixtures';

/*
 * Guards against releases where the UI does not load at all or the main pages are blank,
 * e.g. #4646, #4649, #4831, #4986, #5662, #5251.
 */
test.describe('application shell', () => {
  test('server-rendered resources are delivered', async ({ api }) => {
    for (const path of ['', 'sba-settings.js', 'variables.css']) {
      const res = await api.get(path, { headers: { Accept: '*/*' } });
      expect(res.status(), `GET /${path}`).toBe(200);
    }
    const settings = await (
      await api.get('sba-settings.js', { headers: { Accept: '*/*' } })
    ).text();
    expect(settings).toContain('SBA');
  });

  test('applications list shows the registered sample', async ({ adminPage: page, api }) => {
    await sampleInstanceId(api);
    await page.goto('applications');

    await expect(page.getByRole('navigation').getByText('Spring Boot Admin')).toBeVisible();
    await expect(page.getByText('spring-boot-admin-sample-servlet').first()).toBeVisible();
    await expect(page.getByText('1 instance')).toBeVisible();
  });

  test('wallboard renders applications and opens instance details', async ({
    adminPage: page,
    api,
  }) => {
    const id = await sampleInstanceId(api);
    await page.goto('wallboard');

    const hex = page.getByRole('button', { name: /^spring-boot-admin-sample-servlet \(/ });
    await expect(hex).toBeVisible();
    await hex.click();

    // #4272 / #4400: clicking a hexagon navigated to "instances/[object Object]/details"
    await expect(page).toHaveURL(new RegExp(`/instances/${id}/details`));
    await expect(
      page.getByRole('heading', { name: 'spring-boot-admin-sample-servlet' }),
    ).toBeVisible();
    await expect(page.locator('body')).not.toContainText('[object Object]');
  });

  test('about page renders', async ({ adminPage: page }) => {
    await page.goto('about');
    await expect(page.getByRole('heading', { name: 'About Spring Boot Admin' })).toBeVisible();
    await expect(page.getByRole('heading', { name: /^Version \S+/ })).toBeVisible();
  });

  test('deep link survives a full page reload', async ({ adminPage: page, api }) => {
    const id = await sampleInstanceId(api);
    await page.goto(`instances/${id}/details`);
    await expect(
      page.getByRole('heading', { name: 'spring-boot-admin-sample-servlet' }),
    ).toBeVisible();

    await page.reload();
    await expect(page).toHaveURL(new RegExp(`/instances/${id}/details`));
    await expect(
      page.getByRole('heading', { name: 'spring-boot-admin-sample-servlet' }),
    ).toBeVisible();
  });
});
