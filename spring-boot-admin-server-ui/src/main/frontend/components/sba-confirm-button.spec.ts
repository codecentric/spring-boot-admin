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

import SbaConfirmButton from './sba-confirm-button.vue';

import { render } from '@/test-utils';

describe('SbaConfirmButton', function () {
  let emitted;

  beforeEach(() => {
    const vm = render(SbaConfirmButton, { slots: { default: 'Execute' } });
    emitted = vm.emitted;
  });

  it('should not emit when clicked once', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: 'Execute' }),
    );

    expect(emitted().click).toBeUndefined();
  });

  it('should emit when click confirmed', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: 'Execute' }),
    );
    await userEvent.click(
      await screen.findByRole('button', { name: 'Confirm' }),
    );

    expect(emitted().click).toBeDefined();
  });
});
