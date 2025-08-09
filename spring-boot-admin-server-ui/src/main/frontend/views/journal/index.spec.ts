import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';

import JournalView from './index.vue';

import { server } from '@/mocks/server';
import { render } from '@/test-utils';

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
        return HttpResponse.json({});
      }),
    );
  });

  it('should update instance names when registration is updated', () => {
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

    const { container } = render(JournalView, {
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

    const instanceNames =
      container.firstChild.__vueParentComponent.ctx.instanceNames;

    // Should use the most recent name from REGISTRATION_UPDATED event
    expect(instanceNames['instance-1']).toBe('NEW APP NAME');
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

    const wrapper = mount(JournalView, {
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

    const instanceNames = wrapper.vm.instanceNames;

    // Instance 1 should keep original name (only REGISTERED event)
    expect(instanceNames['instance-1']).toBe('App One');

    // Instance 2 should have updated name (from REGISTRATION_UPDATED event)
    expect(instanceNames['instance-2']).toBe('App Two Updated');
  });
});
