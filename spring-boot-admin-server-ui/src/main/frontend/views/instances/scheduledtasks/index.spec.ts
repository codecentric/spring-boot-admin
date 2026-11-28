import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { expect, it } from 'vitest';

import ScheduledTasks from './index.vue';

import { applications } from '@/mocks/applications/data';
import { scheduledtasksResponse } from '@/mocks/instance/scheduledtasks/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

it('shows a task registered with a custom trigger when there are no periodic tasks', async () => {
  server.use(
    http.get('*/actuator/scheduledtasks', () =>
      HttpResponse.json({
        cron: [],
        fixedRate: [],
        fixedDelay: [],
        custom: [
          {
            runnable: { target: 'com.example.RefreshCatalog' },
            trigger: 'CatalogTrigger',
            lastExecution: { status: 'SUCCESS', time: '2025-03-04T10:15:00Z' },
          },
        ],
      }),
    ),
  );
  render(ScheduledTasks, {
    props: { instance: new Application(applications[0]).instances[0] },
  });
  expect(await screen.findByText('CatalogTrigger')).toBeVisible();
  expect(screen.getByTitle('com.example.RefreshCatalog')).toBeVisible();
  expect(screen.getByRole('status')).toHaveTextContent('SUCCESS');
  expect(
    screen.queryByText('instances.scheduledtasks.no_scheduledtasks'),
  ).not.toBeInTheDocument();
});

it('shows cron and custom triggers together', async () => {
  server.use(
    http.get('*/actuator/scheduledtasks', () =>
      HttpResponse.json(scheduledtasksResponse),
    ),
  );
  render(ScheduledTasks, {
    props: { instance: new Application(applications[0]).instances[0] },
  });
  expect(
    await screen.findByTitle(scheduledtasksResponse.cron[0].expression),
  ).toBeVisible();
  expect(
    screen.getByText(scheduledtasksResponse.custom[0].trigger),
  ).toBeVisible();
});
