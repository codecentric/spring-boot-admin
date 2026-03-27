/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';

import {
  mockJobDetail,
  mockJobDetailNoDescription,
} from './__mocks__/quartz-data';
import { createQuartzI18n } from './__mocks__/test-setup';
import JobRow from './job-row.vue';

describe('JobRow.vue', () => {
  const createWrapper = (jobDetail: any, isExpanded = false, instance: any = {}) => {
    return mount(JobRow, {
      props: {
        jobDetail,
        isExpanded,
        instance,
      },
      global: {
        plugins: [createQuartzI18n()],
      },
    });
  };

  it('renders job name correctly', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain(mockJobDetail.name);
  });

  it('renders job class name correctly', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain(mockJobDetail.className);
  });

  it('displays description in table row', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain(mockJobDetail.description);
  });

  it('displays description dash when no description provided', () => {
    const wrapper = createWrapper(mockJobDetailNoDescription, false);

    const cells = wrapper.findAll('td');
    expect(cells[1].text()).toBe('—');
  });

  it('displays group correctly', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain(mockJobDetail.group);
  });

  it('displays durable status as Yes when true', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain('Yes');
  });

  it('displays durable status as No when false', () => {
    const wrapper = createWrapper(mockJobDetailNoDescription, false);

    expect(wrapper.text()).toContain('No');
  });

  it('displays recovery status as Enabled when true', () => {
    const wrapper = createWrapper(mockJobDetailNoDescription, false);

    expect(wrapper.text()).toContain('Enabled');
  });

  it('displays recovery status as Disabled when false', () => {
    const wrapper = createWrapper(mockJobDetail, false);

    expect(wrapper.text()).toContain('Disabled');
  });

  it('shows expanded details when isExpanded is true', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    expect(wrapper.text()).toContain('Details');
    expect(wrapper.text()).toContain('Configuration');
  });

  it('displays all trigger information in expanded view', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    mockJobDetail.triggers.forEach((trigger) => {
      expect(wrapper.text()).toContain(trigger.name);
      expect(wrapper.text()).toContain(trigger.group);
    });
  });

  it('displays trigger fire times in expanded view', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    expect(wrapper.text()).toContain('Trigger Fire Times');
    mockJobDetail.triggers.forEach((trigger) => {
      if (trigger.previousFireTime) {
        expect(wrapper.text()).toContain('Last Fire');
      }
      if (trigger.nextFireTime) {
        expect(wrapper.text()).toContain('Next Fire');
      }
    });
  });

  it('displays job data in expanded view when present', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    expect(wrapper.text()).toContain('Job Data');
    expect(wrapper.text()).toContain('job.key');
    expect(wrapper.text()).toContain('job-value');
  });

  it('does not display job data section when data is empty', () => {
    const wrapper = createWrapper(mockJobDetailNoDescription, true);

    expect(wrapper.text()).not.toContain('Job Data');
  });

  it('does not display fire times section when no triggers have fire times', () => {
    const jobWithoutFireTimes = {
      ...mockJobDetail,
      triggers: [
        {
          group: 'test',
          name: 'testTrigger',
          priority: 5,
        },
      ],
    };

    const wrapper = createWrapper(jobWithoutFireTimes, true);

    expect(wrapper.text()).not.toContain('Trigger Fire Times');
  });

  it('emits toggle event when clicked', async () => {
    const wrapper = createWrapper(mockJobDetail, false);

    await wrapper.find('tr').trigger('click');

    expect(wrapper.emitted('toggle')).toHaveLength(1);
  });

  it('displays all job detail fields in expanded view', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    expect(wrapper.text()).toContain('Job Name');
    expect(wrapper.text()).toContain('Class');
    expect(wrapper.text()).toContain('Group');
    expect(wrapper.text()).toContain('Description');
    expect(wrapper.text()).toContain('Durable');
    expect(wrapper.text()).toContain('Request Recovery');
  });

  it('displays correct durable and recovery values in expanded view', () => {
    const wrapper = createWrapper(mockJobDetail, true);

    const text = wrapper.text();
    expect(text).toContain('Durable');
    expect(text).toContain('Yes');
    expect(text).toContain('Request Recovery');
    expect(text).toContain('No');
  });
});
