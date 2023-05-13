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

import sbaStatus from './sba-status.vue';

import { render } from '@/test-utils';

moment.now = () => +new Date(1318781879406);

describe('application-status', () => {
  function testSnapshotForStatus(status: string, date?: number) {
    render(sbaStatus, {
      propsData: {
        status,
        date,
      },
    });
  }

  it('should match the snapshot with status UP with Timestamp', async () => {
    const status = 'UP';
    testSnapshotForStatus(status, 1318781000000);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with status RESTRICTED', async () => {
    const status = 'RESTRICTED';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with status OUT_OF_SERVICE', async () => {
    const status = 'OUT_OF_SERVICE';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with status DOWN', async () => {
    const status = 'DOWN';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with status UNKNOWN', async () => {
    const status = 'UNKNOWN';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with status OFFLINE', async () => {
    const status = 'OFFLINE';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });

  it('should match the snapshot with custom status', async () => {
    const status = '?';
    testSnapshotForStatus(status);

    expect(await screen.findByLabelText(status)).toBeDefined();
  });
});
