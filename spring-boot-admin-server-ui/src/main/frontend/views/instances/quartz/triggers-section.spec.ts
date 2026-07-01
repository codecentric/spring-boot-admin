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
import { describe, expect, it } from 'vitest';

import {
  mockCalendarIntervalTriggerDetail,
  mockCronTriggerDetail,
  mockCustomTriggerDetail,
  mockDailyTimeIntervalTriggerDetail,
  mockSimpleTriggerDetail,
  mockTriggerDetailNoDescription,
} from './__mocks__/quartz-data';
import TriggersSection from './triggers-section.vue';

import { render } from '@/test-utils';

describe('TriggersSection.vue', () => {
  it('renders nothing when no triggers provided', () => {
    const { container } = render(TriggersSection, {
      props: { triggers: [] },
    });
    expect(container.querySelector('h2')).toBe(null);
  });

  it('renders section header and displays all triggers', () => {
    const triggers = [mockSimpleTriggerDetail, mockCronTriggerDetail];
    const { container } = render(TriggersSection, {
      props: { triggers },
    });

    expect(container.textContent).toContain('instances.quartz.triggers');
    expect(container.textContent).toContain('instances.quartz.total');
    triggers.forEach((t) => {
      expect(container.textContent).toContain(t.name);
    });
  });

  it('displays trigger descriptions', () => {
    const { container } = render(TriggersSection, {
      props: {
        triggers: [mockSimpleTriggerDetail, mockTriggerDetailNoDescription],
      },
    });

    expect(container.textContent).toContain(
      mockSimpleTriggerDetail.description,
    );
  });

  it('displays trigger groups and states', () => {
    const triggers = [
      mockSimpleTriggerDetail,
      mockDailyTimeIntervalTriggerDetail,
    ];
    const { container } = render(TriggersSection, {
      props: { triggers },
    });

    triggers.forEach((t) => {
      expect(container.textContent).toContain(t.group);
      expect(container.textContent).toContain(t.state);
    });
  });

  it('displays trigger types and priorities', () => {
    const triggers = [mockSimpleTriggerDetail, mockCronTriggerDetail];
    const { container } = render(TriggersSection, {
      props: { triggers },
    });

    expect(container.textContent).toContain('simple');
    expect(container.textContent).toContain('cron');
    triggers.forEach((t) => {
      expect(container.textContent).toContain(String(t.priority));
    });
  });

  it('displays cron trigger configuration when expanded', () => {
    const { container } = render(TriggersSection, {
      props: { triggers: [mockCronTriggerDetail] },
    });

    expect(container.textContent).toContain('cron');
    expect(container.textContent).toContain(mockCronTriggerDetail.name);
  });

  it('displays all trigger types with correct configuration', () => {
    const triggers = [
      mockSimpleTriggerDetail,
      mockCronTriggerDetail,
      mockDailyTimeIntervalTriggerDetail,
      mockCalendarIntervalTriggerDetail,
      mockCustomTriggerDetail,
    ];
    const { container } = render(TriggersSection, {
      props: { triggers },
    });

    expect(container.textContent).toContain('simple');
    expect(container.textContent).toContain('cron');
    expect(container.textContent).toContain('dailyTimeInterval');
    expect(container.textContent).toContain('calendarInterval');
    expect(container.textContent).toContain('custom');
  });

  it('displays all actuator response data', () => {
    const { container } = render(TriggersSection, {
      props: { triggers: [mockSimpleTriggerDetail] },
    });

    const text = container.textContent;
    expect(text).toContain(mockSimpleTriggerDetail.name);
    expect(text).toContain(mockSimpleTriggerDetail.description);
    expect(text).toContain(mockSimpleTriggerDetail.group);
    expect(text).toContain(mockSimpleTriggerDetail.state);
    expect(text).toContain(mockSimpleTriggerDetail.type);
    expect(text).toContain(String(mockSimpleTriggerDetail.priority));
  });
});
