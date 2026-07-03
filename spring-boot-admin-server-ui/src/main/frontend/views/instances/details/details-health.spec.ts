import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { computed, ref } from 'vue';

import { useInstanceData } from '@/composables/useInstanceData';
import { instance as instanceData } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import { render } from '@/test-utils';
import DetailsHealth from '@/views/instances/details/details-health.vue';

vi.mock('@/composables/useInstanceData', () => ({
  useInstanceData: vi.fn(),
}));

const currentInstance = ref<any>({ ...instanceData });

beforeEach(() => {
  currentInstance.value = { ...instanceData };
  (useInstanceData as any).mockReturnValue({
    instance: computed(() => currentInstance.value),
    application: ref(null),
  });

  server.use(
    http.get('/instances/:instanceId/actuator/health', () => {
      return HttpResponse.json({
        status: 'UP',
        groups: ['liveness'],
        components: {
          db: { status: 'UP', details: { database: 'H2' } },
          diskSpace: { status: 'UP', details: { free: 100000, total: 500000 } },
          ping: { status: 'UP' },
        },
      });
    }),
  );
});

describe('DetailsHealth', () => {
  const INSTANCE_ID = instanceData.id;

  it('should display health status from instance statusInfo', async () => {
    render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

    const statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');
  });

  it('should display health details from instance statusInfo', async () => {
    render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

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

  it('should update reactively when statusInfo changes (SSE)', async () => {
    render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

    let statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');

    // Simulate SSE push
    currentInstance.value = {
      ...instanceData,
      statusInfo: { status: 'DOWN', details: {} },
    };

    statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('DOWN');
  });

  it('should handle empty details gracefully', async () => {
    currentInstance.value = {
      ...instanceData,
      statusInfo: { status: 'UP', details: {} },
    };
    render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

    const statusBadges = await screen.findAllByRole('status');
    expect(statusBadges[0]).toHaveTextContent('UP');
  });

  describe('lazy health groups', () => {
    it('should display health group buttons after mount', async () => {
      render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

      await screen.findAllByRole('status');
      expect(
        await screen.findByRole('button', { name: /liveness/ }),
      ).toBeInTheDocument();
    });

    it('should fetch group details on first click', async () => {
      server.use(
        http.get('/instances/:instanceId/actuator/health/custom-group', () =>
          HttpResponse.json({
            status: 'UP',
            details: {
              customDetails: { status: 'UP', details: {} },
              evenMoreDiskSpace: {
                status: 'UP',
                details: { total: 99, free: 30 },
              },
            },
          }),
        ),
        http.get('/instances/:instanceId/actuator/health', () =>
          HttpResponse.json({ status: 'UP', groups: ['custom-group'] }),
        ),
      );

      render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

      const button = await screen.findByRole('button', {
        name: /custom-group/,
      });
      await userEvent.click(button);

      expect(
        await screen.findByRole('group', { name: 'customDetails' }),
      ).toBeInTheDocument();
      expect(
        await screen.findByRole('group', { name: 'evenMoreDiskSpace' }),
      ).toBeInTheDocument();
    });

    it('should toggle group visibility after data is loaded', async () => {
      server.use(
        http.get('/instances/:instanceId/actuator/health/custom-group', () =>
          HttpResponse.json({
            status: 'UP',
            details: { service: { status: 'UP' } },
          }),
        ),
        http.get('/instances/:instanceId/actuator/health', () =>
          HttpResponse.json({ status: 'UP', groups: ['custom-group'] }),
        ),
      );

      render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

      const button = await screen.findByRole('button', {
        name: /custom-group/,
      });

      await userEvent.click(button);
      expect(
        await screen.findByRole('group', { name: 'service' }),
      ).toBeInTheDocument();

      await userEvent.click(button);
      expect(screen.queryByLabelText('service')).not.toBeInTheDocument();

      await userEvent.click(button);
      expect(
        await screen.findByRole('group', { name: 'service' }),
      ).toBeInTheDocument();
    });

    it('should not show groups when none exist', async () => {
      server.use(
        http.get('/instances/:instanceId/actuator/health', () =>
          HttpResponse.json({ status: 'UP', groups: [] }),
        ),
      );

      render(DetailsHealth, { props: { instanceId: INSTANCE_ID } });

      await screen.findAllByRole('status');
      expect(
        screen.queryByRole('button', { name: /Health Group/ }),
      ).not.toBeInTheDocument();
    });

    it('should re-fetch groups when instanceId changes', async () => {
      let callCount = 0;
      server.use(
        http.get('/instances/:instanceId/actuator/health', () => {
          callCount++;
          return HttpResponse.json({ status: 'UP', groups: ['liveness'] });
        }),
      );

      const { rerender } = render(DetailsHealth, {
        props: { instanceId: INSTANCE_ID },
      });

      await waitFor(() => expect(callCount).toBe(1));

      currentInstance.value = { ...instanceData, id: 'different-id' };
      await rerender({ instanceId: 'different-id' });

      await waitFor(() => expect(callCount).toBe(2));
    });
  });
});
