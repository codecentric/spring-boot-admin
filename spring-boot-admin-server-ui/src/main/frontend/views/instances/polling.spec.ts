import type { Subscription } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import HttpExchanges from '@/views/instances/httpexchanges/index.vue';
import Httptrace from '@/views/instances/httptrace/index.vue';
import ThreadDump from '@/views/instances/threaddump/index.vue';

describe.each([
  {
    name: 'thread dump',
    component: ThreadDump,
    fetchMethod: 'fetchThreaddump',
    errorProperty: 'errorFetch',
  },
  {
    name: 'HTTP exchanges',
    component: HttpExchanges,
    fetchMethod: 'fetchHttpExchanges',
    errorProperty: 'error',
  },
  {
    name: 'HTTP trace',
    component: Httptrace,
    fetchMethod: 'fetchHttptrace',
    errorProperty: 'error',
  },
])('$name polling', ({ component, fetchMethod, errorProperty }) => {
  let subscription: Subscription;

  beforeEach(() => {
    vi.useFakeTimers();
    vi.spyOn(console, 'warn').mockImplementation(() => {});
  });

  afterEach(() => {
    subscription?.unsubscribe();
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  const startPolling = (fetch: ReturnType<typeof vi.fn>) => {
    const state = {
      hasLoaded: false,
      [errorProperty]: null,
      [fetchMethod]: fetch,
      updateTimelines: vi.fn(),
      exchanges: [],
      traces: [],
      limit: 100,
    };
    subscription = component.methods.createSubscription.call(state);
    return state;
  };

  it('reports the final failure after two retries and stops loading', async () => {
    const failure = new Error('Actuator unavailable');
    const fetch = vi.fn().mockRejectedValue(failure);
    const state = startPolling(fetch);

    await vi.advanceTimersByTimeAsync(2100);

    expect(fetch).toHaveBeenCalledTimes(3);
    expect(state[errorProperty]).toBe(failure);
    expect(state.hasLoaded).toBe(true);
    expect(subscription.closed).toBe(true);

    await vi.advanceTimersByTimeAsync(10000);
    expect(fetch).toHaveBeenCalledTimes(3);
  });

  it('continues polling when a retry succeeds', async () => {
    const fetch = vi
      .fn()
      .mockRejectedValueOnce(new Error('Temporary failure'))
      .mockResolvedValue([]);
    const state = startPolling(fetch);

    await vi.advanceTimersByTimeAsync(1100);

    expect(fetch).toHaveBeenCalledTimes(2);
    expect(state[errorProperty]).toBeNull();
    expect(state.hasLoaded).toBe(true);
    expect(subscription.closed).toBe(false);

    await vi.advanceTimersByTimeAsync(10000);
    expect(fetch.mock.calls.length).toBeGreaterThan(2);
  });

  it('resets the retry count after a successful poll', async () => {
    let calls = 0;
    const fetch = vi.fn().mockImplementation(() => {
      calls++;
      return calls % 2 === 1
        ? Promise.reject(new Error('Intermittent failure'))
        : Promise.resolve([]);
    });
    const state = startPolling(fetch);

    await vi.advanceTimersByTimeAsync(20000);

    expect(fetch.mock.calls.length).toBeGreaterThan(6);
    expect(state[errorProperty]).toBeNull();
    expect(subscription.closed).toBe(false);
  });

  it('does not retry after unsubscribing during the delay', async () => {
    const fetch = vi.fn().mockRejectedValue(new Error('Actuator unavailable'));
    const state = startPolling(fetch);

    await vi.advanceTimersByTimeAsync(100);
    subscription.unsubscribe();
    await vi.advanceTimersByTimeAsync(3000);

    expect(fetch).toHaveBeenCalledTimes(1);
    expect(state[errorProperty]).toBeNull();
    expect(subscription.closed).toBe(true);
  });
});
