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

import {mount} from '@vue/test-utils';
import moment from 'moment';
import sbaStatus from './sba-status';

moment.now = () => +new Date(1318781879406);

describe('application-status', () => {

  const testSnapshotForStatus = (status, date) => {
    const wrapper = mount(sbaStatus, {
      propsData: {
        status,
        date
      },
      stubs: {
        'font-awesome-icon': true
      }
    });
    expect(wrapper.vm.$el).toMatchSnapshot();
  };

  it('should match the snapshot with status UP with Timestamp', () => {
    testSnapshotForStatus('UP', 1318781000000);
  });

  it('should match the snapshot with status RESTRICTED', () => {
    testSnapshotForStatus('RESTRICTED');
  });

  it('should match the snapshot with status OUT_OF_SERVICE', () => {
    testSnapshotForStatus('OUT_OF_SERVICE');
  });

  it('should match the snapshot with status DOWN', () => {
    testSnapshotForStatus('DOWN');
  });

  it('should match the snapshot with status UNKNOWN', () => {
    testSnapshotForStatus('UNKNOWN');
  });

  it('should match the snapshot with status OFFLINE', () => {
    testSnapshotForStatus('OFFLINE');
  });

  it('should match the snapshot with custom status', () => {
    testSnapshotForStatus('?');
  });

});
