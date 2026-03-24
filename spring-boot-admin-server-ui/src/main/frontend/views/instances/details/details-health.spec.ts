import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsHealth from '@/views/instances/details/details-health.vue';

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: any) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe('DetailsHealth', () => {
  const healthHandlerSpy = vi.fn();

  beforeEach(() => {
    healthHandlerSpy.mockReset();
    server.use(
      http.get('/instances/:instanceId/actuator/health', () => {
        healthHandlerSpy();
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
    instance.statusInfo = { status: 'UP', details: {} };

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

  it('should update health details when instance prop changes (watch)', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];
    const instance2 = {
      ...instance1,
      id: 'other-id',
      statusInfo: { status: 'DOWN', details: {} },
    };

    const { rerender } = render(DetailsHealth, {
      props: {
        instance: instance1,
      },
    });

    await waitFor(() =>
      expect(screen.queryByRole('status')).not.toBeInTheDocument(),
    );

    // Simulate prop change
    await rerender({ instance: instance2 });

    // Wait for the component to react to the prop change
    await waitFor(() =>
      expect(
        screen.queryByRole('button', {
          name: /instances.details.health_group.title: liveness/,
        }),
      ).toBeVisible(),
    );
  });

  it('should refetch health when instance version changes (SSE update)', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];

    const { rerender } = render(DetailsHealth, {
      props: {
        instance: instance1,
      },
    });

    await waitFor(() => {
      expect(healthHandlerSpy).toHaveBeenCalledTimes(1);
    });

    const instance2 = new Application({
      ...application,
      instances: [
        {
          ...instance1,
          id: instance1.id,
          version: (instance1.version ?? 0) + 1,
        },
      ],
    }).instances[0];

    await rerender({ instance: instance2 });

    await waitFor(() => {
      expect(healthHandlerSpy).toHaveBeenCalledTimes(2);
    });
  });

  it('should ignore stale health response when instance updates quickly', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];

    const p1 = deferred<{ data: any }>();
    const p2 = deferred<{ data: any }>();
    const fetchHealthSpy = vi
      .fn()
      .mockReturnValueOnce(p1.promise)
      .mockReturnValueOnce(p2.promise);
    instance1.fetchHealth = fetchHealthSpy;
    instance1.fetchHealthGroup = vi.fn().mockResolvedValue({ data: {} });

    const { rerender } = render(DetailsHealth, {
      props: {
        instance: instance1,
      },
    });

    await waitFor(() => {
      expect(fetchHealthSpy).toHaveBeenCalledTimes(1);
    });

    const instance2 = new Application({
      ...application,
      instances: [
        {
          ...instance1,
          id: instance1.id,
          version: (instance1.version ?? 0) + 1,
        },
      ],
    }).instances[0];
    instance2.fetchHealth = fetchHealthSpy;
    instance2.fetchHealthGroup = instance1.fetchHealthGroup;

    await rerender({ instance: instance2 });

    await waitFor(() => {
      expect(fetchHealthSpy).toHaveBeenCalledTimes(2);
    });

    // Resolve second (newer) response first.
    p2.resolve({ data: { status: 'UP', groups: [] } });
    await waitFor(() => {
      expect(screen.getByText('UP')).toBeInTheDocument();
    });

    // Resolve first (older) response afterwards; UI must not regress.
    p1.resolve({ data: { status: 'DOWN', groups: [] } });
    await waitFor(() => {
      expect(screen.queryByText('DOWN')).not.toBeInTheDocument();
    });
    expect(screen.getByText('UP')).toBeInTheDocument();
  });

  it('should not display health group button if no groups are present', async () => {
    server.use(
      http.get('/instances/:instanceId/actuator/health', () => {
        return HttpResponse.json({
          instance: 'UP',
          groups: [],
        });
      }),
    );
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
      screen.queryByRole('button', {
        name: /instances.details.health_group.title: liveness/,
      }),
    ).toBeNull();
  });

  it('should fetch health details only once on startup', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(DetailsHealth, {
      props: {
        instance,
      },
    });

    await waitFor(() => {
      expect(healthHandlerSpy).toHaveBeenCalledTimes(1);
    });

    // Verify that the handler is not called again after the initial fetch
    await waitFor(
      () => {
        expect(healthHandlerSpy).toHaveBeenCalledTimes(1);
      },
      { timeout: 1000 },
    );
  });
});
