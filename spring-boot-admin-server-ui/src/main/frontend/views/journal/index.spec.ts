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

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

class InstanceEvent {
  constructor({ instance, version, type, timestamp, ...payload }) {
    this.instance = instance;
    this.version = version;
    this.type = type;
    this.timestamp = new Date(timestamp);
    this.payload = payload;
  }

  get key() {
    return `${this.instance}-${this.version}`;
  }
}

InstanceEvent.REGISTERED = 'REGISTERED';
InstanceEvent.REGISTRATION_UPDATED = 'REGISTRATION_UPDATED';

describe('Journal View', () => {
  beforeEach(() => {
    server.use(
      http.get('/instances/events', () => {
        return HttpResponse.json([{ event: '' }]);
      }),
    );

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
    const events = [
      new InstanceEvent({
        instance: 'instance-1',
        version: 1,
        type: 'REGISTERED',
        timestamp: '2023-01-01T10:00:00Z',
        registration: { name: 'OLD APP NAME' },
      }),
      new InstanceEvent({
        instance: 'instance-1',
        version: 2,
        type: 'REGISTRATION_UPDATED',
        timestamp: '2023-01-01T11:00:00Z',
        registration: { name: 'NEW APP NAME' },
      }),
    ];

    render(JournalView, {
      data() {
        return {
          events: events,
        };
      },
      global: {
        mocks: {
          $t: (key) => key,
          $route: { query: {} },
          $router: {
            replace: () => {},
          },
        },
      },
    });

    const allByNewText = await screen.findAllByText('App 1');

    expect(allByNewText).toBeDefined();
  });

  it('should handle both REGISTERED and REGISTRATION_UPDATED events', () => {
    const events = [
      new InstanceEvent({
        instance: 'instance-1',
        version: 1,
        type: 'REGISTERED',
        timestamp: '2023-01-01T10:00:00Z',
        registration: { name: 'App One' },
      }),
      new InstanceEvent({
        instance: 'instance-2',
        version: 1,
        type: 'REGISTERED',
        timestamp: '2023-01-01T10:05:00Z',
        registration: { name: 'App Two' },
      }),
      new InstanceEvent({
        instance: 'instance-2',
        version: 2,
        type: 'REGISTRATION_UPDATED',
        timestamp: '2023-01-01T11:00:00Z',
        registration: { name: 'App Two Updated' },
      }),
    ];

    render(JournalView, {
      data() {
        return {
          events: events,
        };
      },
      global: {
        mocks: {
          $t: (key) => key,
          $route: { query: {} },
          $router: { replace: () => {} },
        },
      },
    });

    const appOneText = screen.getAllByText('App 1');

    expect(appOneText).toBeDefined();

    const appTwoText = screen.getAllByText('App 2');

    expect(appTwoText).toBeDefined();
  });
});
