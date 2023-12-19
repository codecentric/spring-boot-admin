import { waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { ReplaySubject } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { registerWithOneInstance } from '@/mocks/fixtures/eventStream/registerWithOneInstance';
import { registerWithTwoInstances } from '@/mocks/fixtures/eventStream/registerWithTwoInstances';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import ApplicationStore from '@/store';

describe('store.js', () => {
  let applicationStore;

  let mockSubject;
  vi.spyOn(Application, 'getStream').mockImplementation(() => mockSubject);

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
