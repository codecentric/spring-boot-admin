import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsHealth from '@/views/instances/details/details-health.vue';

describe('DetailsHealth', () => {
  describe('Health Group', () => {
    beforeEach(() => {
      server.use(
        http.get('/instances/:instanceId/actuator/health', () => {
          return HttpResponse.json({
            instance: 'UP',
            groups: ['liveness'],
          });
        }),
        http.get('/instances/:instanceId/actuator/health/liveness', () => {
          return HttpResponse.json({
            status: 'UP',
            details: {
              disk: { status: 'UNKNOWN' },
              database: { status: 'UNKNOWN' },
            },
          });
        }),
      );
    });

    it('should display groups as part of health section', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];

      render(DetailsHealth, {
        props: {
          instance,
        },
      });

      await waitFor(() =>
        expect(screen.queryByRole('status')).not.toBeInTheDocument(),
      );

      expect(
        await screen.findByRole('button', {
          name: /instances.details.health_group.title: liveness/,
        }),
      ).toBeVisible();
    });

    it('health groups are toggleable, when details are available', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.statusInfo = {};

      render(DetailsHealth, {
        props: {
          instance,
        },
      });

      await waitFor(() =>
        expect(screen.queryByRole('status')).not.toBeInTheDocument(),
      );

      const button = screen.queryByRole('button', {
        name: /instances.details.health_group.title: liveness/,
      });
      expect(button).toBeVisible();

      expect(screen.queryByLabelText('disk')).toBeNull();
      expect(screen.queryByLabelText('database')).toBeNull();

      await userEvent.click(button);

      expect(screen.queryByLabelText('disk')).toBeDefined();
      expect(screen.queryByLabelText('database')).toBeDefined();
    });
  });
});
