/**
 * The server sends a ":ping" comment every 10 seconds on every event stream. The native EventSource never exposes
 * comments to JavaScript, so a connection that silently stops delivering data (dropped by a NAT, firewall or route
 * without any close or error) can't be detected with it, and the UI would show stale data forever.
 *
 * The polyfill counts comments as activity and aborts and reports an error if nothing arrives within
 * `heartbeatTimeout`, so the caller can reconnect. It is used in all browsers for that reason.
 */
export const HEARTBEAT_TIMEOUT = 30_000;

export type EventSourceFactory = (url: string) => EventSource;

export default async function loadEventSource(
  heartbeatTimeout = HEARTBEAT_TIMEOUT,
): Promise<EventSourceFactory> {
  const { EventSourcePolyfill } = await import('event-source-polyfill');
  return (url) =>
    new EventSourcePolyfill(url, {
      heartbeatTimeout,
    }) as unknown as EventSource;
}
