import { screen } from '@testing-library/vue';
import rxjs from 'rxjs';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { render } from '@/test-utils';
import DetailsMemory from '@/views/instances/details/details-memory.vue';

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: any) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        pollTimer: {
          memory: 1234,
        },
      },
    },
  };
});

describe('DetailsMemory', () => {
  it('should call timer with configured amount', async () => {
    const timer = vi.spyOn(rxjs, 'timer');

    const Instance = (await import('@/services/instance')).default;
    render(DetailsMemory, {
      stubs: {
        MemChart: true,
      },
      props: {
        instance: new Instance({ id: '1' }),
        type: 'heap',
      },
    });

    expect(timer).toHaveBeenCalledWith(0, 1234);
  });

  describe('when type is heap', async () => {
    const Instance = (await import('@/services/instance')).default;

    beforeEach(() => {
      render(DetailsMemory, {
        stubs: {
          MemChart: true,
        },
        props: {
          instance: new Instance({
            id: '1',
            availableMetrics: [
              'jvm.memory.used',
              'jvm.memory.max',
              'jvm.memory.committed',
            ],
          }),
          type: 'heap',
        },
      });
    });

    it('should render memory used', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.used'),
      ).toHaveTextContent('115 MB');
    });

    it('should render memory size', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.size'),
      ).toHaveTextContent('197 MB');
    });

    it('should render memory max', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.max'),
      ).toHaveTextContent('8.59 GB');
    });

    it('should not render memory metaspace', async () => {
      expect(
        screen.queryByLabelText('instances.details.memory.metaspace'),
      ).not.toBeInTheDocument();
    });
  });

  describe('when type is nonheap', async () => {
    const Instance = (await import('@/services/instance')).default;

    beforeEach(() => {
      render(DetailsMemory, {
        stubs: {
          MemChart: true,
        },
        props: {
          instance: new Instance({
            id: '1',
            availableMetrics: [
              'jvm.memory.used',
              'jvm.memory.max',
              'jvm.memory.committed',
            ],
          }),
          type: 'nonheap',
        },
      });
    });

    it('should render memory used', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.used'),
      ).toHaveTextContent('115 MB');
    });

    it('should render memory size', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.size'),
      ).toHaveTextContent('197 MB');
    });

    it('should render memory max', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.max'),
      ).toHaveTextContent('8.59 GB');
    });

    it('should render memory metaspace', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.metaspace'),
      ).toHaveTextContent('115 MB');
    });
  });

  it('should reinitialize metrics when instance version changes (SSE update)', async () => {
    const timer = vi.spyOn(rxjs, 'timer');
    const Instance = (await import('@/services/instance')).default;

    const { rerender } = render(DetailsMemory, {
      stubs: {
        MemChart: true,
      },
      props: {
        instance: new Instance({
          id: '1',
          version: 1,
          availableMetrics: [
            'jvm.memory.used',
            'jvm.memory.max',
            'jvm.memory.committed',
          ],
        }),
        type: 'heap',
      },
    });

    expect(timer).toHaveBeenCalledTimes(1);

    await rerender({
      instance: new Instance({
        id: '1',
        version: 2,
        availableMetrics: [
          'jvm.memory.used',
          'jvm.memory.max',
          'jvm.memory.committed',
        ],
      }),
      type: 'heap',
    });

    expect(timer).toHaveBeenCalledTimes(2);
  });

  it('should not apply stale poll result after resubscribe on instance update', async () => {
    const tick1$ = new Subject<number>();
    const tick2$ = new Subject<number>();
    const timerSpy = vi
      .spyOn(rxjs, 'timer')
      .mockReturnValueOnce(tick1$ as any)
      .mockReturnValueOnce(tick2$ as any);

    const Instance = (await import('@/services/instance')).default;

    const pMax1 = deferred<{ data: any }>();
    const pUsed1 = deferred<{ data: any }>();
    const pCommitted1 = deferred<{ data: any }>();

    const instance1 = new Instance({
      id: '1',
      version: 1,
      availableMetrics: ['jvm.memory.used', 'jvm.memory.max', 'jvm.memory.committed'],
    });
    instance1.fetchMetric = vi.fn().mockImplementation((name: string) => {
      if (name === 'jvm.memory.max') return pMax1.promise;
      if (name === 'jvm.memory.used') return pUsed1.promise;
      if (name === 'jvm.memory.committed') return pCommitted1.promise;
      throw new Error('unexpected metric: ' + name);
    });

    const instance2 = new Instance({
      id: '1',
      version: 2,
      availableMetrics: ['jvm.memory.used', 'jvm.memory.max', 'jvm.memory.committed'],
    });
    instance2.fetchMetric = vi.fn().mockImplementation((name: string) => {
      const mk = (value: number) =>
        Promise.resolve({
          data: {
            measurements: [{ value }],
            availableTags: [],
          },
        });
      if (name === 'jvm.memory.max') return mk(3000);
      if (name === 'jvm.memory.used') return mk(2000);
      if (name === 'jvm.memory.committed') return mk(1000);
      throw new Error('unexpected metric: ' + name);
    });

    const { rerender } = render(DetailsMemory, {
      stubs: {
        MemChart: true,
      },
      props: {
        instance: instance1,
        type: 'heap',
      },
    });

    // Start first polling run but keep its responses pending.
    tick1$.next(0);

    // Update instance: should unsubscribe old polling and subscribe again.
    await rerender({ instance: instance2, type: 'heap' });
    tick2$.next(0);

    // New subscription result renders.
    expect(
      await screen.findByLabelText('instances.details.memory.used'),
    ).toHaveTextContent('2 kB');

    // Resolve the older (stale) in-flight responses; they must not override.
    pMax1.resolve({
      data: { measurements: [{ value: 6000 }], availableTags: [] },
    });
    pUsed1.resolve({
      data: { measurements: [{ value: 5000 }], availableTags: [] },
    });
    pCommitted1.resolve({
      data: { measurements: [{ value: 4000 }], availableTags: [] },
    });

    // Give pending promises a chance to settle.
    await Promise.resolve();
    await Promise.resolve();

    expect(
      await screen.findByLabelText('instances.details.memory.used'),
    ).toHaveTextContent('2 kB');

    timerSpy.mockRestore();
  });
});
