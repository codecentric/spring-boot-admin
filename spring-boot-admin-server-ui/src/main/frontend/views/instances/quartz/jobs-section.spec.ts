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
  mockJobDetail,
  mockJobDetailNoDescription,
} from './__mocks__/quartz-data';
import { render } from '@/test-utils';
import JobsSection from './jobs-section.vue';

describe('JobsSection.vue', () => {

  it('renders nothing when no jobs provided', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [] },
    });
    expect(container.querySelector('h2')).toBe(null);
  });

  it('renders section header and displays all jobs', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail, mockJobDetailNoDescription] },
    });

    expect(container.textContent).toContain('Jobs');
    expect(container.textContent).toContain('2');
    expect(container.textContent).toContain(mockJobDetail.name);
    expect(container.textContent).toContain(mockJobDetailNoDescription.name);
  });

  it('displays job descriptions', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail, mockJobDetailNoDescription] },
    });

    expect(container.textContent).toContain(mockJobDetail.description);
  });

  it('displays job groups and durable status', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail, mockJobDetailNoDescription] },
    });

    expect(container.textContent).toContain(mockJobDetail.group);
    expect(container.textContent).toContain('Yes');
    expect(container.textContent).toContain('No');
  });

  it('displays durable and recovery status correctly', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail, mockJobDetailNoDescription] },
    });

    expect(container.textContent).toContain('Durable');
    expect(container.textContent).toContain('Recovery');
  });

  it('displays enabled and disabled recovery status', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail, mockJobDetailNoDescription] },
    });

    expect(container.textContent).toContain('Disabled');
    expect(container.textContent).toContain('Enabled');
  });

  it('displays all job data from actuator response', () => {
    const { container } = render(JobsSection, {
      props: { jobs: [mockJobDetail] },
    });

    const text = container.textContent;
    expect(text).toContain(mockJobDetail.name);
    expect(text).toContain(mockJobDetail.className);
    expect(text).toContain(mockJobDetail.description);
    expect(text).toContain(mockJobDetail.group);
    expect(text).toContain('Durable');
  });
});
