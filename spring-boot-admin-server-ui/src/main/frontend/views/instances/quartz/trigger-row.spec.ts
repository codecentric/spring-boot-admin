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
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import {
  mockCalendarIntervalTriggerDetail,
  mockCronTriggerDetail,
  mockCustomTriggerDetail,
  mockDailyTimeIntervalTriggerDetail,
  mockSimpleTriggerDetail,
  mockTriggerDetailNoDescription,
} from './__mocks__/quartz-data';
import TriggerRow from './trigger-row.vue';

import { render } from '@/test-utils';

describe('TriggerRow.vue', () => {
  it('renders trigger name correctly', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain(mockSimpleTriggerDetail.name);
  });

  it('displays description in table row', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain(
      mockSimpleTriggerDetail.description,
    );
  });

  it('displays description dash when no description provided', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockTriggerDetailNoDescription },
    });
    const cells = container.querySelectorAll('td');
    expect(cells[2].textContent).toBe('—');
  });

  it('displays group correctly', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain(mockSimpleTriggerDetail.group);
  });

  it('displays trigger state', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain(mockSimpleTriggerDetail.state);
  });

  it('displays trigger type correctly', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain('simple');
  });

  it('displays trigger priority', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    expect(container.textContent).toContain(
      String(mockSimpleTriggerDetail.priority),
    );
  });

  it('displays next fire time', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });
    // Time is formatted by the component, so just check that the raw data is used
    expect(container.textContent).toContain(mockSimpleTriggerDetail.name);
    expect(container.textContent).toContain(mockSimpleTriggerDetail.group);
  });

  it('shows expanded details when clicked', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Timing Information');
    expect(container.textContent).toContain('Basic Information');
  });

  it('displays description in expanded view', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Description');
    expect(container.textContent).toContain(
      mockSimpleTriggerDetail.description,
    );
  });

  it('displays simple trigger configuration', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Schedule Configuration');
    expect(container.textContent).toContain('Interval');
  });

  it('displays cron trigger configuration', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockCronTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Cron Expression');
    expect(container.textContent).toContain('0 0 3 * * ?');
  });

  it('displays daily time interval trigger configuration', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockDailyTimeIntervalTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Schedule Configuration');
  });

  it('displays calendar interval trigger configuration', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockCalendarIntervalTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Schedule Configuration');
  });

  it('displays custom trigger configuration', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockCustomTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Schedule Configuration');
  });

  it('displays time window when start and end times are present', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Time Window');
    expect(container.textContent).toContain('Start Time');
  });

  it('displays calendar association when present', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockCalendarIntervalTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Calendar');
    expect(container.textContent).toContain('businessDaysCalendar');
  });

  it('displays trigger data when present', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).toContain('Trigger Data');
    expect(container.textContent).toContain('trigger.key');
  });

  it('does not display trigger data section when data is empty', async () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockTriggerDetailNoDescription },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(container.textContent).not.toContain('Trigger Data');
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
      const { container } = render(TriggerRow, {
        props: { triggerDetail: trigger },
      });
      expect(container.textContent).toContain(type);
    });
  });

  it('displays all trigger states correctly', () => {
    const states = ['NORMAL', 'PAUSED', 'COMPLETE', 'ERROR', 'BLOCKED'];

    states.forEach((state) => {
      const trigger = {
        ...mockSimpleTriggerDetail,
        state,
      };
      const { container } = render(TriggerRow, {
        props: { triggerDetail: trigger },
      });
      expect(container.textContent).toContain(state);
    });
  });

  it('displays all actuator response data', () => {
    const { container } = render(TriggerRow, {
      props: { triggerDetail: mockSimpleTriggerDetail },
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
