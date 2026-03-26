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
import TriggersSection from './triggers-section.vue';

describe('TriggersSection.vue', () => {
  const createWrapper = (triggers: any[] = []) => {
    return mount(TriggersSection, {
      props: { triggers },
      global: {
        plugins: [createQuartzI18n()],
      },
    });
  };

  it('renders nothing when no triggers provided', () => {
    const wrapper = createWrapper([]);
    expect(wrapper.find('h2').exists()).toBe(false);
  });

  it('renders section header and displays all triggers', () => {
    const triggers = [mockSimpleTriggerDetail, mockCronTriggerDetail];
    const wrapper = createWrapper(triggers);

    expect(wrapper.text()).toContain('Triggers');
    expect(wrapper.text()).toContain('2');
    triggers.forEach((t) => {
      expect(wrapper.text()).toContain(t.name);
    });
  });

  it('displays trigger descriptions', () => {
    const wrapper = createWrapper([
      mockSimpleTriggerDetail,
      mockTriggerDetailNoDescription,
    ]);

    expect(wrapper.text()).toContain(mockSimpleTriggerDetail.description);
  });

  it('displays trigger groups and states', () => {
    const triggers = [
      mockSimpleTriggerDetail,
      mockDailyTimeIntervalTriggerDetail,
    ];
    const wrapper = createWrapper(triggers);

    triggers.forEach((t) => {
      expect(wrapper.text()).toContain(t.group);
      expect(wrapper.text()).toContain(t.state);
    });
  });

  it('displays trigger types and priorities', () => {
    const triggers = [mockSimpleTriggerDetail, mockCronTriggerDetail];
    const wrapper = createWrapper(triggers);

    expect(wrapper.text()).toContain('simple');
    expect(wrapper.text()).toContain('cron');
    triggers.forEach((t) => {
      expect(wrapper.text()).toContain(String(t.priority));
    });
  });

  it('displays cron trigger configuration when expanded', () => {
    const wrapper = createWrapper([mockCronTriggerDetail]);

    expect(wrapper.text()).toContain('cron');
    expect(wrapper.text()).toContain(mockCronTriggerDetail.name);
  });

  it('displays all trigger types with correct configuration', () => {
    const triggers = [
      mockSimpleTriggerDetail,
      mockCronTriggerDetail,
      mockDailyTimeIntervalTriggerDetail,
      mockCalendarIntervalTriggerDetail,
      mockCustomTriggerDetail,
    ];
    const wrapper = createWrapper(triggers);

    expect(wrapper.text()).toContain('simple');
    expect(wrapper.text()).toContain('cron');
    expect(wrapper.text()).toContain('dailyTimeInterval');
    expect(wrapper.text()).toContain('calendarInterval');
    expect(wrapper.text()).toContain('custom');
  });

  it('displays all actuator response data', () => {
    const wrapper = createWrapper([mockSimpleTriggerDetail]);

    const text = wrapper.text();
    expect(text).toContain(mockSimpleTriggerDetail.name);
    expect(text).toContain(mockSimpleTriggerDetail.description);
    expect(text).toContain(mockSimpleTriggerDetail.group);
    expect(text).toContain(mockSimpleTriggerDetail.state);
    expect(text).toContain(mockSimpleTriggerDetail.type);
    expect(text).toContain(String(mockSimpleTriggerDetail.priority));
  });
});
