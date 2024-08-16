import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import Instance from '@/services/instance.js';
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
