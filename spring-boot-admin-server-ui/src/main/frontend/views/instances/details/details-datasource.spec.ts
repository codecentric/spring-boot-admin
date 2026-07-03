import { screen, waitFor } from '@testing-library/vue';
import { enableAutoUnmount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { instance as instanceData } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
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
  const INSTANCE_ID = instanceData.id;
  const ACTIVE = [2.0, 3.0, 4.0];
  const MIN = [1.0, 1.0, 1.0];
  const MAX = [5.0, -1.0, 10.0];

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

  async function renderComponent(instanceId = INSTANCE_ID) {
    return render(DetailsDatasource, {
      props: {
        instanceId,
        dataSource: DATA_SOURCE,
      },
    });
  }

  it('renders panel title with datasource name', async () => {
    await renderComponent();
    const heading = await screen.findByRole('heading');
    expect(heading).toBeVisible();
  });

  it('renders active, min and max values', async () => {
    await renderComponent();
    expect(await screen.findByText(`${ACTIVE[0]}`)).toBeVisible();
    expect(await screen.findByText(`${MIN[0]}`)).toBeVisible();
    expect(await screen.findByText(`${MAX[0]}`)).toBeVisible();
  });

  it('handles unlimited max (-1)', async () => {
    await renderComponent();

    await waitFor(() => {
      expect(screen.getByText(`${ACTIVE[0]}`)).toBeVisible();
    });

    expect(
      await screen.findByText('instances.details.datasource.unlimited'),
    ).toBeVisible();
  });

  it('should reinitialize metrics when instanceId changes', async () => {
    const { rerender } = await renderComponent();

    await waitFor(() => {
      expect(screen.getByText(`${ACTIVE[0]}`)).toBeVisible();
    });

    await rerender({
      instanceId: 'other-instance-id',
      dataSource: DATA_SOURCE,
    });

    await waitFor(() => {
      expect(screen.queryByText(`${ACTIVE[0]}`)).not.toBeInTheDocument();
    });
  });
});
