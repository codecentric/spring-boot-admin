/*
 * Copyright 2014-2018 the original author or authors.
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

import {render} from '@/test-utils';
import SbaToggleScopeButton from './sba-toggle-scope-button';
import userEvent from '@testing-library/user-event';
import {screen} from '@testing-library/vue';

describe('SbaToggleScopeButton', function () {
  let wrapper;

  beforeEach(() => {
    wrapper = render(SbaToggleScopeButton, {props: {instanceCount: 2, scope: 'instance'}})
  })

  it('should emit changed scope when clicked', async () => {
    userEvent.click(await screen.findByRole('button', {name: 'Instance'}));

    expect(wrapper.emitted().changeScope[0]).toEqual(['application']);
  });

  it('should toggle the scope when clicked twice', async () => {
    userEvent.click(await screen.findByRole('button', {name: 'Instance'}));
    expect(wrapper.emitted().changeScope[0]).toEqual(['application']);

    wrapper.updateProps({scope: 'application'})

    userEvent.click(await screen.findByRole('button', {name: 'Application'}));
    expect(wrapper.emitted().changeScope[1]).toEqual(['instance']);
  });
});
