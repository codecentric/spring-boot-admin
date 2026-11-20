import { Locator, Page } from '@playwright/test';
import { expect, sampleInstanceId, test } from './fixtures';

/*
 * Every view of the instance sidebar must render its content without JS errors.
 * Catches "view is completely broken" regressions such as #5195 (Quartz), #4071 (JMX),
 * #4383 (Mappings gone), #4239 (Scheduled tasks), #5263 (sidebar navigation).
 *
 * Each view has a marker that only appears once its data has been loaded and rendered.
 */
const views: Record<string, (main: Locator) => Locator> = {
  details: (main) => main.getByRole('heading', { name: /^Info/ }),
  metrics: (main) => main.getByRole('combobox', { name: 'Metrics' }),
  quartz: (main) => main.getByRole('heading', { name: 'Jobs' }),
  env: (main) => main.getByRole('heading', { name: 'Environment Manager' }),
  beans: (main) => main.getByText('adminHandlerMapping', { exact: true }),
  configprops: (main) => main.getByRole('heading', { name: /AdminServerProperties$/ }),
  conditions: (main) => main.getByRole('heading', { name: 'Positive matches' }),
  scheduledtasks: (main) => main.getByText('No scheduled tasks present.'),
  dependencies: (main) => main.getByRole('heading', { name: /^application \(\d+\/\d+\)$/ }),
  sbom: (main) => main.getByText('Legend', { exact: true }),
  logfile: (main) => main.getByRole('button', { name: 'Download' }),
  loggers: (main) => main.getByRole('heading', { name: 'Logging Groups' }),
  jolokia: (main) => main.getByRole('button', { name: 'java.lang', exact: true }),
  threaddump: (main) => main.getByRole('row', { name: /Reference Handler/ }),
  startup: (main) =>
    main.getByRole('listitem').filter({ hasText: 'spring.boot.application.ready' }),
  mappings: (main) => main.getByRole('columnheader', { name: 'dispatcherServlet' }),
  httpexchanges: (main) => main.getByRole('checkbox', { name: 'exclude /actuator/**' }),
  auditevents: (main) => main.getByRole('combobox', { name: 'Principal' }),
  custom: (main) => main.getByText('Output: Hello World!'),
};

test.describe('instance views', () => {
  let instanceId: string;

  test.beforeAll(async ({ api }) => {
    instanceId = await sampleInstanceId(api);
  });

  test('sidebar offers exactly the expected views', async ({ adminPage: page }) => {
    await page.goto(`instances/${instanceId}/details`);
    expect(await sidebarViews(page, instanceId)).toEqual(Object.keys(views).sort());
  });

  for (const [view, marker] of Object.entries(views)) {
    test(`${view} view renders`, async ({ adminPage: page }) => {
      await page.goto(`instances/${instanceId}/${view}`);

      const main = page.getByRole('main');
      await expect(marker(main).first()).toBeVisible();
      await expect(page.getByTestId('instance-section-loading-spinner')).toHaveCount(0);
      await expect(main).not.toContainText('[object Object]');
    });
  }
});

/** Collects the view names linked in the instance sidebar. Groups form an accordion, so open each one. */
async function sidebarViews(page: Page, instanceId: string): Promise<string[]> {
  const sidebar = page.getByRole('complementary', { name: 'Instance navigation' });
  await expect(sidebar.getByText(instanceId)).toBeVisible();

  const found = new Set<string>();
  const collect = async () => {
    const paths = await sidebar
      .getByRole('link')
      .evaluateAll((links) => links.map((a) => (a as HTMLAnchorElement).pathname));
    paths
      .filter((p) => p.includes(`/instances/${instanceId}/`))
      .map((p) => p.split(`/instances/${instanceId}/`)[1])
      .filter((v) => !v.startsWith('custom-link/'))
      .forEach((v) => found.add(v));
  };

  await collect();
  // Opening a group collapses the others, so read all group names first and then open them by name.
  const toggles = sidebar.getByRole('button', { name: /^(Expand|Collapse) / });
  const groupNames = (
    await toggles.evaluateAll((buttons) => buttons.map((b) => b.getAttribute('aria-label')!))
  ).map((label) => label.replace(/^(Expand|Collapse) /, ''));

  for (const name of groupNames) {
    const expand = sidebar.getByRole('button', { name: `Expand ${name}`, exact: true });
    if (await expand.isVisible()) {
      await expand.click();
    }
    await expect(
      sidebar.getByRole('button', { name: `Collapse ${name}`, exact: true }),
    ).toHaveAttribute('aria-expanded', 'true');
    await collect();
  }
  return [...found].sort();
}
