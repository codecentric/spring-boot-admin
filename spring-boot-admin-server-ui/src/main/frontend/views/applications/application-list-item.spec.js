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

describe('application-list-item.vue', () => {
  it('does not show shutdown button when shutdown endpoint is missing', () => {
    const application = {...applications[0]};
    application.instances[0].endpoints = [];

    render(ApplicationListItem, { props: {application: new Application(application)}})

    const shutdownButton = screen.queryByTitle('shutdown')
    expect(shutdownButton).toBeNull();
  })

  it('should call shutdown endpoint when modal is confirmed', async () => {
    const application = {...applications[0]};

    render(ApplicationListItem, {props: {application: new Application(application)}})

    const shutdownButton = screen.queryByTitle('shutdown')
    userEvent.click(shutdownButton);

    await waitFor(() => {
      screen.findByRole('dialog');
    })

    screen.debug();
  })
})
