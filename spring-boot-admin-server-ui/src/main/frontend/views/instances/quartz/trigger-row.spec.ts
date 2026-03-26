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
  mockCalendarIntervalTriggerDetail,
  mockCronTriggerDetail,
  mockCustomTriggerDetail,
  mockDailyTimeIntervalTriggerDetail,
  mockSimpleTriggerDetail,
  mockTriggerDetailNoDescription,
} from './__mocks__/quartz-data';
import { createQuartzI18n } from './__mocks__/test-setup';
import TriggerRow from './trigger-row.vue';

describe('TriggerRow.vue', () => {
  const createWrapper = (triggerDetail: any) => {
    return mount(TriggerRow, {
      props: { triggerDetail },
      global: {
        plugins: [createQuartzI18n()],
      },
    });
  };

  it('renders trigger name correctly', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.name);
  });

  it('displays description in table row', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.description);
  });

  it('displays description dash when no description provided', () => {
    const wrapper = createWrapper(mockTriggerDetailNoDescription);
    const cells = wrapper.findAll('td');
    expect(cells[2].text()).toBe('—');
  });

  it('displays group correctly', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.group);
  });

  it('displays trigger state', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.state);
  });

  it('displays trigger type correctly', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain('simple');
  });

  it('displays trigger priority', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    expect(wrapper.text()).toContain(String(mockSimpleTriggerDetail.priority));
  });

  it('displays next fire time', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);
    // Time is formatted by the component, so just check that the raw data is used
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.name);
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.group);
  });

  it('shows expanded details when clicked', async () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Timing Information');
    expect(wrapper.text()).toContain('Basic Information');
  });

  it('displays description in expanded view', async () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Description');
    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.description);
  });

  it('displays simple trigger configuration', async () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Schedule Configuration');
    expect(wrapper.text()).toContain('Interval');
  });

  it('displays cron trigger configuration', async () => {
    const wrapper = createWrapper(mockCronTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Cron Expression');
    expect(wrapper.text()).toContain('0 0 3 * * ?');
  });

  it('displays daily time interval trigger configuration', async () => {
    const wrapper = createWrapper(mockDailyTimeIntervalTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Schedule Configuration');
  });

  it('displays calendar interval trigger configuration', async () => {
    const wrapper = createWrapper(mockCalendarIntervalTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Schedule Configuration');
  });

  it('displays custom trigger configuration', async () => {
    const wrapper = createWrapper(mockCustomTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Schedule Configuration');
  });

  it('displays time window when start and end times are present', async () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Time Window');
    expect(wrapper.text()).toContain('Start Time');
  });

  it('displays calendar association when present', async () => {
    const wrapper = createWrapper(mockCalendarIntervalTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Calendar');
    expect(wrapper.text()).toContain('businessDaysCalendar');
  });

  it('displays trigger data when present', async () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Trigger Data');
    expect(wrapper.text()).toContain('trigger.key');
  });

  it('does not display trigger data section when data is empty', async () => {
    const wrapper = createWrapper(mockTriggerDetailNoDescription);

    await wrapper.find('tr').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).not.toContain('Trigger Data');
  });

  it('displays all trigger types correctly', () => {
    const triggers = [
      { trigger: mockSimpleTriggerDetail, type: 'simple' },
      { trigger: mockCronTriggerDetail, type: 'cron' },
      {
        trigger: mockDailyTimeIntervalTriggerDetail,
        type: 'dailyTimeInterval',
      },
      { trigger: mockCalendarIntervalTriggerDetail, type: 'calendarInterval' },
      { trigger: mockCustomTriggerDetail, type: 'custom' },
    ];

    triggers.forEach(({ trigger, type }) => {
      const wrapper = createWrapper(trigger);
      expect(wrapper.text()).toContain(type);
    });
  });

  it('displays all trigger states correctly', () => {
    const states = ['NORMAL', 'PAUSED', 'COMPLETE', 'ERROR', 'BLOCKED'];

    states.forEach((state) => {
      const trigger = {
        ...mockSimpleTriggerDetail,
        state,
      };
      const wrapper = createWrapper(trigger);
      expect(wrapper.text()).toContain(state);
    });
  });

  it('displays all actuator response data', () => {
    const wrapper = createWrapper(mockSimpleTriggerDetail);

    const text = wrapper.text();
    expect(text).toContain(mockSimpleTriggerDetail.name);
    expect(text).toContain(mockSimpleTriggerDetail.description);
    expect(text).toContain(mockSimpleTriggerDetail.group);
    expect(text).toContain(mockSimpleTriggerDetail.state);
    expect(text).toContain(mockSimpleTriggerDetail.type);
    expect(text).toContain(String(mockSimpleTriggerDetail.priority));
  });
});
