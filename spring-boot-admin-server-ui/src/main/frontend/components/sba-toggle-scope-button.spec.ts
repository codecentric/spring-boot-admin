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
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { render } from '../test-utils.js';
import { ActionScope } from './ActionScope.js';
import SbaToggleScopeButton from './sba-toggle-scope-button.vue';

describe('SbaToggleScopeButton', function () {
  let wrapper;

  beforeEach(() => {
    wrapper = render(SbaToggleScopeButton, {
      props: { instanceCount: 2, modelValue: ActionScope.INSTANCE },
    });
  });

  it('should emit changed scope when clicked', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: /instance/i }),
    );

    expect(wrapper.emitted()['update:modelValue'][0]).toEqual(['application']);
  });

  it('should toggle the scope when clicked twice', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: /instance/i }),
    );
    expect(wrapper.emitted()['update:modelValue'][0]).toEqual(['application']);

    await wrapper.rerender({ modelValue: ActionScope.APPLICATION });

    await userEvent.click(
      await screen.findByRole('button', { name: /application/i }),
    );
    expect(wrapper.emitted()['update:modelValue'][1]).toEqual(['instance']);
  });
});
