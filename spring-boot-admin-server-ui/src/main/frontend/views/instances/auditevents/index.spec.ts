import userEvent from '@testing-library/user-event';
import { fireEvent, screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import Auditevents from '@/views/instances/auditevents/index.vue';

describe('Auditevents', () => {
  const fetchAuditevents = vi.fn().mockResolvedValue({
    data: {
      events: [],
    },
  });

  beforeEach(async () => {
    render(Auditevents, {
      props: {
        instance: createInstance(fetchAuditevents),
      },
    });
  });

  it('fetches data on startup', async () => {
    await waitFor(() => expect(fetchAuditevents).toHaveBeenCalled());
  });

  it('fetches data when filter for Principal is changed', async () => {
    const input = await screen.findByPlaceholderText(
      'instances.auditevents.principal',
    );
    await userEvent.type(input, 'Abc');

    await waitFor(() => {
      const calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].principal).toEqual('Abc');
    });
  });

  it('fetches data when filter for Type is changed', async () => {
    const input = await screen.findByPlaceholderText(
      'instances.auditevents.type',
    );
    await userEvent.type(input, 'AUTHENTICATION_FAILURE');

    await waitFor(() => {
      const calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].type).toEqual('AUTHENTICATION_FAILURE');
    });
  });

  it('handles error when fetching data', async () => {
    render(Auditevents, {
      props: {
        instance: createInstance(
          vi.fn().mockRejectedValue({
            response: {
              headers: {
                'content-type': 'application/vnd.spring-boot.actuator.v2',
              },
            },
          }),
        ),
      },
    });

    await screen.findByText('Fetching of data failed.');
  });

  function createInstance(fetchAuditevents) {
    const instance = new Instance({ id: 4711 });
    instance.fetchAuditevents = fetchAuditevents;
    return instance;
  }
});

it('sends the edited local date and omits after when the filter is cleared', async () => {
  const dates: (string | null)[] = [];
  server.use(
    http.get('*/actuator/auditevents', ({ request }) => {
      dates.push(new URL(request.url).searchParams.get('after'));
      return HttpResponse.json({ events: [] });
    }),
  );
  render(Auditevents, {
    props: { instance: new Application(applications[0]).instances[0] },
  });
  const input = screen.getByPlaceholderText('Date');
  await waitFor(() => expect(dates).toHaveLength(1));

  await fireEvent.update(input, '2025-04-07T10:30');
  await waitFor(() =>
    expect(dates.at(-1)).toBe(new Date(2025, 3, 7, 10, 30).toISOString()),
  );
  await fireEvent.update(input, '');
  await waitFor(() => expect(dates.at(-1)).toBeNull());
});
