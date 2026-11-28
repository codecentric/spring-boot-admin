/*!
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { fireEvent, screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';

import JournalView from './index.vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import {
  InstanceEvent,
  InstanceEventType,
} from '@/views/journal/InstanceEvent';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

describe('Journal View', () => {
  beforeEach(() => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    useApplicationStore.mockReturnValue({
      applicationsInitialized: ref(true),
      applications: ref([
        new Application({
          id: 'app-1',
          name: 'App 1',
          instances: [{ id: 'instance-1' }],
        }),
        new Application({
          id: 'app-2',
          name: 'App 2',
          instances: [{ id: 'instance-2' }],
        }),
      ]),
      error: ref(null),
    });
  });

  it('merges history with events received while the initial request is pending', async () => {
    const history = Promise.withResolvers<void>();
    const stream = new Subject<{ data: InstanceEvent }>();
    vi.spyOn(Instance, 'getEventStream').mockReturnValueOnce(stream);
    const event = (version: number) =>
      new InstanceEvent({
        instance: 'one',
        version,
        type: InstanceEventType.INFO_CHANGED,
        timestamp: new Date(2025, 0, 1, 0, version),
      });
    server.use(
      http.get('**/instances/events', async () => {
        await history.promise;
        return HttpResponse.json([event(5), event(6)]);
      }),
    );
    render(JournalView, {
      global: {
        stubs: {
          JournalTable: {
            props: ['events'],
            template:
              '<ul><li v-for="event in events" :key="event.key">{{event.version}}</li></ul>',
          },
        },
      },
    });
    stream.next({ data: event(6) });
    await screen.findByText('6');
    const autoUpdate = screen.getByRole('button');
    await fireEvent.click(autoUpdate);
    stream.next({ data: event(7) });
    history.resolve();
    await screen.findByText('5');
    expect(screen.queryByText('7')).not.toBeInTheDocument();
    await fireEvent.click(autoUpdate);
    stream.next({ data: event(5) });
    await waitFor(() =>
      expect(
        screen.getAllByRole('listitem').map((row) => row.textContent),
      ).toEqual(['7', '6', '5']),
    );
  });

  it('should update instance names when registration is updated', async () => {
    server.use(
      http.get('**/instances/events', () => {
        return HttpResponse.json([
          new InstanceEvent({
            instance: 'instance-1',
            version: 1,
            type: InstanceEventType.REGISTERED,
            timestamp: new Date('2023-01-01T10:00:00Z'),
            registration: { name: 'OLD APP NAME' },
          }),
          new InstanceEvent({
            instance: 'instance-1',
            version: 2,
            type: InstanceEventType.REGISTRATION_UPDATED,
            timestamp: new Date('2023-01-01T11:00:00Z'),
            registration: { name: 'NEW APP NAME' },
          }),
        ]);
      }),
    );
    render(JournalView);

    const allByNewText = await screen.findAllByText('NEW APP NAME');
    expect(allByNewText).toBeDefined();
  });

  it('should handle both REGISTERED and REGISTRATION_UPDATED events', async () => {
    server.use(
      http.get('**/instances/events', () => {
        return HttpResponse.json([
          new InstanceEvent({
            instance: 'instance-1',
            version: 1,
            type: InstanceEventType.REGISTERED,
            timestamp: new Date('2023-01-01T10:00:00Z'),
            registration: { name: 'App One' },
          }),
          new InstanceEvent({
            instance: 'instance-2',
            version: 1,
            type: InstanceEventType.REGISTERED,
            timestamp: new Date('2023-01-01T10:05:00Z'),
            registration: { name: 'App Two' },
          }),
          new InstanceEvent({
            instance: 'instance-2',
            version: 2,
            type: InstanceEventType.REGISTRATION_UPDATED,
            timestamp: new Date('2023-01-01T11:00:00Z'),
            registration: { name: 'App Two Updated' },
          }),
        ]);
      }),
    );
    render(JournalView);

    const appOneText = await screen.findAllByText('App One');
    expect(appOneText).toBeDefined();
    const appTwoText = await screen.findAllByText('App Two Updated');
    expect(appTwoText).toBeDefined();
  });
});
