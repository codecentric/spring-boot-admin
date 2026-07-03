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

// Mock useInstanceService so tests control fetchMetric responses
// without needing a real axios client or MSW for every case.
const mockFetchMetric = vi.fn();
vi.mock('@/composables/useInstanceService', () => ({
  useInstanceService: () => ({
    fetchMetric: mockFetchMetric,
  }),
}));

function makeFetchMetric({
  max = 8589934590,
  used = 115390832,
  committed = 197132288,
  metaspaceValue = 115390832,
  hasMetaspace = false,
} = {}) {
  return vi
    .fn()
    .mockImplementation((name: string, tags: Record<string, any>) => {
      const mk = (value: number, availableTags: any[] = []) =>
        Promise.resolve({ data: { measurements: [{ value }], availableTags } });

      if (name === 'jvm.memory.max') return mk(max);
      if (name === 'jvm.memory.committed') return mk(committed);
      if (name === 'jvm.memory.used') {
        if (tags?.id === 'Metaspace') return mk(metaspaceValue);
        return mk(used, [
          {
            tag: 'id',
            values: hasMetaspace
              ? ['G1 Survivor Space', 'Metaspace']
              : ['G1 Survivor Space', 'G1 Old Gen'],
          },
        ]);
      }
      throw new Error('unexpected metric: ' + name);
    });
}

describe('DetailsMemory', () => {
  it('should call timer with configured amount', async () => {
    const timer = vi.spyOn(rxjs, 'timer');
    mockFetchMetric.mockImplementation(makeFetchMetric());

    render(DetailsMemory, {
      stubs: { MemChart: true },
      props: { instanceId: '1', type: 'heap' },
    });

    expect(timer).toHaveBeenCalledWith(0, 1234);
  });

  describe('when type is heap', () => {
    beforeEach(() => {
      mockFetchMetric.mockImplementation(makeFetchMetric());

      render(DetailsMemory, {
        stubs: { MemChart: true },
        props: { instanceId: '1', type: 'heap' },
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

  describe('when type is nonheap', () => {
    beforeEach(() => {
      mockFetchMetric.mockImplementation(
        makeFetchMetric({ hasMetaspace: true }),
      );

      render(DetailsMemory, {
        stubs: { MemChart: true },
        props: { instanceId: '1', type: 'nonheap' },
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

  it('should not apply stale poll result after resubscribe on instance update', async () => {
    const tick1$ = new Subject<number>();
    const tick2$ = new Subject<number>();
    vi.spyOn(rxjs, 'timer')
      .mockReturnValueOnce(tick1$ as any)
      .mockReturnValueOnce(tick2$ as any);

    const pMax1 = deferred<{ data: any }>();
    const pUsed1 = deferred<{ data: any }>();
    const pCommitted1 = deferred<{ data: any }>();

    // First instanceId: promises that stay pending
    mockFetchMetric.mockImplementation((name: string) => {
      if (name === 'jvm.memory.max') return pMax1.promise;
      if (name === 'jvm.memory.used') return pUsed1.promise;
      if (name === 'jvm.memory.committed') return pCommitted1.promise;
      throw new Error('unexpected metric: ' + name);
    });

    const { rerender } = render(DetailsMemory, {
      stubs: { MemChart: true },
      props: { instanceId: '1', type: 'heap' },
    });

    tick1$.next(0);

    // Switch to a different instanceId — component should pick up new fetchMetric
    mockFetchMetric.mockImplementation(
      makeFetchMetric({ max: 3000, used: 2000, committed: 1000 }),
    );

    await rerender({ instanceId: '2', type: 'heap' });
    tick2$.next(0);

    expect(
      await screen.findByLabelText('instances.details.memory.used'),
    ).toHaveTextContent('2 kB');

    // Resolve stale in-flight responses — must not override the result above
    pMax1.resolve({
      data: { measurements: [{ value: 6000 }], availableTags: [] },
    });
    pUsed1.resolve({
      data: {
        measurements: [{ value: 5000 }],
        availableTags: [{ tag: 'id', values: [] }],
      },
    });
    pCommitted1.resolve({
      data: { measurements: [{ value: 4000 }], availableTags: [] },
    });

    await Promise.resolve();
    await Promise.resolve();

    expect(
      await screen.findByLabelText('instances.details.memory.used'),
    ).toHaveTextContent('2 kB');
  });
});
