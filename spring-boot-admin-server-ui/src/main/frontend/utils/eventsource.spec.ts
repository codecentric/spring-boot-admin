import { beforeEach, describe, expect, it, vi } from 'vitest';

import loadEventSource, { HEARTBEAT_TIMEOUT } from './eventsource';

const created: Array<{ url: string; options: any }> = [];

vi.mock('event-source-polyfill', () => ({
  EventSourcePolyfill: vi.fn().mockImplementation(function (url, options) {
    created.push({ url, options });
    return { close: vi.fn() };
  }),
}));

describe('loadEventSource', () => {
  beforeEach(() => {
    created.length = 0;
  });

  it('opens the stream with the heartbeat timeout, so a silent connection is detected', async () => {
    const open = await loadEventSource();

    open('applications');

    expect(created).toEqual([
      {
        url: 'applications',
        options: { heartbeatTimeout: HEARTBEAT_TIMEOUT },
      },
    ]);
  });

  it('uses a timeout that tolerates a missed ping of the server (every 10s) but not more', () => {
    expect(HEARTBEAT_TIMEOUT).toBeGreaterThan(20_000);
    expect(HEARTBEAT_TIMEOUT).toBeLessThanOrEqual(45_000);
  });

  it('allows to override the heartbeat timeout', async () => {
    const open = await loadEventSource(5_000);

    open('instances/events');

    expect(created[0].options).toEqual({ heartbeatTimeout: 5_000 });
  });
});
