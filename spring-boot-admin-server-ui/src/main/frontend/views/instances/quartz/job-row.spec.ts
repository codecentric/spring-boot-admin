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
import userEvent from '@testing-library/user-event';

import {
  mockJobDetail,
  mockJobDetailNoDescription,
} from './__mocks__/quartz-data';
import { render } from '@/test-utils';
import JobRow from './job-row.vue';

describe('JobRow.vue', () => {
  it('renders job name correctly', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain(mockJobDetail.name);
  });

  it('renders job class name correctly', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain(mockJobDetail.className);
  });

  it('displays description in table row', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain(mockJobDetail.description);
  });

  it('displays description dash when no description provided', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetailNoDescription,
        isExpanded: false,
        instance: {},
      },
    });

    const cells = container.querySelectorAll('td');
    expect(cells[1].textContent).toContain('—');
  });

  it('displays group correctly', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain(mockJobDetail.group);
  });

  it('displays durable status as Yes when true', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.yes');
  });

  it('displays durable status as No when false', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetailNoDescription,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.no');
  });

  it('displays recovery status as Enabled when true', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetailNoDescription,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.enabled');
  });

  it('displays recovery status as Disabled when false', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.disabled');
  });

  it('shows expanded details when isExpanded is true', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.details');
    expect(container.textContent).toContain('instances.quartz.configuration');
  });

  it('displays all trigger information in expanded view', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    mockJobDetail.triggers.forEach((trigger) => {
      expect(container.textContent).toContain(trigger.name);
      expect(container.textContent).toContain(trigger.group);
    });
  });

  it('displays trigger fire times in expanded view', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.trigger_fire_times');
    mockJobDetail.triggers.forEach((trigger) => {
      if (trigger.previousFireTime) {
        expect(container.textContent).toContain('instances.quartz.last_fire');
      }
      if (trigger.nextFireTime) {
        expect(container.textContent).toContain('instances.quartz.next_fire');
      }
    });
  });

  it('displays job data in expanded view when present', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.job_data');
    expect(container.textContent).toContain('job.key');
    expect(container.textContent).toContain('job-value');
  });

  it('does not display job data section when data is empty', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetailNoDescription,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).not.toContain('instances.quartz.job_data');
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

    const { container } = render(JobRow, {
      props: {
        jobDetail: jobWithoutFireTimes,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).not.toContain('instances.quartz.trigger_fire_times');
  });

  it('emits toggle event when clicked', async () => {
    const { container, emitted } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: false,
        instance: {},
      },
    });

    const user = userEvent.setup();
    const row = container.querySelector('tr');
    await user.click(row!);

    expect(emitted().toggle).toBeDefined();
    expect(emitted().toggle).toHaveLength(1);
  });

  it('displays all job detail fields in expanded view', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.job_name');
    expect(container.textContent).toContain('instances.quartz.class');
    expect(container.textContent).toContain('instances.quartz.group');
    expect(container.textContent).toContain('instances.quartz.description');
    expect(container.textContent).toContain('instances.quartz.durable');
    expect(container.textContent).toContain('instances.quartz.request_recovery');
  });

  it('displays correct durable and recovery values in expanded view', () => {
    const { container } = render(JobRow, {
      props: {
        jobDetail: mockJobDetail,
        isExpanded: true,
        instance: {},
      },
    });

    expect(container.textContent).toContain('instances.quartz.durable');
    expect(container.textContent).toContain('instances.quartz.yes');
    expect(container.textContent).toContain('instances.quartz.request_recovery');
    expect(container.textContent).toContain('instances.quartz.no');
  });
});
