import { fireEvent, screen, waitFor } from '@testing-library/vue';
import { flushPromises } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { expect, it, vi } from 'vitest';

import Metrics from './index.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

it('keeps the tags for the selected metric when an older request finishes last', async () => {
  const instance = new Application(applications[0]).instances[0];
  const fetchMetric = vi.spyOn(instance, 'fetchMetric');
  const pending = Promise.withResolvers<void>();
  server.use(
    http.get('*/actuator/metrics', () =>
      HttpResponse.json(
        { names: ['first', 'second'] },
        {
          headers: {
            'Content-Type': 'application/vnd.spring-boot.actuator.v3+json',
          },
        },
      ),
    ),
    http.get('*/actuator/metrics/:name', async ({ params }) => {
      if (params.name === 'first') await pending.promise;
      return HttpResponse.json({
        availableTags: [{ tag: String(params.name) + '-tag', values: ['one'] }],
      });
    }),
  );
  render(Metrics, { props: { instance } });
  const selector = await screen.findByRole('combobox');
  await waitFor(() => expect(fetchMetric).toHaveBeenCalledWith('first'));

  await fireEvent.update(selector, 'second');
  expect(await screen.findByText('second-tag')).toBeVisible();
  pending.resolve();
  await fetchMetric.mock.results[0].value;
  await flushPromises();

  expect(screen.getByText('second-tag')).toBeVisible();
  expect(screen.queryByText('first-tag')).not.toBeInTheDocument();
});
