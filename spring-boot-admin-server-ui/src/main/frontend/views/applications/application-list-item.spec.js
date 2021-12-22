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

import {render} from '../../test-utils';
import Application from '../../services/application';
import ApplicationListItem from './applications-list-item';
import {applications} from '../../mocks/applications/data';
import {screen, waitFor} from '@testing-library/vue';
import userEvent from '@testing-library/user-event';
import _ from 'lodash';

describe('application-list-item.vue', () => {
  let application;

  beforeEach(() => {
    application = _.cloneDeep(applications[0])
  });

  it('does not show shutdown button when shutdown endpoint is missing', () => {
    application.instances[0].endpoints = [];

    render(ApplicationListItem, { props: {application: new Application(application)}})

    const shutdownButton = screen.queryByTitle('shutdown')
    expect(shutdownButton).toBeNull();
  })

  it('should call shutdown endpoint when modal is confirmed', async () => {
    const {emitted} = render(ApplicationListItem, {props: {application: new Application(application)}})

    const element = screen.queryByTitle('shutdown');
    userEvent.click(element);

    await waitFor(() => {
      screen.findByRole('dialog');
    })

    const buttonOK = screen.queryByRole('button', {name: 'OK'});
    userEvent.click(buttonOK);

    expect(emitted().shutdown).toBeDefined();
  })

  it('does not show restart button when restart endpoint is missing', () => {
    application.instances[0].endpoints = [];

    render(ApplicationListItem, { props: {application: new Application(application)}})

    const shutdownButton = screen.queryByTitle('restart')
    expect(shutdownButton).toBeNull();
  })

  it('should call restart endpoint when modal is confirmed', async () => {
    const {emitted} = render(ApplicationListItem, {props: {application: new Application(application)}})

    const element = screen.queryByTitle('restart');
    userEvent.click(element);

    await waitFor(() => {
      screen.findByRole('dialog');
    })

    const buttonOK = screen.queryByRole('button', {name: 'OK'});
    userEvent.click(buttonOK);

    expect(emitted().restart).toBeDefined();
  })

  it('should show confirmation if application is restarted', async () => {
    render(ApplicationListItem, {props: {application: new Application(application)}})

    const element = screen.queryByTitle('restart');
    userEvent.click(element);

    await waitFor(() => {
      screen.findByRole('dialog');
    })

    const buttonOK = screen.queryByRole('button', {name: 'OK'});
    userEvent.click(buttonOK);

    await waitFor(() => {
      let successDialog = screen.queryByText('Successfully restarted application');
      expect(successDialog).toBeVisible();
    })
  })
})
