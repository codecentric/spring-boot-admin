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
import { beforeEach, describe, expect, it, vi } from 'vitest';

import SbaActionButtonScoped from './sba-action-button-scoped.vue';

import { render } from '@/test-utils';

describe('SbaActionButtonScoped', function () {
  const actionFn = vi.fn().mockResolvedValue([]);

  beforeEach(() => {
    render(SbaActionButtonScoped, {
      props: {
        instanceCount: 10,
        label: 'Execute',
        actionFn,
      },
    });
  });

  it('should cal actionFn when confirmed', async () => {
    await userEvent.click(
      await screen.findByRole('button', { name: 'Execute' }),
    );
    await userEvent.click(
      await screen.findByRole('button', { name: 'Confirm' }),
    );

    expect(actionFn).toHaveBeenCalledTimes(1);
  });
});
