import { screen, waitFor } from '@testing-library/vue';
import { flushPromises } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { describe, expect, it, vi } from 'vitest';

import DetailsView from './index.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';

describe('InstanceDetails', () => {
  describe('Metrics', () => {
    const metricsInstance = (exposed: boolean) => {
      const instance = new Application(applications[0]).instances[0];
      instance.endpoints = exposed
        ? [{ id: 'metrics', url: '/actuator/metrics' }]
        : [];
      return instance;
    };

    const renderMetrics = (instance: Instance) =>
      render(DetailsView, {
        props: { instance, application: new Application(applications[0]) },
        global: {
          stubs: {
            DetailsThreads: { template: '<div>Thread metrics</div>' },
          },
        },
      });

    it('loads metrics when the endpoint is discovered for an open instance', async () => {
      const { rerender } = renderMetrics(metricsInstance(false));
      const instance = metricsInstance(true);
      server.use(
        http.get('/instances/:instanceId/actuator/metrics', () =>
          HttpResponse.json({ names: ['jvm.threads.live'] }),
        ),
      );
      const fetchMetrics = vi.spyOn(instance, 'fetchMetrics');

      await rerender({ instance });
      expect(await screen.findByText('Thread metrics')).toBeVisible();

      const refreshed = metricsInstance(true);
      const refreshMetrics = vi.spyOn(refreshed, 'fetchMetrics');
      await rerender({ instance: refreshed });
      expect(fetchMetrics).toHaveBeenCalledTimes(1);
      expect(refreshMetrics).not.toHaveBeenCalled();
      expect(screen.getByText('Thread metrics')).toBeVisible();

      await rerender({ instance: metricsInstance(false) });
      expect(screen.queryByText('Thread metrics')).not.toBeInTheDocument();
    });

    it('ignores a pending metric index after the endpoint disappears', async () => {
      const instance = metricsInstance(true);
      const { promise, reject } = Promise.withResolvers<never>();
      vi.spyOn(instance, 'fetchMetrics').mockReturnValue(promise);
      const { rerender } = renderMetrics(instance);

      expect(
        screen.getByTestId('instance-section-loading-spinner'),
      ).toBeVisible();
      await rerender({ instance: metricsInstance(false) });
      expect(
        screen.queryByTestId('instance-section-loading-spinner'),
      ).not.toBeInTheDocument();

      reject(new Error('Metrics endpoint is gone'));
      await flushPromises();
      expect(
        screen.queryByText('Metrics endpoint is gone'),
      ).not.toBeInTheDocument();
    });

    it('should hide loading spinner, when network call fails', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];

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
  });
});
