import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsThreads from '@/views/instances/details/details-threads.vue';

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        pollTimer: {
          threads: 100,
        },
      },
    },
  };
});

describe('DetailsThreads', () => {
  const LIVE = [10.0, 12.0, 14.0];
  const PEAK = [15.0, 16.0, 18.0];
  const DAEMON = [4.0, 5.0, 6.0];

  beforeEach(() => {
    const liveGen = (function* () {
      yield* LIVE;
    })();
    const peakGen = (function* () {
      yield* PEAK;
    })();
    const daemonGen = (function* () {
      yield* DAEMON;
    })();

    server.use(
      http.get(
        '/instances/:instanceId/actuator/metrics/jvm.threads.live',
        () => {
          return HttpResponse.json({
            measurements: [{ value: liveGen.next()?.value }],
          });
        },
      ),
      http.get(
        '/instances/:instanceId/actuator/metrics/jvm.threads.peak',
        () => {
          return HttpResponse.json({
            measurements: [{ value: peakGen.next()?.value }],
          });
        },
      ),
      http.get(
        '/instances/:instanceId/actuator/metrics/jvm.threads.daemon',
        () => {
          return HttpResponse.json({
            measurements: [{ value: daemonGen.next()?.value }],
          });
        },
      ),
    );
  });

  const renderComponent = async () => {
    const stubChart = {
      props: ['data'],
      template: `
        <div data-testid="chart">{{ JSON.stringify($props.data) }}</div>
      `,
    };

    const application = new Application(applications[0]);
    const instance = application.instances[0];

    return render(DetailsThreads, {
      global: { stubs: { threadsChart: stubChart } },
      props: { instance },
    });
  };

  it('pushes chartData points and exposes them via stub', async () => {
    await renderComponent();

    const text = (await screen.findByTestId('chart')).textContent;
    const data = JSON.parse(text);

    // first sample matches generators
    expect(data[0].live).toEqual(LIVE[0]);
    expect(data[0].peak).toEqual(PEAK[0]);
    expect(data[0].daemon).toEqual(DAEMON[0]);
  });

  it('should reinitialize metrics when instance changes', async () => {
    const { rerender } = await renderComponent();

    const newApp = new Application({
      name: 'Other',
      statusTimestamp: Date.now(),
      instances: [{ id: 'other-1', statusInfo: { status: 'UP' } }],
    });
    const newInstance = newApp.instances[0];

    await rerender({ instance: newInstance });

    const text = (await screen.findByTestId('chart')).textContent;
    const data = JSON.parse(text);

    // first sample matches generators
    expect(data[0].live).toEqual(LIVE[0]);
    expect(data[0].peak).toEqual(PEAK[0]);
    expect(data[0].daemon).toEqual(DAEMON[0]);
  });
});
