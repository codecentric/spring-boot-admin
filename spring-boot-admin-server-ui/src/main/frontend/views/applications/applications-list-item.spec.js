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

import { applications } from '../../mocks/applications/data';
import Application from '../../services/application';
import { render } from '../../test-utils';
import ApplicationListItem from './applications-list-item';

describe('application-list-item.vue', () => {
  let application;

  beforeEach(() => {
    application = cloneDeep(applications[0]);
  });

  it('does not show shutdown button when shutdown endpoint is missing', () => {
    application.instances[0].endpoints = [];

    render(ApplicationListItem, {
      props: { application: new Application(application) },
    });

    const shutdownButton = screen.queryByTitle('shutdown');
    expect(shutdownButton).toBeNull();
  });

  it('should call shutdown endpoint when modal is confirmed', async () => {
    const { emitted } = render(ApplicationListItem, {
      props: { application: new Application(application) },
    });

    const element = screen.queryByTitle('shutdown');
    await userEvent.click(element);

    await waitFor(() => {
      screen.findByRole('dialog');
    });

    const buttonOK = screen.queryByRole('button', { name: 'OK' });
    await userEvent.click(buttonOK);

    expect(emitted()).toBeDefined();
  });

  it('does not show restart button when restart endpoint is missing', () => {
    application.instances[0].endpoints = [];

    render(ApplicationListItem, {
      props: { application: new Application(application) },
    });

    const shutdownButton = screen.queryByTitle('applications.actions.restart');
    expect(shutdownButton).toBeNull();
  });

  it('should call restart endpoint when modal is confirmed', async () => {
    const { emitted } = render(ApplicationListItem, {
      props: { application: new Application(application) },
    });

    const element = screen.queryByTitle('applications.actions.restart');
    await userEvent.click(element);
    await waitFor(() => {
      screen.findByRole('dialog');
    });

    const buttonOK = screen.queryByRole('button', { name: 'term.ok' });
    await userEvent.click(buttonOK);

    let emitted1 = emitted();
    expect(emitted1.restart).toBeDefined();
  });
});
