import { credentials, expect, signIn, test } from './fixtures';

/*
 * Login and logout with Spring Security, including a servlet context path.
 * Regressions: #5438 (login button did not submit), #3828/#3829 (404 after logout + login with
 * context path), #5188 (requests ignoring the context path).
 *
 * These tests start without the stored session: they exercise the login itself, and logging out
 * must not invalidate the session that all other tests share.
 */
test.use({ storageState: { cookies: [], origins: [] } });

test.describe('security', () => {
  test.beforeEach(({ scenario }) => {
    test.skip(!scenario.secure, 'only relevant for secured scenarios');
  });

  test('unauthenticated users are redirected to the login page', async ({
    adminPage: page,
    scenario,
  }) => {
    await page.goto('applications');
    await expect(page).toHaveURL(new RegExp(`${scenario.contextPath}/login`));
    await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
  });

  test('wrong credentials show an error', async ({ adminPage: page }) => {
    await page.goto('login');
    await page.getByLabel('Username').fill(credentials.username);
    await page.getByLabel('Password').fill('wrong');
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page.getByText('Invalid username or password')).toBeVisible();
  });

  test('login, logout and login again', async ({ adminPage: page, scenario }) => {
    await signIn(page);
    await expect(page).toHaveURL(new RegExp(`${scenario.contextPath}/applications`));

    await page.getByRole('button', { name: `User menu of ${credentials.username}` }).click();
    await page.getByRole('menuitem', { name: 'Log out' }).click();
    await expect(page).toHaveURL(new RegExp(`${scenario.contextPath}/login`));
    await expect(page.getByText('Logout successful')).toBeVisible();

    await signIn(page);
    await expect(page.getByText('spring-boot-admin-sample-servlet').first()).toBeVisible();
  });

  test('a deep link is restored after login', async ({ adminPage: page, scenario }) => {
    await page.goto('wallboard');
    await expect(page).toHaveURL(/\/login/);
    await page.getByLabel('Username').fill(credentials.username);
    await page.getByLabel('Password').fill(credentials.password);
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).toHaveURL(new RegExp(`${scenario.contextPath}/wallboard`));
    await expect(page.getByRole('combobox', { name: 'Filter' })).toBeVisible();
  });
});
