import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';

import DetailsView from './index.vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { useInstanceService } from '@/composables/useInstanceService';
import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

vi.mock('@/composables/useInstanceService', () => ({
  useInstanceService: vi.fn(),
}));

const storeApplications = ref<Application[]>([]);

const fetchMetricsMock = vi.fn();

beforeEach(() => {
  storeApplications.value = [];
  (useApplicationStore as any).mockReturnValue({ applications: storeApplications });
  fetchMetricsMock.mockResolvedValue({ data: { names: [] } });
  (useInstanceService as any).mockReturnValue({ fetchMetrics: fetchMetricsMock });
});

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: any) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe('InstanceDetails', () => {
  describe('Metrics', () => {
    it('should hide loading spinner, when network call fails', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      storeApplications.value = [application];

      server.use(
        http.get('/instances/:instanceId/actuator/metrics', () => {
          return HttpResponse.json({});
        }),
      );

      render(DetailsView, {
        props: {
          instance,
          application,
        },
      });

      await waitFor(async () => {
        expect(
          await screen.queryByTestId('instance-section-loading-spinner'),
        ).not.toBeInTheDocument();
      });
    });

    it('should hide loading spinner, when metrics endpoint is not exposed', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      storeApplications.value = [application];

      instance.hasEndpoint = vi.fn().mockImplementation(function (endpoint) {
        return endpoint !== 'metrics';
      });

      render(DetailsView, {
        props: {
          instance,
          application,
        },
      });

      await waitFor(async () => {
        expect(
          await screen.queryByTestId('instance-section-loading-spinner'),
        ).not.toBeInTheDocument();
      });
    });

    it('should refetch metric index when the instance id changes (navigation to different instance)', async () => {
      const application = new Application(applications[0]);
      const instance1 = application.instances[0];
      storeApplications.value = [application];
      fetchMetricsMock.mockResolvedValue({ data: { names: ['jvm.memory.max'] } });
      instance1.hasEndpoint = vi.fn().mockImplementation(() => true);

      const { rerender } = render(DetailsView, {
        props: { instance: instance1, application },
      });

      await waitFor(() => {
        expect(fetchMetricsMock).toHaveBeenCalledTimes(1);
      });

      const instance2 = new Application({
        ...application,
        instances: [{ ...instance1, id: 'different-instance-id' }],
      }).instances[0];
      instance2.hasEndpoint = instance1.hasEndpoint;

      await rerender({ instance: instance2, application });

      await waitFor(() => {
        expect(fetchMetricsMock).toHaveBeenCalledTimes(2);
      });
    });

    it('should ignore stale metric index response when instance id changes quickly', async () => {
      const application = new Application(applications[0]);
      const instance1 = application.instances[0];
      storeApplications.value = [application];

      const p1 = deferred<{ data: { names: string[] } }>();
      const p2 = deferred<{ data: { names: string[] } }>();
      fetchMetricsMock
        .mockReturnValueOnce(p1.promise)
        .mockReturnValueOnce(p2.promise);
      instance1.hasEndpoint = vi.fn().mockImplementation(() => true);

      const { rerender } = render(DetailsView, {
        global: {
          stubs: {
            'details-memory': {
              props: ['instance', 'type'],
              template: '<div data-testid="details-memory" />',
            },
          },
        },
        props: { instance: instance1, application },
      });

      await waitFor(() => {
        expect(fetchMetricsMock).toHaveBeenCalledTimes(1);
      });

      const instance2 = new Application({
        ...application,
        instances: [{ ...instance1, id: 'different-instance-id' }],
      }).instances[0];
      instance2.hasEndpoint = instance1.hasEndpoint;

      await rerender({ instance: instance2, application });

      await waitFor(() => {
        expect(fetchMetricsMock).toHaveBeenCalledTimes(2);
      });

      // Resolve second (newer) response first.
      p2.resolve({ data: { names: ['jvm.memory.max'] } });
      await waitFor(() => {
        expect(
          screen.queryByTestId('instance-section-loading-spinner'),
        ).not.toBeInTheDocument();
      });

      // Memory sections should be present for the newer response.
      await waitFor(() => {
        expect(screen.getAllByTestId('details-memory')).toHaveLength(2);
      });

      // Resolve first (older/stale) response afterwards; UI must not regress.
      p1.resolve({ data: { names: [] } });
      await waitFor(() => {
        expect(screen.getAllByTestId('details-memory')).toHaveLength(2);
      });
    });
  });
});
