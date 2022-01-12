import {render} from '@/test-utils';
import Auditevents from '@/views/instances/auditevents/index';
import Instance from '@/services/instance';
import {screen, waitFor} from '@testing-library/vue';
import userEvent from '@testing-library/user-event';

describe('Auditevents', () => {
  const fetchAuditevents = jest.fn().mockResolvedValue({
    data: {
      events: []
    }
  });

  beforeEach(async () => {
    await render(Auditevents, {
      props: {
        instance: createInstance(fetchAuditevents)
      }
    });
  });

  it('fetches data on startup', async () => {
    await waitFor(() => expect(fetchAuditevents).toHaveBeenCalled());
  });

  it('fetches data when filter for Principal is changed', async () => {
    const input = await screen.findByPlaceholderText('Principal');
    userEvent.type(input, 'Abc')

    await waitFor(() => {
      let calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].principal).toEqual('Abc');
    });
  });

  it('fetches data when filter for Type is changed', async () => {
    const input = await screen.findByPlaceholderText('Type');
    userEvent.type(input, 'AUTHENTICATION_FAILURE')

    await waitFor(() => {
      let calls = fetchAuditevents.mock.calls;
      expect(calls[calls.length - 1][0].type).toEqual('AUTHENTICATION_FAILURE');
    });
  });

  it('handles error when fetching data', async () => {
    await render(Auditevents, {
      props: {
        instance: createInstance(jest.fn().mockRejectedValue({
          response: {
            headers: {
              'content-type': 'application/vnd.spring-boot.actuator.v2'
            }
          }
        }))
      }
    });

    await screen.findByText('Fetching audit events failed.');
  });

  it('handles error when fetching data from Spring 1 services', async () => {
    await render(Auditevents, {
      props: {
        instance: createInstance(jest.fn().mockRejectedValue({
          response: {
            headers: {
              'content-type': ''
            }
          }
        }))
      }
    });

    await screen.findByText('Audit Log is not supported for Spring Boot 1.x applications.');
  });

  function createInstance(fetchAuditevents) {
    let instance = new Instance({id: 4711});
    instance.fetchAuditevents = fetchAuditevents;
    return instance;
  }
});
