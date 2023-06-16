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
import { describe, expect, it } from 'vitest';

import { render } from '../test-utils';
import SbaModal from './sba-modal.vue';

describe('sba-modal.vue', () => {
  it('modal is closed when close button is clicked', async () => {
    const { emitted } = render(SbaModal, {
      props: { modelValue: true },
      slots: { body: '<div>test</div>' },
    });

    await waitFor(() => {
      expect(screen.queryByLabelText('Close')).toBeInTheDocument();
    });
    await userEvent.click(screen.getByLabelText('Close'));

    expect(emitted()['update:modelValue'][0]).toContain(false);
  });
});
