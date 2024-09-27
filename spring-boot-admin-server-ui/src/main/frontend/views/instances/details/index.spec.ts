import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { describe, expect, it, vi } from 'vitest';

import DetailsView from './index.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

describe('InstanceDetails', () => {
  describe('Metrics', () => {
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

      instance.hasEndpoint = vi.fn().mockImplementation((endpoint) => {
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
