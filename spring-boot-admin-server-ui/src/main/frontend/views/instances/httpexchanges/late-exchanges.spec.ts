import { fireEvent, screen } from '@testing-library/vue';
import { afterEach, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import { newHttpRecords } from '@/utils/newHttpRecords';
import HttpExchanges from '@/views/instances/httpexchanges/index.vue';
import Httptrace from '@/views/instances/httptrace/index.vue';

afterEach(() => vi.useRealTimers());

it.each([
  { view: HttpExchanges, field: 'exchanges', list: 'sba-exchanges-list' },
  { view: Httptrace, field: 'traces', list: 'sba-traces-list' },
] as const)(
  'keeps slow requests that finish between $field polls',
  async ({ view, field, list }) => {
    vi.useFakeTimers();
    const item = (path: string, second: number) => ({
      timestamp: new Date(Date.UTC(2025, 0, 1, 0, 0, second)).toISOString(),
      request: { method: 'GET', uri: 'http://service/' + path, headers: {} },
      response: { status: 200, headers: {} },
    });
    const fast = item('fast', 8);
    const slow = item('slow', 2);
    const peer = item('peer', 8);
    const instance = new Application(applications[0]).instances[0];
    vi.spyOn(instance.axios, 'get')
      .mockResolvedValueOnce({ data: { [field]: [fast] } })
      .mockResolvedValue({ data: { [field]: [slow, peer, fast] } });
    const rows = {
      props: [field],
      emits: ['show-new-traces'],
      template:
        '<div><button @click="$emit(\'show-new-traces\')">Show arrivals</button><p v-for="row in ' +
        field +
        '" :key="row.key">{{row.request.uri}}</p></div>',
    };
    render(view, {
      props: { instance },
      global: {
        stubs: {
          [list]: rows,
          'sba-traces-chart': true,
          'sba-exchanges-chart': true,
        },
      },
    });
    await vi.advanceTimersByTimeAsync(10);
    expect(screen.getByText('http://service/fast')).toBeVisible();
    await vi.advanceTimersByTimeAsync(10000);
    await fireEvent.click(screen.getByText('Show arrivals'));
    const paths = screen
      .getAllByText(/^http:\/\/service\//)
      .map((row) => row.textContent);
    expect(paths).toHaveLength(3);
    expect(paths).toContain('http://service/peer');
    expect(paths.at(-1)).toBe('http://service/slow');
  },
);

it('counts repeated identical requests across overlapping polls', () => {
  const read = newHttpRecords();
  const request = {
    timestamp: '2025-01-01T00:00:00Z',
    request: { method: 'GET', uri: '/status' },
  };
  expect(read([request])).toEqual([request]);
  expect(read([request, request])).toEqual([request]);
  expect(read([request, request])).toEqual([]);
});
