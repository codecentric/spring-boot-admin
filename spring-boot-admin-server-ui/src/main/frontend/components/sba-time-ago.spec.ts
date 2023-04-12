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
import { screen } from '@testing-library/vue';
import moment from 'moment';
import { describe, expect, it } from 'vitest';

import sbaTimeAgo from './sba-time-ago.vue';

import { render } from '@/test-utils';

// Sun Oct 16 2011 18:00:00 GMT+0200 (Mitteleuropäische Sommerzeit)
moment.now = () => Date.now();

describe('time-ago', () => {
  it('should show short period', async () => {
    render(sbaTimeAgo, {
      propsData: {
        date: moment().subtract(15, 'minutes').utc(),
      },
    });

    expect(await screen.findByText('15m'));
  });

  it('multiple minutes', async () => {
    render(sbaTimeAgo, {
      propsData: {
        date: moment().subtract(800, 'minutes').utc(),
      },
    });

    expect(await screen.findByText('13h'));
  });

  it('multiple days', async () => {
    render(sbaTimeAgo, {
      propsData: {
        date: moment().subtract(5, 'days'),
      },
    });

    expect(await screen.findByText('5d'));
  });
});
