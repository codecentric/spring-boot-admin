import { fireEvent, screen, waitFor } from '@testing-library/vue';
import { flushPromises } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { expect, it, vi } from 'vitest';

import Sessions from './index.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

const showSessions = (query: Record<string, string>) => {
  const instance = new Application(applications[0]).instances[0];
  render(Sessions, {
    props: { instance },
    global: {
      mocks: {
        $route: { name: 'instances/sessions', query },
        $router: { replace: vi.fn() },
      },
    },
  });
  return instance;
};

it("keeps the current user's sessions when an earlier lookup completes last", async () => {
  const release = Promise.withResolvers<void>();
  server.use(
    http.get('*/actuator/sessions', async ({ request }) => {
      const username = new URL(request.url).searchParams.get('username');
      if (username === 'alice') await release.promise;
      return HttpResponse.json({ sessions: [{ id: username + '-session' }] });
    }),
  );
  const instance = showSessions({ username: 'alice' });
  const fetch = vi.spyOn(instance, 'fetchSessionsByUsername');
  await waitFor(() => expect(fetch).toHaveBeenCalledWith('alice'));
  await fireEvent.update(screen.getByDisplayValue('alice'), 'bob');
  expect(await screen.findByText('bob-session')).toBeVisible();
  release.resolve();
  await fetch.mock.results[0].value;
  await flushPromises();
  expect(screen.queryByText('alice-session')).not.toBeInTheDocument();
  expect(screen.getByText('bob-session')).toBeVisible();
});

it('submits an ID search immediately without querying for a username', async () => {
  server.use(
    http.get('*/actuator/sessions/:id', ({ params }) =>
      HttpResponse.json({ id: params.id }),
    ),
  );
  const instance = showSessions({ sessionId: 'first-id' });
  await screen.findByText('first-id');
  const byUsername = vi.spyOn(instance, 'fetchSessionsByUsername');
  const byId = vi.spyOn(instance, 'fetchSession');
  const input = screen.getByDisplayValue('first-id');
  await fireEvent.update(input, 'second-id');
  await fireEvent.keyUp(input, { key: 'Enter' });
  expect(byId).toHaveBeenCalledWith('second-id');
  expect(byUsername).not.toHaveBeenCalled();
  expect(await screen.findByText('second-id')).toBeVisible();
});
