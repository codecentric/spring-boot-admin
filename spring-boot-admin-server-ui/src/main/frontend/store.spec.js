import { waitFor } from '@testing-library/vue';
import { rest } from 'msw';
import { ReplaySubject } from 'rxjs';

import { registerWithOneInstance } from '@/mocks/fixtures/eventStream/registerWithOneInstance';
import { registerWithTwoInstances } from '@/mocks/fixtures/eventStream/registerWithTwoInstances';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import ApplicationStore from '@/store';

server.use(
  rest.get('/applications', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json([]));
  })
);

describe('store.js', () => {
  let applicationStore;

  let mockSubject;
  jest.spyOn(Application, 'getStream').mockImplementation(() => mockSubject);

  let changedListener;
  let addedListener;
  let updateListener;
  let removedListener;

  beforeEach(() => {
    changedListener = jest.fn();
    addedListener = jest.fn();
    updateListener = jest.fn();
    removedListener = jest.fn();

    mockSubject = new ReplaySubject();
    applicationStore = new ApplicationStore();
    applicationStore.start();
    applicationStore.addEventListener('changed', changedListener);
    applicationStore.addEventListener('added', addedListener);
    applicationStore.addEventListener('updated', updateListener);
    applicationStore.addEventListener('removed', removedListener);
    applicationStore.addEventListener('error', (error) => console.log(error));
  });

  afterEach(() => {
    applicationStore.stop();
  });

  it('registers a new instance', async () => {
    mockSubject.next({ data: registerWithOneInstance });

    await waitFor(() => {
      let applications = applicationStore.applications;
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
      let applications = applicationStore.applications;
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
      let applications = applicationStore.applications;
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

    let data = { ...registerWithOneInstance, instances: [] };
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
