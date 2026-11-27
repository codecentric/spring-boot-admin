import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { expect, it } from 'vitest';

import Quartz from './index.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

const showQuartz = () =>
  render(Quartz, {
    props: { instance: new Application(applications[0]).instances[0] },
  });

it('shows the empty state for a successfully loaded empty scheduler', async () => {
  server.use(
    http.get('*/actuator/quartz/:kind', () =>
      HttpResponse.json({ groups: {} }),
    ),
  );
  showQuartz();
  expect(await screen.findByText('instances.quartz.no_data')).toBeVisible();
});

it('keeps jobs visible and reports a failed trigger lookup', async () => {
  server.use(
    http.get('*/actuator/quartz/jobs', () =>
      HttpResponse.json({ groups: { main: { jobs: ['daily-job'] } } }),
    ),
    http.get('*/actuator/quartz/jobs/main/daily-job', () =>
      HttpResponse.json({
        group: 'main',
        name: 'daily-job',
        className: 'example.DailyJob',
        triggers: [],
      }),
    ),
    http.get(
      '*/actuator/quartz/triggers',
      () => new HttpResponse(null, { status: 503 }),
    ),
  );
  showQuartz();
  expect(await screen.findByText(/503/)).toBeVisible();
  expect(screen.getByText('daily-job')).toBeVisible();
  expect(
    screen.queryByText('instances.quartz.no_data'),
  ).not.toBeInTheDocument();
});
