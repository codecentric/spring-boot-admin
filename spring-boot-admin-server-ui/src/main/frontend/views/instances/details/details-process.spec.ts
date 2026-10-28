import { flushPromises } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import sbaConfig from '@/sba-config';
import Application from '@/services/application';
import { render } from '@/test-utils';
import DetailsProcess from '@/views/instances/details/details-process.vue';

describe('DetailsProcess polling lifecycle', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  const createInstance = () => {
    const instance = new Application(applications[0]).instances[0];
    vi.spyOn(instance, 'fetchEnv').mockResolvedValue({
      data: { property: { value: 42 } },
    } as any);
    vi.spyOn(instance, 'fetchMetric').mockResolvedValue({
      data: { measurements: [{ value: 1 }], baseUnit: 'seconds' },
    } as any);
    vi.spyOn(instance, 'fetchInfo').mockResolvedValue({ data: {} } as any);
    return instance;
  };

  it('can unmount before the initial requests complete', async () => {
    const instance = createInstance();
    let resolveInfo!: (value: any) => void;
    vi.mocked(instance.fetchInfo).mockReturnValue(
      new Promise((resolve) => (resolveInfo = resolve)),
    );
    const onError = vi.fn();
    const { unmount } = render(DetailsProcess, {
      props: { instance },
      global: { config: { errorHandler: onError } },
    });

    unmount();
    resolveInfo({ data: {} });
    await flushPromises();
    expect(onError).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(
      sbaConfig.uiSettings.pollTimer.process * 2,
    );
    expect(instance.fetchMetric).not.toHaveBeenCalledWith('process.cpu.usage');
    expect(instance.fetchMetric).not.toHaveBeenCalledWith('system.cpu.usage');
  });

  it('does not start polling when a pending initial request fails after unmount', async () => {
    const instance = createInstance();
    let rejectInfo!: (reason: Error) => void;
    vi.mocked(instance.fetchInfo).mockReturnValue(
      new Promise((_, reject) => (rejectInfo = reject)),
    );
    const onError = vi.fn();
    const { unmount } = render(DetailsProcess, {
      props: { instance },
      global: { config: { errorHandler: onError } },
    });

    unmount();
    rejectInfo(new Error('Instance unavailable'));
    await flushPromises();
    await vi.advanceTimersByTimeAsync(
      sbaConfig.uiSettings.pollTimer.process * 2,
    );

    expect(instance.fetchMetric).not.toHaveBeenCalledWith('process.cpu.usage');
    expect(instance.fetchMetric).not.toHaveBeenCalledWith('system.cpu.usage');
    expect(onError).not.toHaveBeenCalled();
  });

  it('polls after initialization and stops on unmount', async () => {
    const instance = createInstance();
    const { unmount } = render(DetailsProcess, { props: { instance } });
    await flushPromises();
    await vi.advanceTimersByTimeAsync(0);
    expect(instance.fetchMetric).toHaveBeenCalledWith('process.cpu.usage');
    expect(instance.fetchMetric).toHaveBeenCalledWith('system.cpu.usage');

    const callsBeforeUnmount = vi.mocked(instance.fetchMetric).mock.calls
      .length;
    unmount();
    await vi.advanceTimersByTimeAsync(
      sbaConfig.uiSettings.pollTimer.process * 2,
    );
    expect(instance.fetchMetric).toHaveBeenCalledTimes(callsBeforeUnmount);
  });
});
