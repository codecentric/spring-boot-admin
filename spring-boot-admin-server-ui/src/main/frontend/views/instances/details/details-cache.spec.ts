import { screen, waitFor } from '@testing-library/vue';
import { enableAutoUnmount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsCache from '@/views/instances/details/details-cache.vue';

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        pollTimer: {
          cache: 100,
        },
      },
    },
  };
});

describe('DetailsCache', () => {
  enableAutoUnmount(afterEach);

  const CACHE_NAME = 'foo-cache';
  const HITS = [24.0, 32.0, 48.0];
  const MISSES = [8.0, 8.0, 16.0];
  const TOTAL = [32, 40, 64];
  const HITS_PER_INTERVAL = [0, 8, 16];
  const MISSES_PER_INTERVAL = [0, 0, 8];
  const TOTAL_PER_INTERVAL = [0, 8, 24];

  const stubChart = {
    props: ['data'],
    template: `
        <div data-test="chart">
          {{ JSON.stringify($props.data) }}
        </div>
      `,
  };

  const renderComponent = async (stubs = {}) => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    return render(DetailsCache, {
      global: {
        stubs,
      },
      props: {
        instance,
        cacheName: CACHE_NAME,
        index: 0,
      },
    });
  };

  beforeEach(() => {
    const hitsGenerator = (function* () {
      yield* HITS;
    })();
    const missesGenerator = (function* () {
      yield* MISSES;
    })();

    server.use(
      http.get(
        '/instances/:instanceId/actuator/metrics/cache.gets',
        ({ request }) => {
          const url = new URL(request.url);
          const tags = url.searchParams.getAll('tag');

          if (tags.includes('result:hit')) {
            return HttpResponse.json({
              measurements: [{ value: hitsGenerator.next()?.value }],
            });
          } else if (tags.includes('result:miss')) {
            return HttpResponse.json({
              measurements: [{ value: missesGenerator.next()?.value }],
            });
          }
        },
      ),
      http.get('/instances/:instanceId/actuator/metrics/cache.size', () => {
        return HttpResponse.json({
          measurements: [{ value: '1337' }],
        });
      }),
    );
  });

  it('should render cache name', async () => {
    await renderComponent();

    expect(
      await screen.findByRole('heading', { name: `Cache: ${CACHE_NAME}` }),
    ).toBeVisible();
  });

  it('should render hits and misses', async () => {
    await renderComponent();

    expect(
      await screen.findByLabelText('instances.details.cache.hits'),
    ).toHaveTextContent(`${HITS[0]}`);
    expect(
      await screen.findByLabelText('instances.details.cache.misses'),
    ).toHaveTextContent(`${MISSES[0]}`);
  });

  it('should calculate and render hit/miss ratio', async () => {
    await renderComponent();

    expect(
      await screen.findByLabelText('instances.details.cache.hit_ratio'),
    ).toHaveTextContent('75.00%'); // 24 hits, 8 misses
  });

  it('should calculate total', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    const { container } = await render(DetailsCache, {
      global: { stubs: { cacheChart: stubChart } },
      props: {
        instance,
        cacheName: CACHE_NAME,
        index: 0,
      },
    });

    // wait until chart stub receives at least 3 data points
    await waitFor(
      () => {
        const el = container.querySelector('[data-test="chart"]');
        expect(el).toBeTruthy();
        const text = (el?.textContent as string) || '[]';
        const parsed = JSON.parse(text);
        expect(parsed).toHaveLength(3);
      },
      { timeout: 2000 },
    );

    const chartText =
      (container.querySelector('[data-test="chart"]')?.textContent as string) ||
      '[]';
    const chartData = JSON.parse(chartText);

    for (let index = 0; index < chartData.length; index++) {
      expect(chartData[index].total).toEqual(TOTAL[index]);
    }
  });

  it('should calculate hits, misses and total per interval', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    const { container } = await render(DetailsCache, {
      global: { stubs: { cacheChart: stubChart } },
      props: {
        instance,
        cacheName: CACHE_NAME,
        index: 0,
      },
    });

    // wait until chart stub receives at least 3 data points
    await waitFor(
      () => {
        const el = container.querySelector('[data-test="chart"]');
        expect(el).toBeTruthy();
        const text = (el?.textContent as string) || '[]';
        const parsed = JSON.parse(text);
        expect(parsed).toHaveLength(3);
      },
      { timeout: 2000 },
    );

    const chartText2 =
      (container.querySelector('[data-test="chart"]')?.textContent as string) ||
      '[]';
    const chartData = JSON.parse(chartText2);

    for (let index = 0; index < chartData.length; index++) {
      expect(chartData[index].hitsPerInterval).toEqual(
        HITS_PER_INTERVAL[index],
      );
      expect(chartData[index].missesPerInterval).toEqual(
        MISSES_PER_INTERVAL[index],
      );
      expect(chartData[index].totalPerInterval).toEqual(
        TOTAL_PER_INTERVAL[index],
      );
    }
  });

  it('should reinitialize metrics when instance changes', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    const { getByText, rerender, queryByText } = await render(DetailsCache, {
      props: {
        instance,
        cacheName: CACHE_NAME,
        index: 0,
      },
    });

    // wait until initial fetch rendered a numeric value
    await waitFor(() => {
      expect(getByText(`${HITS[0]}`)).toBeTruthy();
    });

    // simulate switching to a different instance
    const newApp = new Application({
      name: 'Other',
      statusTimestamp: Date.now(),
      instances: [{ id: 'other-1', statusInfo: { status: 'UP' } }],
    });
    const newInstance = newApp.instances[0];

    await rerender({ instance: newInstance, cacheName: CACHE_NAME, index: 0 });

    // component should have reset its rendered data
    await waitFor(() => {
      expect(queryByText(`${HITS[0]}`)).not.toBeInTheDocument();
    });
  });
});
