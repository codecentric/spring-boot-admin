import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { expect, it } from 'vitest';

import DetailsCaches from './details-caches.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

it.each([
  { tag: 'cache', additionalTags: [] },
  { tag: 'name', additionalTags: [] },
  { tag: 'cache', additionalTags: [{ tag: 'name', values: ['sessions'] }] },
])(
  'loads cache measurements with $tag and additional tags $additionalTags',
  async ({ tag, additionalTags }) => {
    const queries: string[][] = [];
    server.use(
      http.get('*/actuator/metrics/:metric', ({ request, params }) => {
        const tags = new URL(request.url).searchParams.getAll('tag');
        if (tags.length)
          queries.push([String(params.metric), ...new Set(tags)]);
        return HttpResponse.json({
          measurements: [
            { statistic: 'COUNT', value: tags.includes('result:hit') ? 24 : 8 },
          ],
          availableTags: [
            {
              tag,
              values: additionalTags.length
                ? ['products', 'sessions']
                : ['products'],
            },
            ...additionalTags,
            { tag: 'result', values: ['hit', 'miss'] },
          ],
        });
      }),
    );
    const instance = new Application(applications[0]).instances[0];
    instance.availableMetrics = ['cache.gets', 'cache.size'];
    render(DetailsCaches, {
      props: { instance },
      global: { stubs: { cacheChart: true } },
    });
    expect(
      (await screen.findAllByLabelText('instances.details.cache.hits'))[0],
    ).toHaveTextContent('24');
    if (additionalTags.length) {
      expect(await screen.findAllByText('Cache: sessions')).toHaveLength(1);
      expect(queries).toContainEqual([
        'cache.gets',
        'name:sessions',
        'result:hit',
      ]);
    }
    expect(queries).toEqual(
      expect.arrayContaining([
        ['cache.gets', tag + ':products', 'result:hit'],
        ['cache.gets', tag + ':products', 'result:miss'],
        ['cache.size', tag + ':products'],
      ]),
    );
  },
);
