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

import {render} from '../test-utils';
import SbaModal from './sba-modal';
import userEvent from '@testing-library/user-event';
import {screen} from '@testing-library/vue';

describe('sba-modal.vue', () => {
  it('modal is closed when close button is clicked', async () => {
    const {emitted} = render(SbaModal, {props: {open: true}})

    userEvent.click(screen.getByLabelText('close'));

    expect(emitted().change[0]).toContain(false);
  })
})
