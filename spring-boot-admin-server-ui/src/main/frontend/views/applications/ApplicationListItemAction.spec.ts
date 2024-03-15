/*
 * Copyright 2014-2021 the original author or authors.
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
import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { cloneDeep } from 'lodash-es';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import Application from '../../services/application';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import ApplicationsListItem from '@/views/applications/ApplicationListItemAction';

async function clickConfirmModal() {
  await waitFor(() => {
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  const buttonOK = screen.queryByRole('button', { name: 'term.ok' });
  await userEvent.click(buttonOK);
}

describe('ApplicationListItemAction', () => {
  let application: Application;
  let instance: Instance;

  beforeEach(() => {
    server.use(
      // Instances
      http.delete('/instances/:instanceId', () => {
        return HttpResponse.json({});
      }),
      http.post('/instances/:instanceId/actuator/restart', () => {
        return HttpResponse.json({});
      }),
      http.post('/instances/:instanceId/actuator/shutdown', () => {
        return HttpResponse.json({});
      }),
      // Applications
      http.delete('/applications/:name', () => {
        return HttpResponse.json({});
      }),
      http.post('/applications/:name/actuator/restart', () => {
        return HttpResponse.json({});
      }),
      http.post('/applications/:name/actuator/shutdown', () => {
        return HttpResponse.json({});
      }),
    );
  });

  beforeEach(() => {
    application = new Application(cloneDeep(applications[0]));
    instance = application.instances[0];
  });

  describe('unregister', () => {
    it('on instance', async () => {
      const spy = vi.spyOn(instance, 'unregister');

      render(ApplicationsListItem, {
        props: { item: instance },
      });

      await userEvent.click(screen.getByTitle('Unregister'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });

    it('on application', async () => {
      const spy = vi.spyOn(application, 'unregister');

      render(ApplicationsListItem, {
        props: { item: application },
      });

      await userEvent.click(screen.getByTitle('Unregister'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });
  });

  describe('restart', () => {
    it('on instance', async () => {
      const spy = vi.spyOn(instance, 'restart');

      render(ApplicationsListItem, {
        props: { item: instance },
      });

      await userEvent.click(screen.getByTitle('Restart'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });

    it('on application', async () => {
      const spy = vi.spyOn(application, 'restart');
      render(ApplicationsListItem, {
        props: { item: application },
      });

      await userEvent.click(screen.getByTitle('Restart'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });
  });

  describe('shutdown', () => {
    it('on application', async () => {
      const spy = vi.spyOn(application, 'shutdown');
      render(ApplicationsListItem, {
        props: { item: application },
      });

      await userEvent.click(screen.getByTitle('Shutdown'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });

    it('on instance', async () => {
      const spy = vi.spyOn(instance, 'shutdown');
      render(ApplicationsListItem, {
        props: { item: instance },
      });

      await userEvent.click(await screen.getByTitle('Shutdown'));
      await clickConfirmModal();

      expect(spy).toHaveBeenCalledOnce();
    });
  });
});
