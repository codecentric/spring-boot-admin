import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import Application from './application';
import Instance from './instance';

type FakeEventSource = {
  url: string;
  onmessage: ((message: { data: string }) => void) | null;
  onerror: ((error: unknown) => void) | null;
  close: ReturnType<typeof vi.fn>;
};

const sources: FakeEventSource[] = [];

vi.mock('event-source-polyfill', () => ({
  EventSourcePolyfill: vi.fn().mockImplementation(function (url: string) {
    const source: FakeEventSource = {
      url,
      onmessage: null,
      onerror: null,
      close: vi.fn(),
    };
    sources.push(source);
    return source;
  }),
}));

const opened = async () => {
  await vi.waitFor(() => expect(sources.length).toBeGreaterThan(0));
  return sources[sources.length - 1];
};

describe.each([
  ['Application.getStream', 'applications', () => Application.getStream()],
  [
    'Instance.getEventStream',
    'instances/events',
    () => Instance.getEventStream(),
  ],
])('%s', (_name, url, createStream) => {
  beforeEach(() => {
    sources.length = 0;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('connects to the event stream', async () => {
    const subscription = createStream().subscribe();

    const source = await opened();

    expect(source.url).toBe(url);
    subscription.unsubscribe();
  });

  it('emits an error when the connection reports one, e.g. the heartbeat timeout of a silent connection', async () => {
    const error = vi.fn();
    const subscription = createStream().subscribe({ error });

    const source = await opened();
    const timeout = new Error('No activity within 30000 milliseconds.');
    source.onerror?.(timeout);

    expect(error).toHaveBeenCalledWith(timeout);
    expect(source.close).toHaveBeenCalled();
    subscription.unsubscribe();
  });

  it('closes the connection when unsubscribing', async () => {
    const subscription = createStream().subscribe();

    const source = await opened();
    subscription.unsubscribe();

    expect(source.close).toHaveBeenCalledTimes(1);
  });
});
