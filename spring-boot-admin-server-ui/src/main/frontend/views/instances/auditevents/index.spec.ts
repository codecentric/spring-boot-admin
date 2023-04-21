import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';

import Instance from '@/services/instance.js';
import { render } from '@/test-utils';
import Auditevents from '@/views/instances/auditevents/index';

describe('Auditevents', () => {
  const fetchAuditevents = jest.fn().mockResolvedValue({
    data: {
      events: [],
    },
  });

  beforeEach(async () => {
    await render(Auditevents, {
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
      'instances.auditevents.principal'
    );
    userEvent.type(input, 'Abc');

    await waitFor(() => {
      let calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].principal).toEqual('Abc');
    });
  });

  it('fetches data when filter for Type is changed', async () => {
    const input = await screen.findByPlaceholderText(
      'instances.auditevents.type'
    );
    userEvent.type(input, 'AUTHENTICATION_FAILURE');

    await waitFor(() => {
      let calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].type).toEqual('AUTHENTICATION_FAILURE');
    });
  });

  it('handles error when fetching data', async () => {
    await render(Auditevents, {
      props: {
        instance: createInstance(
          jest.fn().mockRejectedValue({
            response: {
              headers: {
                'content-type': 'application/vnd.spring-boot.actuator.v2',
              },
            },
          })
        ),
      },
    });

    await screen.findByText('Fetching of data failed.');
  });

  function createInstance(fetchAuditevents) {
    let instance = new Instance({ id: 4711 });
    instance.fetchAuditevents = fetchAuditevents;
    return instance;
  }
});
