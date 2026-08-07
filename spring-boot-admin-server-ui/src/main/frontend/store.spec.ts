import { waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { ReplaySubject } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { registerWithOneInstance } from '@/mocks/fixtures/eventStream/registerWithOneInstance';
import { registerWithTwoInstances } from '@/mocks/fixtures/eventStream/registerWithTwoInstances';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import ApplicationStore from '@/store';

describe('store', () => {
  let applicationStore;

  let mockSubject;
  vi.spyOn(Application, 'getStream').mockImplementation(function () {
    return mockSubject;
  });

  let changedListener;
  let addedListener;
  let updateListener;
  let removedListener;

  beforeEach(() => {
    server.use(
      http.get('/applications', () => {
        return HttpResponse.json([]);
      }),
    );

    changedListener = vi.fn();
    addedListener = vi.fn();
    updateListener = vi.fn();
    removedListener = vi.fn();

    mockSubject = new ReplaySubject();
    applicationStore = new ApplicationStore();
    applicationStore.start();
    applicationStore.addEventListener('changed', changedListener);
    applicationStore.addEventListener('added', addedListener);
    applicationStore.addEventListener('updated', updateListener);
    applicationStore.addEventListener('removed', removedListener);
    applicationStore.addEventListener('error', (error) => console.error(error));
  });

  afterEach(() => {
    applicationStore.stop();
  });

  it('registers a new instance', async () => {
    mockSubject.next({ data: registerWithOneInstance });

    await waitFor(() => {
      const applications = applicationStore.applications;
      expect(applications).toHaveLength(1);
      expect(applications[0].instances).toHaveLength(1);
    });

    expect(changedListener).toHaveBeenCalled();
    expect(addedListener).toHaveBeenCalled();
    expect(updateListener).not.toHaveBeenCalled();
    expect(removedListener).not.toHaveBeenCalled();
  });

  it('registers one instance and then another one', async () => {
    mockSubject.next({ data: registerWithOneInstance });
    mockSubject.next({ data: registerWithTwoInstances });

    await waitFor(() => {
      const applications = applicationStore.applications;
      expect(applications).toHaveLength(1);
      expect(applications[0].instances).toHaveLength(2);
    });

    expect(changedListener).toHaveBeenCalled();
    expect(addedListener).toHaveBeenCalled();
    expect(updateListener).toHaveBeenCalled();
    expect(removedListener).not.toHaveBeenCalled();
  });

  it('deregisters an instance', async () => {
    mockSubject.next({ data: registerWithTwoInstances });
    mockSubject.next({ data: registerWithOneInstance });

    await waitFor(() => {
      const applications = applicationStore.applications;
      expect(applications).toHaveLength(1);
      expect(applications[0].instances).toHaveLength(1);
    });

    expect(changedListener).toHaveBeenCalled();
    expect(addedListener).toHaveBeenCalled();
    expect(updateListener).toHaveBeenCalled();
    expect(removedListener).not.toHaveBeenCalled();
  });

  it('handles instance rename: migrates instance to new application and removes the empty old one', async () => {
    // The backend SSE stream re-publishes the new application (with the migrated
    // instance) and then the previous application with an empty instance list so
    // the store can drop it. The instance's registration.name changes while the
    // id/healthUrl stay the same.
    const instance = { ...registerWithOneInstance.instances[0] };
    instance.registration = {
      ...instance.registration,
      name: 'new-service',
    };
    const newService = { ...registerWithOneInstance, name: 'new-service', instances: [instance] };
    const oldServiceEmpty = { ...registerWithOneInstance, name: 'old-service', instances: [] };

    // Seed the store with the application under its previous name.
    const oldService = { ...registerWithOneInstance, name: 'old-service' };
    mockSubject.next({ data: oldService });

    await waitFor(() => {
      expect(applicationStore.applications).toHaveLength(1);
      expect(applicationStore.applications[0].name).toBe('old-service');
    });

    // Simulate the rename update sequence emitted by the backend: new-service
    // gains the instance, old-service ends up empty and must be removed.
    mockSubject.next({ data: newService });
    mockSubject.next({ data: oldServiceEmpty });

    await waitFor(() => {
      expect(applicationStore.applications).toHaveLength(1);
      const app = applicationStore.applications[0];
      expect(app.name).toBe('new-service');
      expect(app.instances).toHaveLength(1);
      expect(app.instances[0].id).toBe(instance.id);
    });

    expect(removedListener).toHaveBeenCalled();
    const removedName = removedListener.mock.calls[removedListener.mock.calls.length - 1][0].name;
    expect(removedName).toBe('old-service');
  });

  it('handles rename when old application still has other instances: updates both groups, removes none', async () => {
    const migratedInstance = {
      ...registerWithOneInstance.instances[0],
      id: 'instance-a',
      registration: {
        ...registerWithOneInstance.instances[0].registration,
        name: 'new-service',
        healthUrl: 'http://localhost:8080/actuator/health',
      },
    };
    const remainingInstance = {
      ...registerWithOneInstance.instances[0],
      id: 'instance-b',
      registration: {
        ...registerWithOneInstance.instances[0].registration,
        name: 'old-service',
        healthUrl: 'http://localhost:8081/actuator/health',
      },
    };

    const oldServiceWithTwo = {
      ...registerWithOneInstance,
      name: 'old-service',
      instances: [
        { ...migratedInstance, registration: { ...migratedInstance.registration, name: 'old-service' } },
        remainingInstance,
      ],
    };
    const newService = { ...registerWithOneInstance, name: 'new-service', instances: [migratedInstance] };
    const oldServiceWithOne = { ...registerWithOneInstance, name: 'old-service', instances: [remainingInstance] };

    mockSubject.next({ data: oldServiceWithTwo });
    await waitFor(() => {
      expect(applicationStore.applications).toHaveLength(1);
      expect(applicationStore.applications[0].instances).toHaveLength(2);
    });

    mockSubject.next({ data: newService });
    mockSubject.next({ data: oldServiceWithOne });

    await waitFor(() => {
      const names = applicationStore.applications.map((a) => a.name).sort();
      expect(names).toEqual(['new-service', 'old-service']);
      const oldApp = applicationStore.applications.find((a) => a.name === 'old-service');
      expect(oldApp.instances).toHaveLength(1);
      expect(oldApp.instances[0].id).toBe('instance-b');
    });

    expect(removedListener).not.toHaveBeenCalled();
  });

  it('removes an application', async () => {
    mockSubject.next({ data: registerWithOneInstance });

    await waitFor(() => {
      expect(applicationStore.applications).toHaveLength(1);
    });

    const data = { ...registerWithOneInstance, instances: [] };
    mockSubject.next({ data: data });

    await waitFor(() => {
      expect(applicationStore.applications).toHaveLength(0);
    });

    expect(changedListener).toHaveBeenCalled();
    expect(addedListener).toHaveBeenCalled();
    expect(updateListener).not.toHaveBeenCalled();
    expect(removedListener).toHaveBeenCalled();
  });
});
