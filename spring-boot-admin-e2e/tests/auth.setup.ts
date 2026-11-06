import { signIn, test as setup } from './fixtures';

setup('sign in and store session', async ({ page }) => {
  await signIn(page);
  await page.context().storageState({ path: setup.info().project.metadata.authFile });
});
