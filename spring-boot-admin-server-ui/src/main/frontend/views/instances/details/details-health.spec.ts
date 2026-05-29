import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { AxiosResponse } from 'axios';
import { describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsHealth from '@/views/instances/details/details-health.vue';

describe('DetailsHealth', () => {
  it('should display health status from instance.statusInfo', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    // Mock fetchHealth for groups (will be called once on mount)
    instance.fetchHealth = vi
      .fn()
      .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    const statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');
  });

  it('should display health details from instance.statusInfo', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    instance.fetchHealth = vi
      .fn()
      .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    expect(await screen.findByLabelText('db')).toBeInTheDocument();
    expect(await screen.findByLabelText('diskSpace')).toBeInTheDocument();
    expect(await screen.findByLabelText('ping')).toBeInTheDocument();
  });

  it('should update when instance prop changes', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];
    instance1.fetchHealth = vi
      .fn()
      .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

    const { rerender } = render(DetailsHealth, {
      props: {
        instance: instance1,
      },
    });

    let statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');

    const instance2 = new Application({
      ...applications[0],
      instances: [
        {
          ...applications[0].instances[0],
          statusInfo: { status: 'DOWN', details: {} },
        },
      ],
    }).instances[0];
    instance2.fetchHealth = vi
      .fn()
      .mockResolvedValue({ data: { status: 'DOWN', groups: [] } });

    await rerender({ instance: instance2 });

    statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('DOWN');
  });

  it('should handle empty/missing details gracefully', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.statusInfo = { status: 'UP', details: {} };
    instance.fetchHealth = vi
      .fn()
      .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    const statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');
  });

  describe('SSE reactive updates', () => {
    it('should call fetchHealth once on mount, not on SSE version changes', async () => {
      const baseApp = applications[0];
      const instance1 = new Application(baseApp).instances[0];
      const fetchHealthSpy1 = vi.spyOn(instance1, 'fetchHealth');
      fetchHealthSpy1.mockResolvedValue({
        data: { status: 'UP', groups: ['liveness'] },
      } as AxiosResponse);

      const { rerender } = render(DetailsHealth, {
        props: { instance: instance1 },
      });

      await screen.findAllByRole('status');
      expect(fetchHealthSpy1).toHaveBeenCalledTimes(1);

      // Same instance, different version (SSE update) — should NOT call fetchHealth again
      const instance2 = new Application({
        ...baseApp,
        instances: [
          {
            ...baseApp.instances[0],
            version: (baseApp.instances[0].version || 1) + 1,
            statusInfo: { status: 'DOWN', details: {} },
          },
        ],
      }).instances[0];
      const fetchHealthSpy2 = vi.spyOn(instance2, 'fetchHealth');
      fetchHealthSpy2.mockResolvedValue({
        data: { status: 'DOWN', groups: [] },
      } as AxiosResponse);

      await rerender({ instance: instance2 });

      // Original instance's spy should still be 1 (no additional calls)
      expect(fetchHealthSpy1).toHaveBeenCalledTimes(1);
    });

    it('should reactively update through multiple SSE status changes without extra HTTP calls', async () => {
      const baseApp = applications[0];

      const instance1 = new Application(baseApp).instances[0];
      instance1.fetchHealth = vi
        .fn()
        .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

      const { rerender } = render(DetailsHealth, {
        props: { instance: instance1 },
      });

      let statusBadges = await screen.findAllByRole('status');
      expect(statusBadges[0]).toHaveTextContent('UP');

      // SSE update: same id, different version
      const instance2 = new Application({
        ...baseApp,
        instances: [
          {
            ...baseApp.instances[0],
            version: 4,
            statusInfo: {
              status: 'DOWN',
              details: {
                db: {
                  status: 'DOWN',
                  details: { error: 'Connection refused' },
                },
              },
            },
          },
        ],
      }).instances[0];
      instance2.fetchHealth = vi
        .fn()
        .mockResolvedValue({ data: { status: 'DOWN', groups: [] } });

      await rerender({ instance: instance2 });

      statusBadges = await screen.findAllByRole('status');
      expect(statusBadges[0]).toHaveTextContent('DOWN');
      expect(await screen.findByLabelText('db')).toBeInTheDocument();
    });
  });

  describe('lazy health groups', () => {
    it('should display health group buttons after mount', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchHealth = vi.fn().mockResolvedValue({
        data: { status: 'UP', groups: ['liveness', 'readiness'] },
      });
      const fetchGroupSpy = vi.spyOn(instance, 'fetchHealthGroup');

      render(DetailsHealth, {
        props: { instance },
      });

      await screen.findAllByRole('status');

      expect(
        await screen.findByRole('button', { name: /liveness/ }),
      ).toBeInTheDocument();
      expect(
        await screen.findByRole('button', { name: /readiness/ }),
      ).toBeInTheDocument();
      expect(fetchGroupSpy).not.toHaveBeenCalled();
    });

    it('should fetch group details on first click', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchHealth = vi.fn().mockResolvedValue({
        data: { status: 'UP', groups: ['custom-group'] },
      });
      const fetchGroupSpy = vi.spyOn(instance, 'fetchHealthGroup');
      fetchGroupSpy.mockResolvedValue({
        data: {
          status: 'UP',
          details: {
            customDetails: {
              status: 'UP',
              details: { error: 'no property sources located' },
            },
            evenMoreDiskSpace: {
              status: 'UP',
              details: {
                total: 994662584320,
                free: 300063879168,
                threshold: 10485760,
                exists: true,
              },
            },
          },
        },
      } as AxiosResponse);

      render(DetailsHealth, {
        props: { instance },
      });

      const button = await screen.findByRole('button', {
        name: /custom-group/,
      });
      await userEvent.click(button);

      await waitFor(() => {
        expect(fetchGroupSpy).toHaveBeenCalledWith('custom-group');
      });

      // custom-group has service component
      expect(await screen.findByLabelText('customDetails')).toBeInTheDocument();
      expect(
        await screen.findByLabelText('evenMoreDiskSpace'),
      ).toBeInTheDocument();
    });

    it('should toggle group visibility after data is loaded', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchHealth = vi.fn().mockResolvedValue({
        data: { status: 'UP', groups: ['custom-group'] },
      });
      const fetchGroupSpy = vi.spyOn(instance, 'fetchHealthGroup');
      fetchGroupSpy.mockResolvedValue({
        data: { status: 'UP', details: { service: { status: 'UP' } } },
      } as AxiosResponse);

      render(DetailsHealth, {
        props: { instance },
      });

      const button = await screen.findByRole('button', {
        name: /custom-group/,
      });

      // First click — fetch & show
      await userEvent.click(button);
      await screen.findByLabelText('service');

      // Second click — hide
      await userEvent.click(button);
      expect(screen.queryByLabelText('service')).not.toBeInTheDocument();

      // Third click — show again
      await userEvent.click(button);
      expect(await screen.findByLabelText('service')).toBeInTheDocument();

      // fetchHealthGroup should only be called once (first click)
      expect(fetchGroupSpy).toHaveBeenCalledTimes(1);
    });

    it('should not show groups when none exist', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchHealth = vi.fn().mockResolvedValue({
        data: { status: 'UP', groups: [] },
      });

      render(DetailsHealth, {
        props: { instance },
      });

      await screen.findAllByRole('status');

      expect(
        screen.queryByRole('button', { name: /Health Group/ }),
      ).not.toBeInTheDocument();
    });

    it('should re-fetch groups when instance id changes', async () => {
      const app1 = new Application(applications[0]).instances[0];
      app1.fetchHealth = vi
        .fn()
        .mockResolvedValue({ data: { status: 'UP', groups: ['liveness'] } });

      const { rerender } = render(DetailsHealth, {
        props: { instance: app1 },
      });

      await waitFor(() => {
        expect(app1.fetchHealth).toHaveBeenCalledTimes(1);
      });

      const app2 = new Application({
        ...applications[0],
        instances: [
          {
            ...applications[0].instances[0],
            id: 'different-id',
          },
        ],
      }).instances[0];
      app2.fetchHealth = vi
        .fn()
        .mockResolvedValue({ data: { status: 'UP', groups: ['readiness'] } });

      await rerender({ instance: app2 });

      await waitFor(() => {
        expect(app2.fetchHealth).toHaveBeenCalledTimes(1);
      });

      // Original instance should still have only 1 call
      expect(app1.fetchHealth).toHaveBeenCalledTimes(1);
    });
  });
});
