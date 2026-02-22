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
import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';

import JournalView from './index.vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { server } from '@/mocks/server';
import Application from '@/services/application';
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
