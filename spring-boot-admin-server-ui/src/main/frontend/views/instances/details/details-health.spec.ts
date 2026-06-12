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

    // Mock fetchCachedHealthGroups for groups (will be called once on mount)
    instance.fetchCachedHealthGroups = vi
      .fn()
      .mockResolvedValue({ data: ['liveness'] });

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

    instance.fetchCachedHealthGroups = vi
      .fn()
      .mockResolvedValue({ data: ['liveness'] });

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    expect(
      await screen.findByRole('group', { name: 'db' }),
    ).toBeInTheDocument();
    expect(
      await screen.findByRole('group', { name: 'diskSpace' }),
    ).toBeInTheDocument();
    expect(
      await screen.findByRole('group', { name: 'ping' }),
    ).toBeInTheDocument();
  });

  it('should update when instance prop changes', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];
    instance1.fetchCachedHealthGroups = vi
      .fn()
      .mockResolvedValue({ data: ['liveness'] });

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
    instance2.fetchCachedHealthGroups = vi.fn().mockResolvedValue({ data: [] });

    await rerender({ instance: instance2 });

    statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('DOWN');
  });

  it('should handle empty/missing details gracefully', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.statusInfo = { status: 'UP', details: {} };
    instance.fetchCachedHealthGroups = vi
      .fn()
      .mockResolvedValue({ data: ['liveness'] });

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    const statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');
  });

  describe('SSE reactive updates', () => {
    it('should re-fetch cached health groups on SSE version changes', async () => {
      const baseApp = applications[0];
      const instance1 = new Application(baseApp).instances[0];
      instance1.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: ['liveness'] });

      const { rerender } = render(DetailsHealth, {
        props: { instance: instance1 },
      });

      await screen.findAllByRole('status');
      expect(instance1.fetchCachedHealthGroups).toHaveBeenCalledTimes(1);

      // Same instance, different version (SSE update) — should re-fetch the
      // (server-cached) group list so it self-corrects when groups change.
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
      instance2.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: ['liveness', 'readiness'] });

      await rerender({ instance: instance2 });

      // The new instance's cached groups should have been fetched on the SSE update.
      await waitFor(() => {
        expect(instance2.fetchCachedHealthGroups).toHaveBeenCalledTimes(1);
      });
    });

    it('should reactively update health status and details through SSE status changes', async () => {
      const baseApp = applications[0];

      const instance1 = new Application(baseApp).instances[0];
      instance1.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: ['liveness'] });

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
      instance2.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: [] });

      await rerender({ instance: instance2 });

      statusBadges = await screen.findAllByRole('status');
      expect(statusBadges[0]).toHaveTextContent('DOWN');

      expect(
        await screen.findByRole('group', { name: 'db' }),
      ).toBeInTheDocument();
    });
  });

  describe('lazy health groups', () => {
    it('should display health group buttons after mount', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchCachedHealthGroups = vi.fn().mockResolvedValue({
        data: ['liveness', 'readiness'],
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
      instance.fetchCachedHealthGroups = vi.fn().mockResolvedValue({
        data: ['custom-group'],
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
      expect(
        await screen.findByRole('group', { name: 'customDetails' }),
      ).toBeInTheDocument();
      expect(
        await screen.findByRole('group', { name: 'evenMoreDiskSpace' }),
      ).toBeInTheDocument();
    });

    it('should toggle group visibility after data is loaded', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchCachedHealthGroups = vi.fn().mockResolvedValue({
        data: ['custom-group'],
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
      expect(
        await screen.findByRole('group', { name: 'service' }),
      ).toBeInTheDocument();

      // Second click — hide
      await userEvent.click(button);
      expect(screen.queryByLabelText('service')).not.toBeInTheDocument();

      // Third click — show again
      await userEvent.click(button);
      expect(
        await screen.findByRole('group', { name: 'service' }),
      ).toBeInTheDocument();

      // fetchHealthGroup should only be called once (first click)
      expect(fetchGroupSpy).toHaveBeenCalledTimes(1);
    });

    it('should not show groups when none exist', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];
      instance.fetchCachedHealthGroups = vi.fn().mockResolvedValue({
        data: [],
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
      app1.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: ['liveness'] });

      const { rerender } = render(DetailsHealth, {
        props: { instance: app1 },
      });

      await waitFor(() => {
        expect(app1.fetchCachedHealthGroups).toHaveBeenCalledTimes(1);
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
      app2.fetchCachedHealthGroups = vi
        .fn()
        .mockResolvedValue({ data: ['readiness'] });

      await rerender({ instance: app2 });

      await waitFor(() => {
        expect(app2.fetchCachedHealthGroups).toHaveBeenCalledTimes(1);
      });

      // Original instance should still have only 1 call
      expect(app1.fetchCachedHealthGroups).toHaveBeenCalledTimes(1);
    });
  });
});
