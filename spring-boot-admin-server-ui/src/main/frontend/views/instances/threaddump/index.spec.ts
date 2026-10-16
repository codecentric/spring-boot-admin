import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import ThreadDump from '@/views/instances/threaddump/index.vue';

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        ...sbaConfig.default.uiSettings,
        pollTimer: {
          ...sbaConfig.default.uiSettings?.pollTimer,
          threaddump: 50,
        },
      },
    },
  };
});

const thread = (threadId: number, threadName: string) => ({
  threadId,
  threadName,
  threadState: 'RUNNABLE',
  stackTrace: [],
});

const renderedThreadNames = () =>
  screen
    .getAllByRole('row')
    .map((row) => row.querySelector('.threads__thread-name span:not([title])'))
    .filter(Boolean)
    .map((span) => span.textContent);

describe('ThreadDump', () => {
  let responses: Array<Array<ReturnType<typeof thread>>>;

  beforeEach(() => {
    let call = 0;
    server.use(
      http.get('/instances/:instanceId/actuator/threaddump', () => {
        const threads = responses[Math.min(call++, responses.length - 1)];
        return HttpResponse.json({ threads });
      }),
    );
  });

  const renderComponent = () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    return render(ThreadDump, { props: { instance } });
  };

  it('renders threads in the order returned by the endpoint', async () => {
    responses = [[thread(42, 'second'), thread(7, 'first')]];

    renderComponent();

    await waitFor(() =>
      expect(renderedThreadNames()).toEqual(['second', 'first']),
    );
  });

  it('follows the response order of subsequent polls', async () => {
    responses = [
      [thread(42, 'a'), thread(7, 'b')],
      [thread(42, 'a'), thread(13, 'c'), thread(7, 'b')],
    ];

    renderComponent();

    await waitFor(() => expect(renderedThreadNames()).toEqual(['a', 'c', 'b']));
  });

  it('moves terminated threads to the bottom', async () => {
    responses = [[thread(1, 'gone'), thread(2, 'alive')], [thread(2, 'alive')]];

    renderComponent();

    await waitFor(() =>
      expect(renderedThreadNames()).toEqual(['alive', 'gone']),
    );
    const terminatedRow = screen.getByText('gone').closest('tr');
    expect(terminatedRow.querySelector('[title]')).toHaveAttribute(
      'title',
      'TERMINATED',
    );
  });
});
