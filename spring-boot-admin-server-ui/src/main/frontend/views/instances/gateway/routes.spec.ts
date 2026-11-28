import { fireEvent, screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { expect, it } from 'vitest';

import Routes from './routes.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

it('finds routes by destination, predicate and filter in the verbose and legacy formats', async () => {
  server.use(
    http.get('*/actuator/gateway/routes', () =>
      HttpResponse.json([
        {
          route_id: 'redirect',
          uri: 'lb://catalog',
          predicate: 'Paths: [/products/**]',
          filters: ['StripPrefix=1'],
          order: 1,
        },
        {
          route_id: 'legacy',
          route_definition: {
            uri: 'lb://archive',
            predicates: [{ name: 'Path', args: { pattern: '/old/**' } }],
            filters: [],
          },
          order: 2,
        },
      ]),
    ),
  );
  render(Routes, {
    props: { instance: new Application(applications[0]).instances[0] },
  });
  await screen.findByText('redirect');
  const search = screen.getByRole('searchbox');
  for (const term of ['CATALOG', '/products/', 'StripPrefix']) {
    await fireEvent.update(search, term);
    expect(screen.getByText('redirect')).toBeVisible();
    expect(screen.queryByText('legacy')).not.toBeInTheDocument();
  }
  await fireEvent.update(search, '/old/');
  expect(screen.getByText('legacy')).toBeVisible();
  expect(screen.queryByText('redirect')).not.toBeInTheDocument();
});
