import { fireEvent, screen } from '@testing-library/vue';
import { expect, it, vi } from 'vitest';

import Configprops from './index.vue';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';

it('filters flattened properties without walking the bound bean again', async () => {
  const instance = new Application(applications[0]).instances[0];
  const hosts = vi.fn(() => [{ port: 9090 }, { port: 9091 }]);
  const properties = {
    get servers() {
      return hosts();
    },
    enabled: false,
  };
  vi.spyOn(instance.axios, 'get').mockResolvedValue({
    data: {
      contexts: {
        app: {
          beans: {
            client: { prefix: 'orders', properties },
          },
        },
      },
    },
  });
  render(Configprops, { props: { instance } });
  expect(await screen.findByText('orders.servers[1].port')).toBeVisible();
  const reads = hosts.mock.calls.length;
  const input = screen.getByPlaceholderText('Filter');
  await fireEvent.update(input, '9091');
  expect(screen.getByText('orders.servers[1].port')).toBeVisible();
  expect(screen.queryByText('orders.servers[0].port')).not.toBeInTheDocument();
  await fireEvent.update(input, 'orders.enabled');
  expect(screen.getByText('false')).toBeVisible();
  expect(hosts).toHaveBeenCalledTimes(reads);
});
