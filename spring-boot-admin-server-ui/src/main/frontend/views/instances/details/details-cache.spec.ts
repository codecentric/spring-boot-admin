import { screen, waitFor } from '@testing-library/vue';
import { enableAutoUnmount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { instance as instanceData } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
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
  const INSTANCE_ID = instanceData.id;
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

  const renderComponent = async (stubs = {}, instanceId = INSTANCE_ID) => {
    return render(DetailsCache, {
      global: {
        stubs: { cacheChart: stubChart, ...stubs },
      },
      props: {
        instanceId,
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
    ).toHaveTextContent('75.00%');
  });

  it('should calculate total', async () => {
    const { container } = await renderComponent();

    await waitFor(
      () => {
        const el = container.querySelector('[data-test="chart"]');
        expect(el).toBeTruthy();
        const parsed = JSON.parse((el?.textContent as string) || '[]');
        expect(parsed).toHaveLength(3);
      },
      { timeout: 2000 },
    );

    const chartData = JSON.parse(
      (container.querySelector('[data-test="chart"]')?.textContent as string) ||
        '[]',
    );

    for (let i = 0; i < chartData.length; i++) {
      expect(chartData[i].total).toEqual(TOTAL[i]);
    }
  });

  it('should calculate hits, misses and total per interval', async () => {
    const { container } = await renderComponent();

    await waitFor(
      () => {
        const el = container.querySelector('[data-test="chart"]');
        expect(el).toBeTruthy();
        const parsed = JSON.parse((el?.textContent as string) || '[]');
        expect(parsed).toHaveLength(3);
      },
      { timeout: 2000 },
    );

    const chartData = JSON.parse(
      (container.querySelector('[data-test="chart"]')?.textContent as string) ||
        '[]',
    );

    for (let i = 0; i < chartData.length; i++) {
      expect(chartData[i].hitsPerInterval).toEqual(HITS_PER_INTERVAL[i]);
      expect(chartData[i].missesPerInterval).toEqual(MISSES_PER_INTERVAL[i]);
      expect(chartData[i].totalPerInterval).toEqual(TOTAL_PER_INTERVAL[i]);
    }
  });

  it('should reinitialize metrics when instanceId changes', async () => {
    const { getByText, rerender, queryByText } = await renderComponent();

    await waitFor(() => {
      expect(getByText(`${HITS[0]}`)).toBeTruthy();
    });

    await rerender({
      instanceId: 'other-instance-id',
      cacheName: CACHE_NAME,
      index: 0,
    });

    await waitFor(() => {
      expect(queryByText(`${HITS[0]}`)).not.toBeInTheDocument();
    });
  });
});
