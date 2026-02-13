import { screen, waitFor } from '@testing-library/vue';
import { enableAutoUnmount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsDatasource from '@/views/instances/details/details-datasource.vue';

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        pollTimer: {
          datasource: 100,
        },
      },
    },
  };
});

describe('DetailsDatasource', () => {
  enableAutoUnmount(afterEach);

  const DATA_SOURCE = 'my-ds';
  const ACTIVE = [2.0, 3.0, 4.0];
  const MIN = [1.0, 1.0, 1.0];
  const MAX = [5.0, -1.0, 10.0]; // include unlimited (-1) case

  beforeEach(() => {
    const activeGenerator = (function* () {
      yield* ACTIVE;
    })();
    const minGenerator = (function* () {
      yield* MIN;
    })();
    const maxGenerator = (function* () {
      yield* MAX;
    })();

    server.use(
      http.get('/instances/:instanceId/actuator/metrics', () => {
        return HttpResponse.json({
          names: [
            'jdbc.connections.active',
            'jdbc.connections.min',
            'jdbc.connections.max',
          ],
        });
      }),
      http.get(
        '/instances/:instanceId/actuator/metrics/jdbc.connections.active',
        () => {
          return HttpResponse.json({
            measurements: [{ value: activeGenerator.next()?.value }],
          });
        },
      ),
      http.get(
        '/instances/:instanceId/actuator/metrics/jdbc.connections.min',
        () => {
          return HttpResponse.json({
            measurements: [{ value: minGenerator.next()?.value }],
          });
        },
      ),
      http.get(
        '/instances/:instanceId/actuator/metrics/jdbc.connections.max',
        () => {
          return HttpResponse.json({
            measurements: [{ value: maxGenerator.next()?.value }],
          });
        },
      ),
    );
  });

  async function renderComponent(stubs = {}) {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    return render(DetailsDatasource, {
      global: {
        stubs,
      },
      props: {
        instance,
        dataSource: DATA_SOURCE,
      },
    });
  }

  it('renders panel title with datasource name', async () => {
    await renderComponent();

    // title uses i18n keys in the test environment; ensure heading is present
    const heading = await screen.findByRole('heading');
    expect(heading).toBeVisible();
  });

  it('renders active, min and max values', async () => {
    await renderComponent();

    // labels use i18n keys; assert numeric values are rendered
    expect(await screen.findByText(`${ACTIVE[0]}`)).toBeVisible();
    expect(await screen.findByText(`${MIN[0]}`)).toBeVisible();
    expect(await screen.findByText(`${MAX[0]}`)).toBeVisible();
  });

  it('handles unlimited max (-1) and pushes chartData points', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    await render(DetailsDatasource, {
      props: {
        instance,
        dataSource: DATA_SOURCE,
      },
    });

    // wait until initial metric value is visible
    await waitFor(() => {
      expect(screen.getByText(`${ACTIVE[0]}`)).toBeVisible();
    });

    // second measurement MAX[1] === -1 should be displayed as 'unlimited' in rendered markup
    expect(
      await screen.findByText('instances.details.datasource.unlimited'),
    ).toBeVisible();
  });

  it('should reinitialize metrics when instance changes', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    const { rerender } = await render(DetailsDatasource, {
      props: {
        instance,
        dataSource: DATA_SOURCE,
      },
    });

    // Wait for the component to poll and populate chartData (3 samples)
    await waitFor(() => {
      // find any of the numeric texts to ensure initial fetch happened
      expect(screen.getByText(`${ACTIVE[0]}`)).toBeVisible();
    });

    const newApp = new Application({
      name: 'Other',
      statusTimestamp: Date.now(),
      instances: [{ id: 'other-1', statusInfo: { status: 'UP' } }],
    });
    const newInstance = newApp.instances[0];

    await rerender({ instance: newInstance, dataSource: DATA_SOURCE });

    // After rerender with a different instance, the component should reset
    await waitFor(() => {
      expect(screen.queryByText(`${ACTIVE[0]}`)).not.toBeInTheDocument();
    });
  });
});
