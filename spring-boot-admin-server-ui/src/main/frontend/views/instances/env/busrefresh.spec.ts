import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { server } from '@/mocks/server';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import Busrefresh from '@/views/instances/env/busrefresh.vue';

describe('Busrefresh', () => {
  const busRefreshInstanceContextMock = vi.fn();
  let emitted;

  function createInstance() {
    const instance = new Instance({ id: 1233 });
    instance.busRefreshContext = busRefreshInstanceContextMock;
    return instance;
  }

  beforeEach(async () => {
    server.use(
      http.post('/instances/:instanceId/actuator/busrefresh', () => {
        return HttpResponse.json({});
      }),
    );
    const vm = render(Busrefresh, {
      props: {
        instance: createInstance(),
      },
    });
    emitted = vm.emitted;
  });

  it('should trigger busrefresh on confirm', async () => {
    busRefreshInstanceContextMock.mockResolvedValue({});
    const busRefreshButton = await screen.findByText(
      'instances.env.bus_refresh',
    );
    await userEvent.click(busRefreshButton);

    const confirmButton = await screen.findByText('Confirm');
    await userEvent.click(confirmButton);

    expect(emitted().refresh[0][0]).toBe(true);
  });

  it('should handle busrefresh failure gracefully', async () => {
    busRefreshInstanceContextMock.mockRejectedValueOnce(new Error());

    const busRefreshButton = await screen.findByText(
      'instances.env.bus_refresh',
    );
    await userEvent.click(busRefreshButton);

    const confirmButton = await screen.findByText('Confirm');
    await userEvent.click(confirmButton);

    expect(emitted().refresh).toBeUndefined();
  });
});
