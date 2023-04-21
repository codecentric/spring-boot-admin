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
import { screen, waitFor, within } from '@testing-library/vue';
import { cloneDeep } from 'lodash-es';

import Application from '../../services/application';
import ApplicationListItem from './applications-list-item';

import { applications } from '@/mocks/applications/data';
import { render } from '@/test-utils';

async function clickConfirmModal() {
  await waitFor(() => {
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  const buttonOK = screen.queryByRole('button', { name: 'term.ok' });
  await userEvent.click(buttonOK);
}

describe('application-list-item.vue', () => {
  describe('unregister', () => {
    it('on instance', async () => {
      let application = new Application(cloneDeep(applications[0]));
      const { emitted } = render(ApplicationListItem, {
        props: { application, isExpanded: true },
      });

      const htmlElement = await screen.findByTestId(
        application.instances[0].id
      );
      const element = within(htmlElement).queryByTitle(
        'applications.actions.unregister'
      );
      await userEvent.click(element);

      await clickConfirmModal();

      await waitFor(() => expect(emitted('unregister')).toBeDefined());
    });

    it('on application', async () => {
      const { emitted } = render(ApplicationListItem, {
        props: { application: new Application(cloneDeep(applications[0])) },
      });

      const element = screen.queryByTitle('applications.actions.unregister');
      await userEvent.click(element);

      await clickConfirmModal();

      await waitFor(() => expect(emitted('unregister')).toBeDefined());
    });
  });

  describe('restart', () => {
    it('on instance', async () => {
      let application = new Application(cloneDeep(applications[0]));
      const { emitted } = render(ApplicationListItem, {
        props: { application, isExpanded: true },
      });

      const htmlElement = await screen.findByTestId(
        application.instances[0].id
      );
      const element = within(htmlElement).queryByTitle(
        'applications.actions.restart'
      );
      await userEvent.click(element);

      await clickConfirmModal();

      await waitFor(() => expect(emitted('restart')).toBeDefined());
    });

    it('on application', async () => {
      const { emitted } = render(ApplicationListItem, {
        props: { application: new Application(cloneDeep(applications[0])) },
      });

      const element = screen.queryByTitle('applications.actions.restart');
      await userEvent.click(element);

      await clickConfirmModal();

      await waitFor(() => expect(emitted('restart')).toBeDefined());
    });
  });

  describe('shutdown', () => {
    it('on application', async () => {
      const { emitted } = render(ApplicationListItem, {
        props: { application: new Application(cloneDeep(applications[0])) },
      });

      const element = await screen.findByTitle('applications.actions.shutdown');
      await userEvent.click(element);

      await clickConfirmModal();

      expect(emitted('shutdown')).toBeDefined();
    });

    it('on instance', async () => {
      let application = new Application(cloneDeep(applications[0]));
      const { emitted } = render(ApplicationListItem, {
        props: { application, isExpanded: true },
      });

      let htmlElement = await screen.findByTestId(application.instances[0].id);

      const element = await within(htmlElement).findByTitle(
        'applications.actions.shutdown'
      );
      await userEvent.click(element);

      await clickConfirmModal();

      expect(emitted('shutdown')).toBeDefined();
    });
  });
});
