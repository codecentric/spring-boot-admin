import { render, screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { expect, it } from 'vitest';
import { nextTick } from 'vue';
import { RouterView, createMemoryHistory, createRouter } from 'vue-router';

import InstanceShell from './InstanceShell.vue';

import { createViewRegistry } from '@/composables/ViewRegistry';
import {
  createApplicationStore,
  useApplicationStore,
} from '@/composables/useApplicationStore';
import { instance as sample } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Caches from '@/views/instances/caches/index.vue';

it('loads caches for the newly selected instance and keeps them across store updates', async () => {
  const requests: string[] = [];
  server.use(
    http.get('*/instances/:id/actuator/caches', ({ params }) => {
      const id = String(params.id);
      requests.push(id);
      return HttpResponse.json({
        cacheManagers: { main: { caches: { [id + '-cache']: {} } } },
      });
    }),
  );

  createApplicationStore();
  createViewRegistry();
  const { applications } = useApplicationStore();
  const application = () =>
    new Application({
      name: 'test',
      instances: ['first', 'second'].map((id) => ({ ...sample, id })),
    });
  applications.value = [application()];

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/instances/:instanceId',
        component: InstanceShell,
        children: [{ path: 'caches', component: Caches }],
      },
    ],
  });
  await router.push('/instances/first/caches');
  render(RouterView, {
    global: {
      plugins: [router],
      mocks: { $t: (key: string) => key },
      stubs: {
        Sidebar: true,
        SbaWave: true,
        SbaInstanceSection: { template: '<section><slot /></section>' },
        SbaPanel: { template: '<div><slot /></div>' },
        SbaButton: true,
        FontAwesomeIcon: true,
      },
    },
  });
  expect(await screen.findByText('first-cache')).toBeVisible();

  await router.push('/instances/second/caches');
  expect(await screen.findByText('second-cache')).toBeVisible();
  expect(screen.queryByText('first-cache')).not.toBeInTheDocument();

  applications.value = [application()];
  await nextTick();
  expect(screen.getByText('second-cache')).toBeVisible();
  expect(requests).toEqual(['first', 'second']);
});
